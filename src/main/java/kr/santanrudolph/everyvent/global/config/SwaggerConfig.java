package kr.santanrudolph.everyvent.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    String jwtSchemeName = "JWT Token";
    String basicAuthSchemeName = "Basic Auth";

    SecurityRequirement jwtSecurityRequirement = new SecurityRequirement()
        .addList(jwtSchemeName);

    SecurityRequirement basicAuthSecurityRequirement = new SecurityRequirement()
        .addList(basicAuthSchemeName);

    Components components = new Components()
        .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
            .name(jwtSchemeName)
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .description("JWT 토큰을 입력하세요. 'Bearer ' 접두사는 자동으로 추가됩니다."))

        .addSecuritySchemes(basicAuthSchemeName, new SecurityScheme()
            .name(basicAuthSchemeName)
            .type(SecurityScheme.Type.HTTP)
            .scheme("basic")
            .description("Swagger UI 접근을 위한 Basic Authentication"));

    return new OpenAPI()
        .info(new Info()
            .title("Everyvent API")
            .description("Everyvent 서버 API 문서")
            .version("1.0.0"))
        .servers(List.of(
            new Server()
                .url("https://api.everyvent.cloud")
                .description("배포 서버"),
            new Server()
                .url("http://localhost:8080")
                .description("로컬 개발 서버")
        ))
        .addSecurityItem(jwtSecurityRequirement)
        .addSecurityItem(basicAuthSecurityRequirement)
        .components(components);
  }
}
