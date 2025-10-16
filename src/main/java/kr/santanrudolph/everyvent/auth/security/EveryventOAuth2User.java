package kr.santanrudolph.everyvent.auth.security;

import kr.santanrudolph.everyvent.domain.user.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class EveryventOAuth2User implements OAuth2User {

    private final User user;

    private final Map<String, Object> attributes;

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return user.getSocialId();
    }

    public Long getUserId() {
        return user.getId();
    }
}
