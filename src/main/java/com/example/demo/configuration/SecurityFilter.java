package com.example.demo.configuration;

import com.example.demo.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserRepository repository;

    private final String authorizationHeaderString = "Authorization";
    private final String bearerPrefix = "Bearer ";

    public SecurityFilter(TokenService tokenService, UserRepository repository) {
        this.tokenService = tokenService;
        this.repository = repository;
    }

    /**
     * Filtro customizado que intercepta cada requisição HTTP para validar o token JWT.
     * * <p>Este metodo é o núcleo da autenticação Stateless. Ele executa os seguintes passos:</p>
     * <ul>
     * <li><b>Recuperação:</b> Tenta extrair o token do cabeçalho "Authorization" da requisição.</li>
     * <li><b>Validação:</b> Utiliza o {@code TokenService} para verificar a integridade e validade do token.</li>
     * <li><b>Consulta:</b> Se o token for válido, busca os detalhes do usuário no banco de dados através do login extraído.</li>
     * <li><b>Autenticação:</b> Cria um objeto {@link UsernamePasswordAuthenticationToken} preenchido com as
     * permissões (Authorities) do usuário.</li>
     * <li><b>Contexto:</b> Injeta essa autenticação no {@link SecurityContextHolder}, permitindo que o
     * Spring Security autorize o acesso às rotas protegidas.</li>
     * </ul>
     * * <p>Ao final, o metodo chama {@code filterChain.doFilter} para garantir que a requisição
     * continue seu fluxo para o próximo filtro ou para o Controller.</p>
     * * @param request Objeto contendo os dados da requisição HTTP.
     * @param response Objeto para manipular a resposta HTTP.
     * @param filterChain Cadeia de filtros de segurança que a requisição deve percorrer.
     * @throws ServletException Caso ocorra um erro de processamento no Servlet.
     * @throws IOException Caso ocorra um erro de entrada/saída na comunicação.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var token = this.recoverToken(request);
        if (token != null) {
            var login = this.tokenService.validateToken(token);
            repository.findByLogin(login).ifPresent(user -> {
                var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }
        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        var authorizationHeader = request.getHeader(this.authorizationHeaderString);
        if (authorizationHeader == null) return null;
        return authorizationHeader.replace(this.bearerPrefix, "");
    }

}
