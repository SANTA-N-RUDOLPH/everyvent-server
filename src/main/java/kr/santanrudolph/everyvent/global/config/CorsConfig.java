package kr.santanrudolph.everyvent.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

  @Value("${frontend.url}")
  private String frontendUrl;

  @Value("${server.url}")
  private String serverUrl;

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // 허용할 출처 (로컬 개발 + 배포 환경)
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:3000",
        "http://localhost:3001",
        "http://localhost:3030",
        frontendUrl,      // 환경별 프론트엔드 URL
        serverUrl   // 배포 서버 URL (Swagger용)
    ));

    // 허용할 HTTP 메서드
    configuration.setAllowedMethods(Arrays.asList(
        "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
    ));

    // 허용할 헤더
    configuration.setAllowedHeaders(Arrays.asList(
        "Authorization",
        "Content-Type",
        "X-Requested-With"
    ));

    // 인증 정보 허용 (쿠키 등)
    configuration.setAllowCredentials(true);

    // preflight 요청 캐시 시간 (초)
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }
}
