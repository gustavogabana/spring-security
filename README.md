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