package io.github.fherbreteau.functional.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.BOOLEAN;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AccessRightTest {

    public static Stream<Arguments> shouldBeCorrectlyConfigured() {
        return Stream.of(
                Arguments.arguments(AccessRight.none(), false, false, false),
                Arguments.arguments(AccessRight.readOnly(), true, false, false),
                Arguments.arguments(AccessRight.writeOnly(), false, true, false),
                Arguments.arguments(AccessRight.executeOnly(), false, false, true),
                Arguments.arguments(AccessRight.readWrite(), true, true, false),
                Arguments.arguments(AccessRight.readExecute(), true, false, true),
                Arguments.arguments(AccessRight.writeExecute(), false, true, true),
                Arguments.arguments(AccessRight.full(), true, true, true)
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldBeCorrectlyConfigured(AccessRight right, boolean read, boolean write, boolean execute) {
        assertThat(right).extracting(AccessRight::isRead, BOOLEAN).isEqualTo(read);
        assertThat(right).extracting(AccessRight::isWrite, BOOLEAN).isEqualTo(write);
        assertThat(right).extracting(AccessRight::isExecute, BOOLEAN).isEqualTo(execute);
    }

    @Test
    void testThatRemovedElementChangeAccesses() {
        AccessRight right = AccessRight.full();

        AccessRight changed = right.removeExecute();
        assertThat(changed).extracting(AccessRight::isRead, BOOLEAN).isTrue();
        assertThat(changed).extracting(AccessRight::isWrite, BOOLEAN).isTrue();
        assertThat(changed).extracting(AccessRight::isExecute, BOOLEAN).isFalse();

        changed = right.removeWrite();
        assertThat(changed).extracting(AccessRight::isRead, BOOLEAN).isTrue();
        assertThat(changed).extracting(AccessRight::isWrite, BOOLEAN).isFalse();
        assertThat(changed).extracting(AccessRight::isExecute, BOOLEAN).isTrue();

        changed = right.removeRead();
        assertThat(changed).extracting(AccessRight::isRead, BOOLEAN).isFalse();
        assertThat(changed).extracting(AccessRight::isWrite, BOOLEAN).isTrue();
        assertThat(changed).extracting(AccessRight::isExecute, BOOLEAN).isTrue();
    }

    @Test
    void testThatHashcodeAndEqualsAreCorrectlyChecked() {
        AccessRight right = AccessRight.none();

        assertThat((Object) right).isNotEqualTo("");
        AccessRight left = right.addRead();
        assertThat(right).isNotEqualTo(left).doesNotHaveSameHashCodeAs(left);
        left = right.addWrite();
        assertThat(right).isNotEqualTo(left).doesNotHaveSameHashCodeAs(left);
        left = right.addExecute();
        assertThat(right).isNotEqualTo(left).doesNotHaveSameHashCodeAs(left);
    }
}
