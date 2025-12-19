# Projeto Teste para documentar a configuração do Spring Security

## Comando para executar o banco de dados via docker:
```bash
docker run -d \
  --name postgres-tutorial \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=manager \
  -e POSTGRES_DB=tutorialseguranca \
  -p 5432:5432 \
  -v postgres-data:/var/lib/postgresql \
  postgres:latest
```
## Flyway

O flyway é um esquema de migration para querys no banco de dados. Ele funciona de forma sequencial por versão.
A sintaxe obrigatória do arquivo é vx__ e a descrição .sql.
Uma vez que o arquivo é executado, ele não pode ser mais modificado.
Para cada query nova, é necessário um novo arquivo de migration flyway.

## Implementação do Security

### UserDetails

No Spring Security, a autenticação e a autorização são baseadas numa espécie de contrato.
O framework precisa saber quem é usuário, qual a senha e quais as permissões que este usuário possui.
Esse contrato é definida por uma interface específica, chamada de user details.
A classe principal que representa o usuário precisa implementar essa interface.

### Repository

Na implementação do repository da entidade usuário, precisa existir um método que retorna o UserDetails.
Essa classe contém todos os dados da autenticação do usuário.
Como a classe implementa a interface UserDetails, não deve haver problemas.
