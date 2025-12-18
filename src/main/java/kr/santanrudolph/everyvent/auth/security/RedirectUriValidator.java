package kr.santanrudolph.everyvent.auth.security;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConfigurationProperties(prefix = "oauth2.redirect")
@Getter
@Setter
public class RedirectUriValidator {

  private static final String DEFAULT_REDIRECT_URI = "http://localhost:5173";

  private List<String> allowedUris;

  public boolean isAllowed(String redirectUri) {
    if (!isPresent(redirectUri)) {
      return false;
    }

    URI uri = parseUri(redirectUri);
    if (uri == null) {
      return false;
    }

    if (!hasValidHttpScheme(uri)) {
      return false;
    }

    if (!isWhitelisted(redirectUri)) {
      log.warn("Redirect URI not in whitelist: {}", redirectUri);
      return false;
    }

    return true;
  }


  public String getDefaultRedirectUri() {
    log.info("Using default redirect URI: {}", DEFAULT_REDIRECT_URI);
    return DEFAULT_REDIRECT_URI;
  }

  private boolean isPresent(String redirectUri) {
    if (redirectUri == null || redirectUri.isBlank()) {
      log.warn("Redirect URI is null or blank");
      return false;
    }
    return true;
  }

  private URI parseUri(String redirectUri) {
    try {
      return new URI(redirectUri);
    } catch (URISyntaxException e) {
      log.warn("Malformed redirect URI: {}", redirectUri, e);
      return null;
    }
  }

  private boolean hasValidHttpScheme(URI uri) {
    String scheme = uri.getScheme();

    if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
      log.warn("Invalid scheme in redirect URI: {}", scheme);
      return false;
    }
    return true;
  }

  private boolean isWhitelisted(String redirectUri) {
    String normalizedUrl = normalizeUrl(redirectUri);

    return allowedUris.stream()
        .map(this::normalizeUrl)
        .anyMatch(normalizedUrl::equals);
  }

  private String normalizeUrl(String url) {
    try {
      URI uri = new URI(url);

      String scheme = uri.getScheme().toLowerCase();
      String host = uri.getHost().toLowerCase();
      int port = normalizePort(scheme, uri.getPort());
      String path = normalizePath(uri.getPath());

      StringBuilder normalizedUrl = new StringBuilder();
      normalizedUrl.append(scheme).append("://").append(host);

      if (port != -1) {
        normalizedUrl.append(":").append(port);
      }

      if (path != null && !path.isEmpty()) {
        normalizedUrl.append(path);
      }

      return normalizedUrl.toString();

    } catch (URISyntaxException e) {
      log.warn("Failed to normalize URL: {}", url, e);
      return url;
    }
  }

  private int normalizePort(String scheme, int port) {
    if ((scheme.equals("http") && port == 80) ||
        (scheme.equals("https") && port == 443)) {
      return -1;
    }
    return port;
  }

  private String normalizePath(String path) {
    if (path != null && path.endsWith("/")) {
      return path.substring(0, path.length() - 1);
    }
    return path;
  }
}

