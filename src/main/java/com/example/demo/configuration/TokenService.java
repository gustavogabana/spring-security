package com.example.demo.configuration;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.demo.domain.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    private final Algorithm algorithm;
    // nome da aplicacao que criou o token
    private final String issuer;

    public TokenService(@Value("${api.security.token.secret}") String secret) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.issuer = "spring-security";
    }

    /**
     * Gera um token JWT para um usuário autenticado.
     * * <p>Este metodo cria um token assinado digitalmente contendo informações básicas (claims)
     * que identificam o usuário nas requisições subsequentes. O processo segue o padrão:
     * <ul>
     * <li><b>Issuer:</b> Identifica a aplicação que emitiu o token (emissor).</li>
     * <li><b>Subject:</b> Define o proprietário do token (geralmente o login ou ID).</li>
     * <li><b>Expiration:</b> Define um tempo de vida para o token para reduzir riscos em caso de interceptação.</li>
     * <li><b>Signature:</b> O token é assinado com um algoritmo secreto, garantindo que não foi alterado.</li>
     * </ul>
     * </p>
     * * @param user A entidade do usuário autenticado que receberá o token.
     * @return Uma String representando o JWT no formato Header.Payload.Signature.
     * @throws RuntimeException Caso ocorra uma falha interna na criação do token.
     */
    public String generateToken(User user) {
        try {
            String token = JWT.create()
                    .withIssuer(issuer)
                    .withSubject(user.getLogin())
                    .withExpiresAt(generateExpirationDate())
                    .sign(algorithm);
            return token;
        } catch (JWTCreationException e) {
            throw new RuntimeException("Error while generating token", e);
        }
    }

    /**
     * Valida a integridade de um token JWT e extrai o identificador do usuário (Subject).
     * * <p>Este metodo é utilizado em cada requisição protegida para verificar se o token
     * enviado pelo cliente é confiável. O processo de validação garante que:</p>
     * <ul>
     * <li>O token foi assinado pela mesma chave secreta da aplicação.</li>
     * <li>O emissor (Issuer) é válido.</li>
     * <li>O token não expirou.</li>
     * <li>O conteúdo do token não foi modificado por terceiros.</li>
     * </ul>
     * * @param token A String do token JWT recebida no cabeçalho Authorization.
     * @return O Subject (login/identificador) do usuário contido no token.
     * @throws RuntimeException Caso o token seja inválido, expirado ou malformado.
     */
    public String validateToken(String token) {
        try {
            return JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException e) {
            return "";
        }
    }

    private Instant generateExpirationDate() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }

}
