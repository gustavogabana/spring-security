package com.example.demo.controller;

import com.example.demo.config.TokenConfig;
import com.example.demo.dto.request.LoginRequestDTO;
import com.example.demo.dto.request.RegisterUserRequestDTO;
import com.example.demo.dto.response.LoginResponseDTO;
import com.example.demo.dto.response.RegisterUserResponseDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenConfig tokenConfig;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            TokenConfig tokenConfig
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenConfig = tokenConfig;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO requestDTO) {
        UsernamePasswordAuthenticationToken userAndPass = new UsernamePasswordAuthenticationToken(requestDTO.email(), requestDTO.password());
        Authentication authenticated = authenticationManager.authenticate(userAndPass);
        User user = (User) authenticated.getPrincipal();
        if (user == null) throw new IllegalStateException("Erro de autenticacao");
        String token = tokenConfig.generateToken(user);
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterUserResponseDTO> register(@Valid @RequestBody RegisterUserRequestDTO requestDTO) {
        User newUser = new User();
        newUser.setEmail(requestDTO.email());
        newUser.setName(requestDTO.name());
        newUser.setPassword(passwordEncoder.encode(requestDTO.password()));

        userRepository.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterUserResponseDTO(newUser.getName(), newUser.getEmail()));
    }

}
