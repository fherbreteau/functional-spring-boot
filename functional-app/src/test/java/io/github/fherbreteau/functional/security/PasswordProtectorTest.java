package io.github.fherbreteau.functional.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;

import io.github.fherbreteau.functional.driven.PasswordProtector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PasswordProtectorTest {
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PasswordValidator passwordValidator;
    @Captor
    private ArgumentCaptor<PasswordData> dataCaptor;

    private PasswordProtector passwordProtector;

    @BeforeEach
    void setup() {
        passwordProtector = new PasswordProtectorImpl(passwordEncoder, passwordValidator);
    }

    @Test
    void passwordProtectorShouldDelegateToPasswordEncoderWhenProtecting() {
        // GIVEN
        given(passwordEncoder.encode(anyString())).willAnswer(invocation -> invocation.getArgument(0));
        // WHEN
        assertThat(passwordProtector.protect("password")).isEqualTo("password");
        // THEN
        then(passwordEncoder).should().encode("password");
    }

    @Test
    void passwordProtectorShouldDelegateToPasswordValidatorWhenValidating() {
        // GIVEN
        given(passwordValidator.validate(any())).willReturn(new RuleResult());
        // WHEN
        assertThat(passwordProtector.validate("password")).isEmpty();
        // THEN
        then(passwordValidator).should().validate(dataCaptor.capture());
        assertThat(dataCaptor.getValue())
                .extracting(PasswordData::getPassword)
                .isEqualTo("password");
    }

    @Test
    void passwordProtectorShouldReturnValidationErrorsWhenValidationFails() {
        // GIVEN
        given(passwordValidator.validate(any())).willReturn(new RuleResult(false));
        given(passwordValidator.getMessages(any())).willReturn(List.of("error1", "error2"));
        // WHEN
        assertThat(passwordProtector.validate("password"))
                .hasSize(2)
                .containsExactly("error1", "error2");
        // THEN
        then(passwordValidator).should().validate(dataCaptor.capture());
        assertThat(dataCaptor.getValue())
                .extracting(PasswordData::getPassword)
                .isEqualTo("password");
    }
}
