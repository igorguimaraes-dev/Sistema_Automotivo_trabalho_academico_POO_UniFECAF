package com.unifecaf.automotivo;

import java.net.URI;
import java.net.http.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EstoqueApiTest {
    @Value("${local.server.port}") int port;
    @Autowired JdbcTemplate jdbc;
    final HttpClient client = HttpClient.newHttpClient();
    final ObjectMapper json = new ObjectMapper();

    @BeforeEach void limparBancoDeTeste() {
        String database = jdbc.queryForObject("SELECT DATABASE()", String.class);
        assertThat(database).isEqualTo("estoque_veiculos_test");
        jdbc.execute("DELETE FROM veiculo");
        jdbc.execute("DELETE FROM modelo");
        jdbc.execute("DELETE FROM marca");
    }

    HttpResponse<String> request(String method, String path, String body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api" + path))
                .header("Content-Type", "application/json");
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body));
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
    JsonNode node(HttpResponse<String> response, int status) {
        assertThat(response.statusCode()).as(response.body()).isEqualTo(status);
        return json.readTree(response.body());
    }
    long marca(String nome) throws Exception {
        return node(request("POST", "/marcas", "{\"nome\":\"" + nome + "\"}"), 201).get("id").asLong();
    }
    long modelo(String nome, long marcaId) throws Exception {
        return node(request("POST", "/modelos", "{\"nome\":\"" + nome + "\",\"marcaId\":" + marcaId + "}"), 201).get("id").asLong();
    }
    String dados(long modeloId, int ano, String preco, String status, String tipo) {
        return "{\"modeloId\":" + modeloId + ",\"anoFabricacao\":" + ano
                + ",\"cor\":\"Prata\",\"preco\":" + preco
                + ",\"quilometragem\":35000,\"status\":\"" + status + "\",\"tipo\":\"" + tipo + "\"}";
    }

    @Test void cadastroConsultaAtualizacaoExclusaoPersistemNoMysql() throws Exception {
        long ma = marca("Toyota"), mo = modelo("Corolla", ma);
        assertThat(node(request("GET", "/marcas", null), 200).size()).isEqualTo(1);
        assertThat(node(request("GET", "/modelos", null), 200).get(0).get("marca").get("id").asLong()).isEqualTo(ma);
        var criado = node(request("POST", "/veiculos", dados(mo, 2022, "110000.00", "DISPONIVEL", "CARRO")), 201);
        long id = criado.get("id").asLong();
        assertThat(criado.get("marca").get("nome").asText()).isEqualTo("Toyota");
        assertThat(criado.get("modelo").get("nome").asText()).isEqualTo("Corolla");
        assertThat(criado.get("anoFabricacao").asInt()).isEqualTo(2022);
        assertThat(criado.get("cor").asText()).isEqualTo("Prata");
        assertThat(criado.get("quilometragem").asInt()).isEqualTo(35000);
        node(request("PATCH", "/veiculos/" + id,
                "{\"preco\":108000.50,\"quilometragem\":36000,\"status\":\"VENDIDO\"}"), 200);
        var consultado = node(request("GET", "/veiculos/" + id, null), 200);
        assertThat(consultado.get("preco").decimalValue()).isEqualByComparingTo("108000.50");
        assertThat(consultado.get("quilometragem").asInt()).isEqualTo(36000);
        assertThat(consultado.get("status").asText()).isEqualTo("VENDIDO");
        assertThat(jdbc.queryForObject("SELECT preco FROM veiculo WHERE id=?", BigDecimal.class, id))
                .isEqualByComparingTo("108000.50");
        assertThat(request("DELETE", "/veiculos/" + id, null).statusCode()).isEqualTo(204);
        node(request("GET", "/veiculos/" + id, null), 404);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM veiculo", Long.class)).isZero();
    }

    @Test void atualizacaoCompletaAlteraCaracteristicasSemCriarOutroVeiculo() throws Exception {
        long honda = marca("Honda"), toyota = marca("Toyota");
        long cg = modelo("CG 160", honda), corolla = modelo("Corolla", toyota);
        long id = node(request("POST", "/veiculos", dados(cg, 2011, "15000", "DISPONIVEL", "MOTO")), 201).get("id").asLong();
        String novosDados = dados(corolla, 2020, "90000.50", "VENDIDO", "CARRO")
                .replace("Prata", "Preto").replace("35000", "42000");
        var atualizado = node(request("PUT", "/veiculos/" + id, novosDados), 200);
        assertThat(atualizado.get("id").asLong()).isEqualTo(id);
        assertThat(atualizado.get("marca").get("id").asLong()).isEqualTo(toyota);
        assertThat(atualizado.get("modelo").get("id").asLong()).isEqualTo(corolla);
        assertThat(atualizado.get("anoFabricacao").asInt()).isEqualTo(2020);
        assertThat(atualizado.get("cor").asText()).isEqualTo("Preto");
        assertThat(atualizado.get("tipo").asText()).isEqualTo("CARRO");
        assertThat(atualizado.get("preco").decimalValue()).isEqualByComparingTo("90000.50");
        assertThat(atualizado.get("quilometragem").asInt()).isEqualTo(42000);
        assertThat(atualizado.get("status").asText()).isEqualTo("VENDIDO");
        assertThat(node(request("GET", "/veiculos/" + id, null), 200)).isEqualTo(atualizado);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM veiculo", Long.class)).isEqualTo(1L);
        assertThat(jdbc.queryForObject("SELECT modelo_id FROM veiculo WHERE id=?", Long.class, id)).isEqualTo(corolla);
        assertThat(node(request("GET", "/veiculos?marcaId=" + honda, null), 200).size()).isZero();
        assertThat(node(request("GET", "/veiculos?marcaId=" + toyota + "&ano=2020", null), 200).size()).isEqualTo(1);
    }

    @Test void atualizacaoCompletaInvalidaPreservaTodosOsDadosAnteriores() throws Exception {
        long mo = modelo("CG 160", marca("Honda"));
        long id = node(request("POST", "/veiculos", dados(mo, 2011, "15000", "DISPONIVEL", "MOTO")), 201).get("id").asLong();
        var anterior = node(request("GET", "/veiculos/" + id, null), 200);
        String novo = dados(mo, 2020, "16000", "VENDIDO", "CARRO").replace("Prata", "Preto");
        for (String invalido : new String[]{novo.replace("Preto", "  "), novo.replace("16000", "-1"),
                novo.replace("2020", "3500"), "{}"}) {
            node(request("PUT", "/veiculos/" + id, invalido), 400);
            assertThat(node(request("GET", "/veiculos/" + id, null), 200)).isEqualTo(anterior);
        }
        node(request("PUT", "/veiculos/" + id, dados(999999999, 2020, "16000", "VENDIDO", "CARRO")), 404);
        assertThat(node(request("GET", "/veiculos/" + id, null), 200)).isEqualTo(anterior);
        node(request("PUT", "/veiculos/999999999", novo), 404);
    }

    @Test void filtrosIsoladosECombinadosSelecionamOsVeiculosCorretos() throws Exception {
        long toyota = marca("Toyota"), fiat = marca("Fiat"), honda = marca("Honda");
        long corolla = modelo("Corolla", toyota), argo = modelo("Argo", fiat), cg = modelo("CG 160", honda);
        long esperado = node(request("POST", "/veiculos", dados(corolla, 2022, "110000", "DISPONIVEL", "CARRO")), 201).get("id").asLong();
        node(request("POST", "/veiculos", dados(argo, 2021, "65000", "DISPONIVEL", "CARRO")), 201);
        node(request("POST", "/veiculos", dados(cg, 2020, "15000", "VENDIDO", "MOTO")), 201);
        String[] filtros = {"marcaId=" + toyota, "modeloId=" + corolla, "precoMin=110000&precoMax=110000", "ano=2022",
                "marcaId=" + toyota + "&modeloId=" + corolla + "&precoMin=100000&precoMax=120000&ano=2022&status=DISPONIVEL&tipo=CARRO"};
        for (String filtro : filtros) {
            var resultado = node(request("GET", "/veiculos?" + filtro, null), 200);
            assertThat(resultado.size()).as(filtro).isEqualTo(1);
            assertThat(resultado.get(0).get("id").asLong()).isEqualTo(esperado);
        }
        assertThat(node(request("GET", "/veiculos?status=DISPONIVEL", null), 200).size()).isEqualTo(2);
        assertThat(node(request("GET", "/veiculos?tipo=MOTO", null), 200).get(0).get("modelo").get("nome").asText()).isEqualTo("CG 160");
        assertThat(node(request("GET", "/veiculos?precoMax=100", null), 200).size()).isZero();
        assertThat(node(request("GET", "/veiculos?ano=2022&marcaId=" + fiat, null), 200).size()).isZero();
    }

    @Test void entradasInvalidasNaoCriamRegistrosNemAlteramVeiculo() throws Exception {
        long mo = modelo("Corolla", marca("Toyota"));
        for (String body : new String[]{dados(mo, 2022, "-1", "DISPONIVEL", "CARRO"),
                dados(mo, 2022, "10.001", "DISPONIVEL", "CARRO"),
                dados(mo, 2022, "100", "INVALIDO", "CARRO"),
                dados(mo, 2022, "100", "DISPONIVEL", "CARRO").replace("35000", "-1"),
                dados(mo, 2022, "100", "DISPONIVEL", "CARRO").replace("Prata", "  "), "{}"}) {
            node(request("POST", "/veiculos", body), 400);
        }
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM veiculo", Long.class)).isZero();
        long id = node(request("POST", "/veiculos", dados(mo, 2022, "100", "DISPONIVEL", "CARRO")), 201).get("id").asLong();
        node(request("PATCH", "/veiculos/" + id, "{\"preco\":-5,\"quilometragem\":0,\"status\":\"VENDIDO\"}"), 400);
        assertThat(node(request("GET", "/veiculos/" + id, null), 200).get("preco").decimalValue()).isEqualByComparingTo("100");
        node(request("GET", "/veiculos?precoMin=500&precoMax=100", null), 400);
        node(request("GET", "/veiculos?status=INVALIDO", null), 400);
    }

    @Test void associacoesInexistentesEDuplicidadesSaoRecusadas() throws Exception {
        long ma = marca("Toyota"), mo = modelo("Corolla", ma);
        node(request("POST", "/marcas", "{\"nome\":\"Toyota\"}"), 409);
        node(request("POST", "/modelos", "{\"nome\":\"Corolla\",\"marcaId\":" + ma + "}"), 409);
        node(request("POST", "/modelos", "{\"nome\":\"X\",\"marcaId\":999999999}"), 404);
        node(request("POST", "/veiculos", dados(999999999, 2022, "100", "DISPONIVEL", "CARRO")), 404);
        node(request("GET", "/veiculos/999999999", null), 404);
        node(request("PATCH", "/veiculos/999999999", "{\"preco\":100,\"quilometragem\":0,\"status\":\"VENDIDO\"}"), 404);
        node(request("DELETE", "/veiculos/999999999", null), 404);
        long id = node(request("POST", "/veiculos", dados(mo, 2022, "100", "DISPONIVEL", "CARRO")), 201).get("id").asLong();
        node(request("DELETE", "/veiculos/" + id, null), 409);
        node(request("PATCH", "/veiculos/" + id, "{\"preco\":100,\"quilometragem\":0,\"status\":\"DESCONTINUADO\"}"), 200);
        assertThat(request("DELETE", "/veiculos/" + id, null).statusCode()).isEqualTo(204);
    }
}
