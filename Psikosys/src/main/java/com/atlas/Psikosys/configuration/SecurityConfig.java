package com.atlas.Psikosys.configuration;

import com.atlas.Psikosys.service.OAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final OAuth2UserService oAuth2UserService;

    public SecurityConfig(OAuth2UserService oAuth2UserService) {
        this.oAuth2UserService = oAuth2UserService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(registry -> {
                    registry.requestMatchers("/", "/css/**", "/js/**", "/images/**", "/change-language").permitAll();
                    registry.anyRequest().authenticated();
                })
                .csrf(csrf ->
                        csrf.ignoringRequestMatchers("/change-language")
                )
                // Configure OAuth2 login functionality
                .oauth2Login(oauth2 ->
                        // Customize the userInfo endpoint processing
                        oauth2.userInfoEndpoint(userInfo ->
                                // Set our custom OAuth2UserService to handle user information
                                // This service processes user data from the OAuth provider like Google
                                // and maps it to our application's user model
                                userInfo.userService(oAuth2UserService)
                        )
                )
                .formLogin(Customizer.withDefaults());

        return http.build();
    }
}