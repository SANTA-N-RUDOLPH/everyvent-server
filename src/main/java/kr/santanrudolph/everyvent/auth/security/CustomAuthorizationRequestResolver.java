package kr.santanrudolph.everyvent.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

  private static final String REDIRECT_URI_SESSION_KEY = "OAUTH2_REDIRECT_URI";

  private final OAuth2AuthorizationRequestResolver defaultResolver;
  private final RedirectUriValidator redirectUriValidator;


  public CustomAuthorizationRequestResolver(
      ClientRegistrationRepository clientRegistrationRepository,
      RedirectUriValidator redirectUriValidator) {
    this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
        clientRegistrationRepository,
        "/oauth2/authorization"
    );
    this.redirectUriValidator = redirectUriValidator;
  }


  // socialProvider 정보가 없을 때 oauth 처리하는 메서드
  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
    OAuth2AuthorizationRequest authRequest = defaultResolver.resolve(request);
    if (authRequest != null) {
      storeRedirectUriIfValid(request);
    }
    return authRequest;
  }

  // // socialProvider가 확정된 상태에서 OAuth 인가 요청을 처리
  @Override
  public OAuth2AuthorizationRequest resolve(
      HttpServletRequest request,
      String clientRegistrationId) {

    OAuth2AuthorizationRequest authRequest =
        defaultResolver.resolve(request, clientRegistrationId);

    if (authRequest != null) {
      storeRedirectUriIfValid(request);
    }
    return authRequest;
  }

  private void storeRedirectUriIfValid(HttpServletRequest request) {
    String redirectUri = request.getParameter("redirect_uri");
    String uriToStore;

    if (redirectUri != null && redirectUriValidator.isAllowed(redirectUri)) {
      uriToStore = redirectUri;
    } else {
      uriToStore = redirectUriValidator.getDefaultRedirectUri();
      if (redirectUri != null) {
        log.warn("Invalid redirect_uri rejected: {}, using default", redirectUri);
      }
    }

    request.getSession().setAttribute(REDIRECT_URI_SESSION_KEY, uriToStore);
  }
}
