package kr.santanrudolph.everyvent.auth.controller;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@Hidden
// 프론트 구현 전 OAuth2 로그인 테스트용 임시 컨트롤러 (토큰 값 보기 위함)
public class OAuth2TestController {

    @GetMapping("/oauth/callback")
    @ResponseBody
    public String oauthCallback(
            @RequestParam(required = false) String accessToken,
            @RequestParam(required = false) String refreshToken) {
        log.info("OAuth2 Callback - AccessToken: {}, RefreshToken: {}", accessToken, refreshToken);

        return """
            <html>
            <head><title>OAuth2 Login Success</title></head>
            <body>
                <h1>카카오 로그인 성공!</h1>
                <h2>Access Token:</h2>
                <p style="word-break: break-all; background: #e8f5e9; padding: 10px; font-size: 12px;">%s</p>
                <h2>Refresh Token:</h2>
                <p style="word-break: break-all; background: #fff3e0; padding: 10px; font-size: 12px;">%s</p>
                <h3>테스트 방법:</h3>
                <p>아래 명령어로 API 테스트하세요:</p>
                <code style="background: #f0f0f0; padding: 10px; display: block;">
                curl -H "Authorization: Bearer %s" http://localhost:8080/api/users/me
                </code>
            </body>
            </html>
            """.formatted(accessToken, refreshToken, accessToken);
    }
}
