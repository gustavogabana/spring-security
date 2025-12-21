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

### Default deny all

Por padrão, o spring security vem fechado, bloquando todas as rotas.
Significa que tudo exige um autenticação até que seja configurado.
Essa configuração é feita atarvés da classe SecurityConfig.
Essa classe define como as requisições vão ser tratadas em relação a segurança do backend.

### @Configuration e @EnableWebSecurity

@Configuration define que a classe é um bean e será gerenciada pelo spring.
@EnableWebSecurity habilita a segurança web e permite aos devs criar a regras de acesso.
Para isso, é preciso criar um método chamado security filter chain.
Esse método define o pipeline de filtros de segurança.

### CSRF e CORS

CSRF (Cross-Site Request Forgery) é um ataque que força um usuário logado em uma aplicação a executar ações malicíosas.
É desativado no spring security porque, normalmente, são usados tokens JWT stateless, que previnem esses ataques.
CORS (Cross-Origin Resource Sharing) controla a política Same-Origin Policy (SOP) dos browsers.
Essa política impede que requisições sejam aceitas de fora do mesmo domínio.
É desativada para fins de desenvolvimento ou, se em produção, quando a outra forma de controle configurada pelos devs.

### Session management e session create policy stateless

Define que o tipo da sessão será stateles para esse servidor backend.
Stateless significa que o servidor não irá se preocupar em saber se o usuário está logado/ativo ou não.
Normalemente isso é feito via token JWT, assinado com info dos usuários e enviado a cada requisição pelo cliente.

### authorizeHttpRequests

Método que retorna um http security que recebe como parâmetros as políticas de permissão e bloqueio de requests por verbos.
Cada verbo deve ser passado com uma rota específica do servidor.

### UsernamePasswordAuthenticationToken, AuthenticationManager, Authentication e authorities

UsernamePasswordAuthenticationToken vem do spring security e serve como um DTO interno do spring.
Ele recebe as informações que constituem login e senha do app e retornam o DTO interno prenchido.
AuthenticationManager é a classe responsável por receber esse DTO e realizar a verificação das infos no banco de dados.
Se as informações estiverem corretas com o que está na tabela da entidade que implementa a user details, retorna sucesso.
O tipo Authentication, além de sucesso, contém também a lista de authorities (permissões/roles) recuperados do banco.
Ele faz o fluxo chamando a classe authconfig que implementa a user details service.
Ele serve como prova de autenticação do usuário para o restante da aplicação.
O authentication manager precisa ser criado no security config, recebendo um auth configuration como paramento, sendo um @Bean.

### Fluxo de autenticação

Authentication manager -> Authentication configure -> Authentication provider 
-> UsernamePasswordAuthenticationToken -> DaoAuthenticationProvider (classe interna do spring)
-> UserDetailsService -> (nossa classe) AuthConfig.

### JWT e AUTH0

Para usar JWT e AUTH0, é preciso criar uma classe TokenConfig, que será um @Component do spring.
JWT é um token assinado que prova quem é o usuário depois que ele loga na aplicação.
O algoritmo precisa ser passado para a assinatura ser válida, normalmente de Algorithm algorithm = Algorithm.HMAC256(secret).
Na classe tokenConfig, ele é criado a partir das informações do usuário e de uma secret.
A secret normalmente fica como ENV do OS ou fica no .properties do app.
A função/builder usada pra criar, normalmente, vem de com.auth0.jwt.JWT.
Na função, passamos as datas de expiração e criação do token.
Também passado o subject, que é quem criou. Normalmente o e-mail, para validar via comparação depois.
O subject pode ser outra informação única do usuário dependendo da regra de negócio.
Também é passado o claim, uma informação que fica dentro do token. Serve também para identificar e definir permissões.

### Security Filter

Para fazer o token funciona a cada requisição, é preciso configurar no spring via filtro interno.
Classe SecurityFilter que extende a classe OncePerRequestFilter e sobreescreve o método doFilterInternal.
Toda requisição irá passar pelo doFilterInternal.
Dentro desse método fica implementado a lógica para validar o token enviado.
Os dados são pegos do token decoded, da claims e subject, e construído o JWTUserData.
O userData com os dados é passado no UsernamePasswordAuthenticationToken(userData, null).
O token é colocado no authorization via security context holder do spring.
O método chama o doFilter passando request e response para continuar.
O spring se encarrega da exception caso algo de errado.
O securityFilter precisa ser adicionado no filterChain via addFilterBefore, junto com a classe de autenticação (UsernamePasswordAuthenticationFilter).
