package com.atlas.Psikosys.configuration;

import com.atlas.Psikosys.service.CustomUserDetailsService;
import com.atlas.Psikosys.service.OAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final OAuth2UserService oAuth2UserService;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(OAuth2UserService oAuth2UserService, CustomUserDetailsService userDetailsService) {
        this.oAuth2UserService = oAuth2UserService;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider()) // Bu satır eklendi
                .authorizeHttpRequests(registry -> {
                    registry.requestMatchers("/", "/css/**", "/js/**", "/images/**", "/change-language", "/login", "/register").permitAll();
                    registry.anyRequest().authenticated();
                })
                .csrf(csrf ->
                        csrf.ignoringRequestMatchers(
                                "/change-language",
                                "chat/*/delete",
                                "/profile/language",
                                "/profile/password",
                                "/logout",
                                "/register")
                )
                .oauth2Login(oauth2 ->
                        oauth2.userInfoEndpoint(userInfo ->
                                        userInfo.userService(oAuth2UserService)
                                )
                                .loginPage("/login")
                                .defaultSuccessUrl("/chat", true)
                )
                .formLogin(form ->
                        form.loginPage("/login")
                                .defaultSuccessUrl("/chat", true)
                                .failureUrl("/login?error=true")
                                .usernameParameter("username")
                                .passwordParameter("password")
                                .permitAll()
                )
                .logout(logout ->
                        logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/")
                                .invalidateHttpSession(true)
                                .clearAuthentication(true)
                                .deleteCookies("JSESSIONID")
                                .permitAll()
                );

        return http.build();
    }
}