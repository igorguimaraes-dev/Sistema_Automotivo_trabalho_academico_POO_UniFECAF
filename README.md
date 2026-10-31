# Sistema Automotivo — Gestão de Estoque de Veículos

Trabalho de Object Oriented Programming — UniFECAF. Sistema web em Java para cadastrar, consultar, filtrar, atualizar e remover veículos, com interface Vaadin, API REST e persistência em MySQL.

## Funcionalidades

- Cadastro e listagem de marcas e de modelos associados às marcas.
- Cadastro de veículos com modelo, marca obtida pelo modelo, ano de fabricação, cor, preço, quilometragem, status e tipo.
- Consulta geral e por ID; filtros por marca, modelo, faixa de preço, ano, status e tipo, isolados ou combinados.
- Atualização de preço, quilometragem e status.
- Exclusão de veículos vendidos ou descontinuados.

## Tecnologias e arquitetura

Java 21, Spring Boot 4.1.2, Vaadin Flow 25.3.4, Spring Web MVC, Spring Data JPA, Jakarta Validation e MySQL 8.4. As telas estão em `view`, escritas em componentes Java, com CSS para a aparência. A API está organizada em `controller`, `service`, `repository`, `model`, `dto` e `exception`. A interface e a API utilizam os mesmos services. As entidades herdam a identificação de `EntidadeBase`; campos privados e métodos de alteração protegem as regras do veículo.

O frontend opcional foi implementado com três telas: Veículos, Marcas e Modelos. A API também pode ser utilizada por um cliente HTTP, como Postman. A coleção de requisições está em `postman/`.

## Execução no Windows/PowerShell

Pré-requisitos: JDK 21 e MySQL Server 8.4. Para compilar os recursos visuais, utilize Node.js 24 ou superior; o Vaadin pode baixar a versão compatível quando necessário. Docker Desktop pode fornecer o MySQL, conforme a alternativa abaixo. O Maven Wrapper está incluído; o primeiro uso precisa de internet para baixar dependências.

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

Interface: **http://localhost:8080/**. A página inicial exibe o estoque. O menu dá acesso a **Marcas** (`/marcas`) e **Modelos** (`/modelos`). `PORT` pode alterar a porta HTTP; ajuste também a variável `baseUrl` da coleção.

Para começar, cadastre uma marca, cadastre um modelo dessa marca e clique em **Novo veículo**. Os filtros podem ser combinados e limpos. **Editar** permite alterar preço, quilometragem e disponibilidade. **Excluir** exige confirmação e fica disponível para veículos vendidos ou descontinuados. Nos campos monetários da interface, use vírgula para os centavos, por exemplo `75000,50`.

API: `http://localhost:8080/api`. Exemplo: `GET http://localhost:8080/api/marcas` retorna `[]` em banco vazio. Em JSON, números continuam usando ponto decimal, conforme os exemplos abaixo.

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
.\mvnw.cmd -Pproduction clean verify
```

Se usar outro MySQL, crie previamente `estoque_veiculos_test`, conceda acesso ao usuário de `DB_USER` e configure:

```powershell
$env:TEST_DB_URL = 'jdbc:mysql://localhost:3306/estoque_veiculos_test'
.\mvnw.cmd -Pproduction clean verify
```

Os quatro cenários de integração cobrem CRUD e dados salvos, filtros isolados/combinados, entradas inválidas e associações/duplicidades. Um teste adicional verifica a mensagem amigável quando não é possível abrir a transação com o banco. A interface também foi verificada em navegador real, incluindo cancelamento, confirmação de exclusão, valores monetários e largura de celular. Os relatórios do Maven ficam em `target/surefire-reports/`.

O perfil `production` empacota os recursos visuais, sem as ferramentas de desenvolvimento do Vaadin. Após o build, execute com as mesmas variáveis de conexão:

```powershell
java -jar target/sistema-automotivo-0.0.1-SNAPSHOT.jar
```

## Entregáveis

- [Relatório teórico](docs/relatorio-teorico.pdf).
- [Evidências práticas e códigos principais](docs/evidencias-praticas.pdf).
- [Vídeo pitch no YouTube](https://youtu.be/nzAiVEuqOuE), com execução real da API e narração sintética. [Arquivo MP4](docs/pitch.mp4).
- [Código-fonte público](https://github.com/igorguimaraes-dev/sistema-automotivo-poo).

O vídeo original demonstra a API por cliente HTTP. O relatório prático também contém capturas da interface Java implementada posteriormente. Não foram implementados login, vendas financeiras ou funcionalidades externas à gestão de estoque solicitada.
