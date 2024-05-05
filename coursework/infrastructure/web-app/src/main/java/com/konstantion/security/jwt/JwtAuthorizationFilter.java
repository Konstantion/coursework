package com.konstantion.security.jwt;

import com.konstantion.expedition.ExpeditionService;
import com.konstantion.expedition.authentication.TableAuthenticationToken;
import com.konstantion.jwt.JwtService;
import com.konstantion.security.jwt.config.JwtConfig;
import com.konstantion.user.UserService;
import com.konstantion.user.authentication.UserAuthenticationToken;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

import static com.konstantion.utils.EntityNameConstants.ENTITY;
import static com.konstantion.utils.EntityNameConstants.TABLE;
import static com.konstantion.utils.EntityNameConstants.USER;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    private static final String BEARER_TOKEN_NOT_FOUND_MESSAGE =
            "Bearer token not found";
    private static final Function<Claims, String> EXTRACT_ENTITY_FUNCTION =
            claim -> claim.getOrDefault(ENTITY, USER).toString();
    private static final Runnable EXPIRED_OR_INVALID_JWT_TOKEN_ACTION = () -> {
        throw new BadCredentialsException("Expired or invalid JWT token");
    };
    private final JwtConfig jwtConfig;
    private final JwtService jwtService;
    private final UserService userService;
    private final ExpeditionService expeditionService;

    public JwtAuthorizationFilter(JwtConfig jwtConfig, JwtService jwtService, UserService userService, ExpeditionService expeditionService) {
        this.jwtConfig = jwtConfig;
        this.jwtService = jwtService;
        this.userService = userService;
        this.expeditionService = expeditionService;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = resolveToken(request);

        if (isNull(token)) {
            logger.warn(BEARER_TOKEN_NOT_FOUND_MESSAGE);
            filterChain.doFilter(request, response);
            return;
        }

        String username = jwtService.extractUsername(token);
        String type = jwtService.extractClaim(token, EXTRACT_ENTITY_FUNCTION);

        if (enableToAuthorize(username)) {
            try {
                getUserDetails(username, type)
                        .filter(userDetails -> jwtService.isTokenValid(token, userDetails))
                        .map(userDetails -> createAuthentication(userDetails, type))
                        .map(authentication -> extractAndSetDetails(authentication, request))
                        .ifPresentOrElse(this::authenticate, EXPIRED_OR_INVALID_JWT_TOKEN_ACTION);
            } catch (AuthenticationException e) {
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        final String authHeader = request.getHeader(jwtConfig.getAuthorizationHeader());
        if (isNull(authHeader) || !authHeader.startsWith(jwtConfig.getTokenPrefix())) {
            return null;
        }
        return authHeader.substring(jwtConfig.getTokenPrefix().length());
    }

    private boolean enableToAuthorize(String username) {
        return nonNull(username)
                && isNull(SecurityContextHolder
                .getContext()
                .getAuthentication());
    }

    private Optional<UserDetails> getUserDetails(String username, String type) {
        return switch (type) {
            case USER -> Optional.ofNullable(userService.loadUserByUsername(username));
            case TABLE -> Optional.ofNullable(expeditionService.loadUserByUsername(username));
            default -> Optional.empty();
        };
    }

    private UsernamePasswordAuthenticationToken createAuthentication(UserDetails userDetails, String type) {
        return switch (type) {
            case TABLE -> new TableAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            case USER -> new UserAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            default -> new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
        };
    }

    private UsernamePasswordAuthenticationToken extractAndSetDetails(UsernamePasswordAuthenticationToken authenticationToken, HttpServletRequest request) {
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authenticationToken;
    }

    private void authenticate(UsernamePasswordAuthenticationToken authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
