package com.konstantion.authentication;

import com.konstantion.exception.ValidationException;
import com.konstantion.expedition.model.LoginTableRequest;
import com.konstantion.expedition.validator.ExpeditionValidator;
import com.konstantion.jwt.JwtService;
import com.konstantion.user.model.LoginUserRequest;
import com.konstantion.user.validator.UserValidator;
import com.konstantion.utils.validator.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class AuthenticationServiceImplTest {
    @Mock
    JwtService jwtService;
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    ExpeditionValidator tableValidator;
    @Mock
    UserValidator userValidator;
    @InjectMocks
    AuthenticationServiceImpl authenticationService;
    LoginTableRequest tableRequest;
    LoginUserRequest userRequest;
    Authentication authentication;

    @BeforeEach
    void setUp() {
        tableRequest = new LoginTableRequest("table");
        userRequest = new LoginUserRequest("user");
        authentication = new UsernamePasswordAuthenticationToken(
                new User("name", "password", Set.of(new SimpleGrantedAuthority("User"))),
                "password"
        );
    }

    @Test
    void shouldThrowValidateExceptionWhenInvalidTableRequest() {
        when(tableValidator.validate(tableRequest)).thenReturn(ValidationResult.invalid(Set.of()));

        assertThatThrownBy(() ->
                authenticationService.authenticate(tableRequest)
        ).isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowValidateExceptionWhenInvalidUserRequest() {
        when(userValidator.validate(userRequest)).thenReturn(ValidationResult.invalid(Set.of()));

        assertThatThrownBy(() ->
                authenticationService.authenticate(userRequest)
        ).isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldGenerateTokenWhenValidUserRequest() {
        when(userValidator.validate(userRequest)).thenReturn(ValidationResult.valid());
        when(jwtService.generateToken(any(), any())).thenReturn("token");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        String token = authenticationService.authenticate(userRequest).token();

        assertThat(token).isEqualTo("token");
        verify(jwtService, times(1)).generateToken(any(), any());
        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    void shouldGenerateTokenWhenValidTableRequest() {
        when(tableValidator.validate(tableRequest)).thenReturn(ValidationResult.valid());
        when(jwtService.generateToken(any(), any())).thenReturn("token");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        String token = authenticationService.authenticate(tableRequest).token();

        assertThat(token).isEqualTo("token");
        verify(jwtService, times(1)).generateToken(any(), any());
        verify(authenticationManager, times(1)).authenticate(any());
    }
}