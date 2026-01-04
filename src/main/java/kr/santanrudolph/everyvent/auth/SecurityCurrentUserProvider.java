package kr.santanrudolph.everyvent.auth;


import org.springframework.stereotype.Component;

@Component
public class SecurityCurrentUserProvider implements CurrentUserProvider {

  @Override
  public Long getCurrentUserId() {
    return AuthenticationUtil.getCurrentUserId();
  }

}
