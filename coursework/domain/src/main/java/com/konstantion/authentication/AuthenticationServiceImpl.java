package com.konstantion.authentication;

import com.konstantion.authentication.model.AuthenticationResponse;
import com.konstantion.expedition.authentication.TableAuthenticationToken;
import com.konstantion.expedition.model.LoginTableRequest;
import com.konstantion.expedition.validator.ExpeditionValidator;
import com.konstantion.jwt.JwtService;
import com.konstantion.user.authentication.UserAuthenticationToken;
import com.konstantion.user.model.LoginUserRequest;
import com.konstantion.user.validator.UserValidator;
import com.konstantion.utils.validator.ValidationResult;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.konstantion.utils.EntityNameConstants.ENTITY;
import static com.konstantion.utils.EntityNameConstants.TABLE;
import static com.konstantion.utils.EntityNameConstants.USER;

@Component
public record AuthenticationServiceImpl(
        JwtService jwtService,
        AuthenticationManager authenticationManager,
        ExpeditionValidator tableValidator,
        UserValidator userValidator
) implements AuthenticationService {
    @Override
    public AuthenticationResponse authenticate(LoginTableRequest request) {
        ValidationResult validationResult = tableValidator.validate(request);
        validationResult.validOrTrow();

        TableAuthenticationToken authenticationToken = new TableAuthenticationToken(
                null,
                request.password()
        );

        UserDetails tableDetails = (UserDetails) authenticationManager
                .authenticate(authenticationToken)
                .getPrincipal();

        Map<String, Object> extraClaim = Map.of(ENTITY, TABLE);

        String token = jwtService.generateToken(extraClaim, tableDetails);

        return new AuthenticationResponse(token, tableDetails, TABLE);
    }

    @Override
    public AuthenticationResponse authenticate(LoginUserRequest request) {
        ValidationResult validationResult = userValidator.validate(request);
        validationResult.validOrTrow();

        UserAuthenticationToken authenticationToken = new UserAuthenticationToken(
                null,
                request.password()
        );

        UserDetails userDetails = (UserDetails) authenticationManager
                .authenticate(authenticationToken)
                .getPrincipal();

        Map<String, Object> extraClaim = Map.of(ENTITY, USER);

        String token = jwtService.generateToken(extraClaim, userDetails);

        return new AuthenticationResponse(token, userDetails, USER);
    }
}
