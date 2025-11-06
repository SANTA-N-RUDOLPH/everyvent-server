package kr.santanrudolph.everyvent.auth;

import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public class AuthenticationUtil {

  private static final String ANONYMOUS_USER = "anonymousUser";

  private AuthenticationUtil() {
  }

  public static Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null) {
      throw new EveryventException(ErrorCode.UNAUTHORIZED, "인증 정보가 null 입니다.");
    }

    Object principal = authentication.getPrincipal();
    if (!authentication.isAuthenticated() || ANONYMOUS_USER.equals(principal)) {
      throw new EveryventException(ErrorCode.UNAUTHORIZED, "인증되지 않은 사용자입니다.");
    }

    try {
      return (Long) authentication.getPrincipal();
    } catch (ClassCastException e) {
      throw new EveryventException(
          ErrorCode.INTERNAL_SERVER_ERROR,
          "인증 정보의 Principal이 예상 타입(Long)이 아닙니다. 현재 타입: "
              + authentication.getPrincipal().getClass().getSimpleName()
      );
    }
  }

  public static boolean isAuthenticated() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return false;
    }

    Object principal = authentication.getPrincipal();
    return principal != null && !ANONYMOUS_USER.equals(principal);

  }
}
