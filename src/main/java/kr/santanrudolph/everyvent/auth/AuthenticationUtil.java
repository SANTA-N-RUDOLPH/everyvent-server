package kr.santanrudolph.everyvent.auth;

import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthenticationUtil {

  private AuthenticationUtil() {
    throw new IllegalStateException("Utility class");
  }

  public static Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal()
        .equals("anonymousUser")) {
      throw new EveryventException(ErrorCode.UNAUTHORIZED);
    }

    try {
      return (Long) authentication.getPrincipal();
    } catch (ClassCastException e) {
      throw new EveryventException(ErrorCode.INVALID_TOKEN);
    }
  }

  public static boolean isAuthenticated() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null && authentication.isAuthenticated()
        && !authentication.getPrincipal().equals("anonymousUser");
  }
}