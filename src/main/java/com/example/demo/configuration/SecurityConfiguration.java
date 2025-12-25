package com.example.demo.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final SecurityFilter securityFilter;

    public SecurityConfiguration(SecurityFilter securityFilter) {
        this.securityFilter = securityFilter;
    }

    /**
     * Middleware do spring para proteger rotas:
     * Configura a cadeia de filtros de segurança (Security Filter Chain) da aplicação.
     * * <p>Este metodo é um Bean de configuração que define a estratégia global de segurança,
     * substituindo as configurações padrão do Spring Security. Ele utiliza o objeto
     * {@link HttpSecurity} para construir uma pilha ordenada de filtros que serão
     * aplicados a cada requisição HTTP.</p>
     * * <p>O funcionamento baseia-se em:</p>
     * <ul>
     * <li><b>Autorização:</b> Define quais rotas são públicas (permitAll) e quais exigem roles específicas.</li>
     * <li><b>Cadeia de Filtros:</b> Retorna um {@link SecurityFilterChain}, que é uma lista de filtros
     * interceptores (ex: filtros de autenticação, CSRF, sessão) processados em sequência.</li>
     * <li><b>Contexto de Segurança:</b> Garante que as credenciais e permissões (Authorities)
     * sejam validadas antes da requisição atingir os Controllers.</li>
     * </ul>
     *
     * @param http O objeto builder utilizado para configurar permissões, sessões e proteções de segurança.
     * @return A configuração da cadeia de filtros finalizada e pronta para ser injetada pelo Spring.
     * @throws Exception Caso ocorra algum erro durante a construção da configuração de segurança.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll() garante que as páginas de erro sejam acessadas
        // sem auth, mesmo que o erro tenha ocorrido em rotas protegidas
        // não faz sentido em apps stateless com controllerAdvice tratando erros e front controlando os erros

        // .authorizeHttpRequests define a politica de acesso por rota e permissão do usuário fazendo a request
        // .requestMatchers é a ponte usada pela classe AuthorizationManagerRequestMatcherRegistry authorize
        // para saber e filtrar quais verbos e rotas aplicar as politicas para permitir ou bloquear

        //.addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
        // serve para adicionar o filtro JWT (securityFilter) antes do UsernamePasswordAuthenticationFilter ser chamado pelo spring

        return http
                .csrf(AbstractHttpConfigurer::disable) // CSRF desativado para configuração stateless
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/product").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Expõe o gerenciador de autenticação central (AuthenticationManager) como um Bean.
     * * <p>O {@link AuthenticationManager} é o componente do Spring Security responsável por
     * processar os pedidos de autenticação. Ele é essencial em fluxos de login customizados,
     * onde a autenticação é disparada manualmente no Controller (ex: APIs REST com JWT).</p>
     * * <p><b>Funcionamento Interno:</b></p>
     * <ul>
     * <li>Recebe um objeto de autenticação (geralmente {@link org.springframework.security.authentication.UsernamePasswordAuthenticationToken}).</li>
     * <li>Utiliza o {@link org.springframework.security.core.userdetails.UserDetailsService} para localizar o usuário no banco de dados.</li>
     * <li>Utiliza o {@link org.springframework.security.crypto.password.PasswordEncoder} para validar se a senha enviada coincide com o hash armazenado.</li>
     * <li>Retorna um objeto de autenticação totalmente preenchido (incluindo as permissões/authorities) caso as credenciais sejam válidas.</li>
     * </ul>
     * * @param authenticationConfiguration Objeto de configuração do Spring que mantém as definições de segurança do projeto.
     * @return A instância configurada do AuthenticationManager.
     * @throws Exception Caso ocorra algum erro ao recuperar o gerenciador de autenticação.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Define o algoritmo de hashing para o armazenamento e validação de senhas.
     * * <p>O {@link PasswordEncoder} é utilizado pelo Spring Security para garantir que as senhas
     * nunca sejam armazenadas em texto plano (plain text) no banco de dados, atendendo às
     * melhores práticas de segurança (OWASP).</p>
     * * <p><b>Funcionamento do BCrypt:</b></p>
     * <ul>
     * <li><b>Hashing Seguro:</b> O BCrypt utiliza uma função de derivação de chave baseada no cifrador Blowfish.</li>
     * <li><b>Salt Automático:</b> Gera um "salt" (tempero) aleatório para cada senha, garantindo que
     * dois usuários com a mesma senha tenham hashes diferentes no banco, prevenindo ataques de Rainbow Tables.</li>
     * <li><b>Custo Adaptativo:</b> É um algoritmo deliberadamente lento, o que dificulta ataques de força bruta.</li>
     * </ul>
     * * <p>Este Bean é consultado automaticamente pelo {@link AuthenticationManager} durante o
     * processo de autenticação para comparar a senha enviada pelo usuário com o hash salvo.</p>
     * * @return Uma instância de {@link BCryptPasswordEncoder} para criptografia forte.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
