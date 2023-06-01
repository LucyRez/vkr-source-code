package cs.hse.skliforganizationmanagement.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Configuration
public class SecurityConfig {

    @Value("${jwksUri}")
    private String jwksUri;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.cors(Customizer.withDefaults())
        .oauth2ResourceServer(
                r -> r.jwt().jwkSetUri(jwksUri)
                        .jwtAuthenticationConverter(new CustomJWTTokenAuthenticationConverter())
        )
        .authorizeHttpRequests()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/register/confirm").permitAll()
                .requestMatchers("/api/register/confirm_updated").permitAll()
                .requestMatchers("/api/register/organization").hasAuthority("ADMIN_GLOBAL")
                .requestMatchers("/api/register/user").hasAnyAuthority("ADMIN_GLOBAL", "ADMIN_LOCAL")
                .requestMatchers("api/organizations").hasAuthority("ADMIN_GLOBAL")
                .requestMatchers("/api/user/edit_role").hasAnyAuthority("ADMIN_GLOBAL", "ADMIN_LOCAL")
                .anyRequest().authenticated().and()
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return encoder;
    }

}
