package kr.santanrudolph.everyvent.global.config;

import kr.santanrudolph.everyvent.auth.security.JwtAuthenticationFilter;
import kr.santanrudolph.everyvent.auth.service.KakaoOAuthService;
import kr.santanrudolph.everyvent.auth.security.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final KakaoOAuthService kakaoOAuthService;
  private final OAuth2SuccessHandler oAuth2SuccessHandler;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final Environment environment;

  @Value("${spring.security.user.name}")
  private String swaggerUsername;

  @Value("${spring.security.user.password}")
  private String swaggerPassword;

  @Order(1)
  @Bean
  public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
    http
        .securityMatcher("/swagger-ui/**", "/v3/api-docs/**")
        .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
        .httpBasic(httpBasic -> httpBasic.realmName("Swagger UI"))
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    return http.build();
  }

  @Bean
  public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http, CorsConfig corsConfig)
      throws Exception {

    http
        .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        .authorizeHttpRequests(auth -> {

          auth.requestMatchers(
              "/", "/error", "/favicon.ico",
              "/h2-console/**",
              "/api/auth/**",
              "/oauth2/**", "/login/**",
              "/oauth/callback", "/oauth/test"
          ).permitAll();

          if (environment.acceptsProfiles("dev", "local")) {
            auth.requestMatchers("/api/dev/**").permitAll();
          }

          // 나머지 API는 JWT 인증 필요
          auth.anyRequest().authenticated();
        })

        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

        .headers(headers -> headers
            .frameOptions(frame -> frame.sameOrigin()))

        .oauth2Login(oauth2 -> oauth2
            .userInfoEndpoint(info -> info.userService(kakaoOAuthService))
            .successHandler(oAuth2SuccessHandler));

    return http.build();
  }

  @Order(2)
  @Bean
  public UserDetailsService userDetailsService() {
    return new InMemoryUserDetailsManager(
        User.builder()
            .username(swaggerUsername)
            .password(passwordEncoder().encode(swaggerPassword))
            .roles("SWAGGER")
            .build()
    );
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
