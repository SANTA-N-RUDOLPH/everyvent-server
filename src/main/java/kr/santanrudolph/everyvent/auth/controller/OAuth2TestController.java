package kr.santanrudolph.everyvent.auth.controller;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

// TODO: 프론트엔드 연동 완료 후 삭제 예정 (OAuth2 로그인 테스트용 임시 컨트롤러)
@Slf4j
@Profile({"local", "dev"})
@Controller
@Hidden
public class OAuth2TestController {

    @GetMapping("/oauth/test")
    @ResponseBody
    public String oauthTestPage() {
        return """
            <html>
            <head>
                <title>OAuth2 로그인 테스트</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        padding: 40px;
                        text-align: center;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        min-height: 100vh;
                        display: flex;
                        flex-direction: column;
                        justify-content: center;
                        align-items: center;
                    }
                    .container {
                        background: rgba(255, 255, 255, 0.95);
                        padding: 40px;
                        border-radius: 10px;
                        box-shadow: 0 10px 25px rgba(0,0,0,0.2);
                        color: #333;
                        max-width: 500px;
                    }
                    h1 { margin-bottom: 10px; color: #667eea; }
                    p { color: #666; margin-bottom: 30px; }
                    .login-button {
                        display: inline-block;
                        background: #FEE500;
                        color: #000000;
                        padding: 15px 30px;
                        border-radius: 8px;
                        text-decoration: none;
                        font-weight: bold;
                        font-size: 16px;
                        margin: 10px;
                        transition: all 0.3s;
                    }
                    .login-button:hover {
                        background: #FDD835;
                        transform: translateY(-2px);
                        box-shadow: 0 5px 15px rgba(0,0,0,0.2);
                    }
                    .info {
                        margin-top: 30px;
                        padding: 20px;
                        background: #f5f5f5;
                        border-radius: 8px;
                        font-size: 14px;
                        text-align: left;
                    }
                    .info h3 { color: #667eea; margin-top: 0; }
                    code {
                        background: #e0e0e0;
                        padding: 2px 6px;
                        border-radius: 3px;
                        font-size: 13px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>🎄 EveryVent OAuth2 테스트</h1>
                    <p>카카오 계정으로 로그인하여 테스트하세요</p>

                    <a href="/oauth2/authorization/kakao?prompt=login" class="login-button">
                        카카오 로그인 시작
                    </a>

                    <div class="info">
                        <h3>📝 테스트 흐름</h3>
                        <ol style="padding-left: 20px;">
                            <li>카카오 로그인 버튼 클릭</li>
                            <li>카카오 계정으로 로그인</li>
                            <li>토큰 정보 확인 페이지로 이동</li>
                            <li>내 정보 조회 / 로그아웃 테스트</li>
                        </ol>
                        <p style="margin-top: 15px; color: #666;">
                            <strong>직접 접속:</strong><br>
                            <code>http://localhost:8080/oauth/test</code>
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """;
    }

    @GetMapping("/oauth/callback")
    @ResponseBody
    public String oauthCallback(
            @RequestParam(required = false) String accessToken,
            @RequestParam(required = false) String refreshToken) {
        log.info("OAuth2 Callback - AccessToken: {}, RefreshToken: {}", accessToken, refreshToken);

        return """
            <html>
            <head>
                <title>OAuth2 Login Success</title>
                <style>
                    body { font-family: Arial, sans-serif; padding: 20px; }
                    .token-box { word-break: break-all; padding: 10px; font-size: 12px; margin: 10px 0; }
                    .access-token { background: #e8f5e9; }
                    .refresh-token { background: #fff3e0; }
                    .token-info { font-size: 11px; color: #666; margin-top: 5px; }
                    .expired { color: #f44336; font-weight: bold; }
                    .button { background: #f44336; color: white; padding: 10px 20px; border: none; border-radius: 4px; cursor: pointer; margin: 10px 5px 10px 0; }
                    .button:hover { background: #d32f2f; }
                    .button.secondary { background: #2196F3; }
                    .button.secondary:hover { background: #1976D2; }
                    .button.warning { background: #FF9800; }
                    .button.warning:hover { background: #F57C00; }
                    code { background: #f0f0f0; padding: 10px; display: block; margin: 10px 0; }
                    #result { margin-top: 20px; padding: 10px; display: none; }
                    .success { background: #c8e6c9; border: 1px solid #4caf50; }
                    .error { background: #ffcdd2; border: 1px solid #f44336; }
                </style>
            </head>
            <body>
                <h1>카카오 로그인 성공!</h1>
                <h2>Access Token:</h2>
                <p class="token-box access-token" id="accessToken">%s</p>
                <div class="token-info" id="accessTokenExpiry"></div>

                <h2>Refresh Token:</h2>
                <p class="token-box refresh-token" id="refreshToken">%s</p>
                <div class="token-info" id="refreshTokenExpiry"></div>

                <h3>테스트 기능:</h3>
                <button class="button secondary" onclick="testMe()">내 정보 조회</button>
                <button class="button warning" onclick="refreshTokens()">토큰 재발급</button>
                <button class="button" onclick="logout()">로그아웃</button>

                <div id="result"></div>

                <h3>cURL 명령어:</h3>
                <code>
                # 내 정보 조회
                curl -H "Authorization: Bearer %s" http://localhost:8080/api/users/me

                # 토큰 재발급
                curl -X POST http://localhost:8080/api/auth/refresh \\
                  -H "Content-Type: application/json" \\
                  -d '{"refreshToken": "%s"}'

                # 로그아웃
                curl -X POST -H "Authorization: Bearer %s" http://localhost:8080/api/auth/logout
                </code>

                <script>
                    // JWT 디코딩 함수
                    function parseJwt(token) {
                        try {
                            const base64Url = token.split('.')[1];
                            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
                            const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => {
                                return '%%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
                            }).join(''));
                            return JSON.parse(jsonPayload);
                        } catch (e) {
                            console.error('JWT 디코딩 실패:', e);
                            return null;
                        }
                    }

                    let accessToken = document.getElementById('accessToken').textContent.trim();
                    let refreshToken = document.getElementById('refreshToken').textContent.trim();

                    // JWT에서 만료 시간 추출 (exp는 초 단위)
                    const accessPayload = parseJwt(accessToken);
                    const refreshPayload = parseJwt(refreshToken);

                    let accessTokenExpiresAt = accessPayload ? accessPayload.exp * 1000 : 0;
                    let refreshTokenExpiresAt = refreshPayload ? refreshPayload.exp * 1000 : 0;

                    function updateTokenExpiry() {
                        const now = Date.now();

                        const accessRemaining = Math.max(0, accessTokenExpiresAt - now);
                        const refreshRemaining = Math.max(0, refreshTokenExpiresAt - now);

                        const accessMinutes = Math.floor(accessRemaining / 60000);
                        const accessSeconds = Math.floor((accessRemaining %% 60000) / 1000);

                        const refreshMinutes = Math.floor(refreshRemaining / 60000);
                        const refreshSeconds = Math.floor((refreshRemaining %% 60000) / 1000);

                        const accessExpiry = document.getElementById('accessTokenExpiry');
                        const refreshExpiry = document.getElementById('refreshTokenExpiry');

                        if (accessRemaining > 0) {
                            accessExpiry.innerHTML = `만료까지: ${accessMinutes}분 ${accessSeconds}초 남음`;
                            accessExpiry.className = 'token-info';
                        } else {
                            accessExpiry.innerHTML = '⚠️ 만료됨';
                            accessExpiry.className = 'token-info expired';
                        }

                        if (refreshRemaining > 0) {
                            refreshExpiry.innerHTML = `만료까지: ${refreshMinutes}분 ${refreshSeconds}초 남음`;
                            refreshExpiry.className = 'token-info';
                        } else {
                            refreshExpiry.innerHTML = '⚠️ 만료됨';
                            refreshExpiry.className = 'token-info expired';
                        }
                    }

                    setInterval(updateTokenExpiry, 1000);
                    updateTokenExpiry();

                    async function testMe() {
                        const result = document.getElementById('result');
                        result.style.display = 'block';
                        result.textContent = '요청 중...';
                        result.className = '';

                        try {
                            const response = await fetch('http://localhost:8080/api/users/me', {
                                headers: { 'Authorization': 'Bearer ' + accessToken }
                            });
                            const data = await response.json();
                            result.className = 'success';
                            result.textContent = '성공: ' + JSON.stringify(data, null, 2);
                        } catch (error) {
                            result.className = 'error';
                            result.textContent = '에러: ' + error.message;
                        }
                    }

                    async function refreshTokens() {
                        const result = document.getElementById('result');
                        result.style.display = 'block';
                        result.textContent = '토큰 재발급 중...';
                        result.className = '';

                        try {
                            const response = await fetch('http://localhost:8080/api/auth/refresh', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify({ refreshToken: refreshToken })
                            });

                            if (response.ok) {
                                const data = await response.json();

                                // 토큰 업데이트
                                accessToken = data.accessToken;
                                refreshToken = data.refreshToken;

                                // JWT에서 만료 시간 추출
                                const newAccessPayload = parseJwt(data.accessToken);
                                const newRefreshPayload = parseJwt(data.refreshToken);

                                accessTokenExpiresAt = newAccessPayload ? newAccessPayload.exp * 1000 : 0;
                                refreshTokenExpiresAt = newRefreshPayload ? newRefreshPayload.exp * 1000 : 0;

                                // UI 업데이트
                                document.getElementById('accessToken').textContent = data.accessToken;
                                document.getElementById('refreshToken').textContent = data.refreshToken;

                                result.className = 'success';
                                result.textContent = '토큰 재발급 성공!\\n새로운 Access Token과 Refresh Token이 발급되었습니다.';

                                updateTokenExpiry();
                            } else {
                                const errorData = await response.json();
                                result.className = 'error';
                                result.textContent = '재발급 실패: ' + JSON.stringify(errorData, null, 2);
                            }
                        } catch (error) {
                            result.className = 'error';
                            result.textContent = '에러: ' + error.message;
                        }
                    }

                    async function logout() {
                        if (!confirm('로그아웃 하시겠습니까?')) return;

                        const result = document.getElementById('result');
                        result.style.display = 'block';
                        result.textContent = '로그아웃 중...';
                        result.className = '';

                        try {
                            const response = await fetch('http://localhost:8080/api/auth/logout', {
                                method: 'POST',
                                headers: { 'Authorization': 'Bearer ' + accessToken }
                            });

                            if (response.ok) {
                                result.className = 'success';
                                result.textContent = '로그아웃 성공! 로그인 페이지로 이동합니다...';
                                setTimeout(() => {
                                    window.location.href = '/oauth/test';
                                }, 2000);
                            } else {
                                const errorText = await response.text();
                                result.className = 'error';
                                result.textContent = '로그아웃 실패: ' + errorText;
                            }
                        } catch (error) {
                            result.className = 'error';
                            result.textContent = '에러: ' + error.message;
                        }
                    }
                </script>
            </body>
            </html>
            """.formatted(accessToken, refreshToken, accessToken, refreshToken, accessToken);
    }
}
