package com.konstantion.expedition.authentication;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class TableAuthenticationToken extends UsernamePasswordAuthenticationToken {
    public TableAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }

    public TableAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }
}
