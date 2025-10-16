package kr.santanrudolph.everyvent.global.config;

import kr.santanrudolph.everyvent.auth.security.JwtAuthenticationFilter;
import kr.santanrudolph.everyvent.auth.service.KakaoOAuthService;
import kr.santanrudolph.everyvent.auth.security.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final KakaoOAuthService kakaoOAuthService;
  private final OAuth2SuccessHandler oAuth2SuccessHandler;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfig corsConfig)
      throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/", "/error", "/favicon.ico").permitAll()
            .requestMatchers("/h2-console/**").permitAll()
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/oauth2/**").permitAll()
            .requestMatchers("/login/**").permitAll()
            .requestMatchers("/oauth/callback", "/oauth/test").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .anyRequest().authenticated())
        .headers(headers -> headers
            .frameOptions(frameOptions -> frameOptions.sameOrigin()))
        .oauth2Login(oauth2 -> oauth2
            .userInfoEndpoint(userInfo -> userInfo
                .userService(kakaoOAuthService))
            .successHandler(oAuth2SuccessHandler))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
