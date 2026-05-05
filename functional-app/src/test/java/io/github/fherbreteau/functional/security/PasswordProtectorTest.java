package io.github.fherbreteau.functional.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;

import io.github.fherbreteau.functional.driven.PasswordProtector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.ValidationResult;
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

    @Nested
    class PasswordValidationProtector {
        @Mock
        private ValidationResult result;

        @Test
        void passwordProtectorShouldDelegateToPasswordValidatorWhenValidating() {
            // GIVEN
            given(result.isValid()).willReturn(true);
            given(passwordValidator.validate(any())).willReturn(result);
            // WHEN
            assertThat(passwordProtector.validate("username", "password")).isEmpty();
            // THEN
            then(passwordValidator).should().validate(dataCaptor.capture());
            assertThat(dataCaptor.getValue())
                    .extracting(PasswordData::getPassword)
                    .hasToString("password");
            assertThat(dataCaptor.getValue())
                    .extracting(PasswordData::getUsername)
                    .hasToString("username");
        }

        @Test
        void passwordProtectorShouldReturnValidationErrorsWhenValidationFails() {
            // GIVEN
            given(result.isValid()).willReturn(false);
            given(result.getMessages()).willReturn(List.of("error1", "error2"));
            given(passwordValidator.validate(any())).willReturn(result);
            // WHEN
            assertThat(passwordProtector.validate("username", "password"))
                    .hasSize(2)
                    .containsExactly("error1", "error2");
            // THEN
            then(passwordValidator).should().validate(dataCaptor.capture());
            assertThat(dataCaptor.getValue())
                    .extracting(PasswordData::getPassword)
                    .hasToString("password");
            assertThat(dataCaptor.getValue())
                    .extracting(PasswordData::getUsername)
                    .hasToString("username");
        }
    }
}
