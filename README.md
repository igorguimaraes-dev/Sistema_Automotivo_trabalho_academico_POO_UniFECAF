# Sistema Automotivo — Gestão de Estoque de Veículos

Trabalho de Object Oriented Programming — UniFECAF. API REST orientada a objetos para cadastrar, consultar, filtrar, atualizar e remover veículos, com persistência em MySQL.

## Funcionalidades

- Cadastro e listagem de marcas e de modelos associados às marcas.
- Cadastro de veículos com modelo, marca obtida pelo modelo, ano de fabricação, cor, preço, quilometragem, status e tipo.
- Consulta geral e por ID; filtros por marca, modelo, faixa de preço, ano, status e tipo, isolados ou combinados.
- Atualização de preço, quilometragem e status.
- Exclusão de veículos vendidos ou descontinuados.

## Tecnologias e arquitetura

Java 21, Spring Boot 4.1.2, Spring Web MVC, Spring Data JPA, Jakarta Validation e MySQL 8.4. A API está organizada em `controller`, `service`, `repository`, `model`, `dto` e `exception`. As entidades herdam a identificação de `EntidadeBase`; campos privados e métodos de alteração protegem as regras do veículo.

O frontend é opcional no enunciado e não integra esta entrega. A API pode ser utilizada por um cliente HTTP, como Postman. A coleção de requisições está em `postman/`.

## Execução no Windows/PowerShell

Pré-requisitos: JDK 21 e MySQL Server 8.4. Docker Desktop pode fornecer o MySQL, conforme a alternativa abaixo. O Maven Wrapper está incluído; o primeiro uso precisa de internet para baixar dependências.

Abra um terminal **na pasta deste README, que contém `pom.xml`**. Confira `java -version` e configure `JAVA_HOME` para o seu JDK 21, se necessário.

### Alternativa A — MySQL com Docker

Escolha uma senha local e mantenha-a somente no seu terminal:

```powershell
$env:DB_PASSWORD = 'SUBSTITUA_POR_UMA_SENHA_LOCAL'
docker compose up -d --wait
$env:DB_USER = 'estoque'
$env:DB_URL = 'jdbc:mysql://localhost:3308/estoque_veiculos'
.\mvnw.cmd spring-boot:run
```

O Compose cria os bancos `estoque_veiculos` e `estoque_veiculos_test` e o usuário `estoque`. A porta local padrão do banco é 3308; se estiver ocupada, defina `DB_PORT` antes de subir e ajuste `DB_URL`/`TEST_DB_URL`. Os dados ficam no volume `mysql_dados`. `docker compose down` para o container sem apagar esse volume.

### Alternativa B — MySQL já instalado

No seu cliente SQL, crie o banco:

```sql
CREATE DATABASE estoque_veiculos
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Use um usuário local que tenha acesso a esse banco:

```powershell
$env:DB_USER = 'SEU_USUARIO_MYSQL'
$env:DB_PASSWORD = 'SUA_SENHA_LOCAL'
$env:DB_URL = 'jdbc:mysql://localhost:3306/estoque_veiculos'
.\mvnw.cmd spring-boot:run
```

A aplicação executa `database/schema.sql` para criar as tabelas que ainda não existem e valida o mapeamento JPA. Esse script é o modelo físico da entrega. Ele não apaga tabelas nem migra versões antigas de estrutura.

### Endereço

API: `http://localhost:8080/api`. Exemplo inicial: `GET http://localhost:8080/api/marcas` retorna `[]` em banco vazio. A raiz `/` não é uma tela do sistema. `PORT` pode alterar a porta HTTP; ajuste também a variável `baseUrl` da coleção.

## Endpoints

| Método | Caminho | Operação |
|---|---|---|
| POST / GET | `/api/marcas` | Cadastrar / listar marcas |
| POST / GET | `/api/modelos` | Cadastrar / listar modelos |
| POST / GET | `/api/veiculos` | Cadastrar / listar e filtrar veículos |
| GET | `/api/veiculos/{id}` | Consultar veículo |
| PATCH | `/api/veiculos/{id}` | Atualizar preço, quilometragem e status |
| DELETE | `/api/veiculos/{id}` | Remover vendido ou descontinuado |

### Sequência de uso

Envie JSON com `Content-Type: application/json` e use os IDs realmente retornados; os números abaixo são exemplos.

1. `POST /api/marcas`: `{"nome":"Toyota"}`.
2. `POST /api/modelos`: `{"nome":"Corolla","marcaId":1}`.
3. `POST /api/veiculos`:

```json
{
  "modeloId": 1,
  "anoFabricacao": 2022,
  "cor": "Prata",
  "preco": 110000.00,
  "quilometragem": 35000,
  "status": "DISPONIVEL",
  "tipo": "CARRO"
}
```

4. `GET /api/veiculos?marcaId=1&modeloId=1&precoMin=100000&precoMax=120000&ano=2022&status=DISPONIVEL&tipo=CARRO`.
5. `PATCH /api/veiculos/1`: `{"preco":108000.50,"quilometragem":36000,"status":"VENDIDO"}`.
6. `DELETE /api/veiculos/1` retorna 204 sem corpo. O GET posterior desse ID retorna 404.

Todos os filtros são opcionais; quando combinados, todos precisam ser atendidos. Limites de preço são inclusivos. Consulta sem correspondência retorna `[]`.

O PATCH exige os três campos comerciais. Status aceitos: `DISPONIVEL`, `VENDIDO`, `DESCONTINUADO`. Tipos: `CARRO`, `MOTO`, `UTILITARIO`. Preço deve ser positivo, com até duas casas decimais; quilometragem deve ser inteira e não negativa. O ano de fabricação deve estar entre 1886 e o ano atual. A marca é derivada do modelo, evitando associações incompatíveis.

Entradas inválidas retornam 400; recurso inexistente, 404; duplicidade ou tentativa de excluir um disponível, 409. A exclusão não remove a marca ou o modelo associados. Marcar como vendido preserva o registro até uma exclusão explícita.

## Coleção de requisições

Importe `postman/estoque-veiculos.postman_collection.json`, confira `baseUrl` e execute na ordem indicada. Os cadastros salvam automaticamente `marcaId`, `modeloId` e `veiculoId` na coleção. Reexecutar a criação da mesma marca/modelo retorna conflito; utilize nomes novos ou reutilize os IDs existentes.

## Testes e compilação

Os testes exercitam HTTP e MySQL reais. **Eles apagam os registros somente do banco `estoque_veiculos_test` antes de cada cenário**, e verificam o nome do banco antes da limpeza. Nunca use esse banco para informações que deseja preservar.

Com o banco iniciado pelo Compose e `DB_PASSWORD` definido, encerre a API com `Ctrl+C` antes de recompilar, especialmente se estiver executando o JAR no Windows:

```powershell
.\mvnw.cmd clean verify
```

Se usar outro MySQL, crie previamente `estoque_veiculos_test`, conceda acesso ao usuário de `DB_USER` e configure:

```powershell
$env:TEST_DB_URL = 'jdbc:mysql://localhost:3306/estoque_veiculos_test'
.\mvnw.cmd clean verify
```

Os quatro cenários cobrem CRUD e dados salvos, filtros isolados/combinados, entradas inválidas e associações/duplicidades. Os relatórios do Maven ficam em `target/surefire-reports/`. Após o build, também é possível executar:

```powershell
java -jar target/sistema-automotivo-0.0.1-SNAPSHOT.jar
```

## Entregáveis

- [Relatório teórico](docs/relatorio-teorico.pdf).
- [Evidências práticas e códigos principais](docs/evidencias-praticas.pdf).
- [Vídeo pitch no YouTube](https://youtu.be/nzAiVEuqOuE), com execução real da API e narração sintética. [Arquivo MP4](docs/pitch.mp4).
- [Código-fonte público](https://github.com/igorguimaraes-dev/sistema-automotivo-poo).

As capturas e o vídeo usam um cliente HTTP local para demonstrar a API. Esse cliente é uma ferramenta de teste, não um frontend entregue. Não foram implementados login, vendas financeiras ou funcionalidades externas à gestão de estoque solicitada.
