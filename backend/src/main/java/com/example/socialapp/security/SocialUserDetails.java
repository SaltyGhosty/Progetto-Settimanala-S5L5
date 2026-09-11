package com.example.socialapp.security;

import com.example.socialapp.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** Incapsula l'entità User così i controller possono recuperarla dal contesto di sicurezza. */
@Getter
public class SocialUserDetails implements UserDetails {

    private final User domainUser;

    public SocialUserDetails(User domainUser) {
        this.domainUser = domainUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return domainUser.getPassword();
    }

    @Override
    public String getUsername() {
        return domainUser.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
