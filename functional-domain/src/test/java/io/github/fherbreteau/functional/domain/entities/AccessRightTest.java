package io.github.fherbreteau.functional.domain.entities;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.BOOLEAN;

class AccessRightTest {

    @Test
    void shouldBeCorrectlyConfigured() {
        AccessRight right = AccessRight.none();
        assertThat(right).extracting(AccessRight::isRead, BOOLEAN).isFalse();
        assertThat(right).extracting(AccessRight::isWrite, BOOLEAN).isFalse();
        assertThat(right).extracting(AccessRight::isExecute, BOOLEAN).isFalse();

        right = AccessRight.readOnly();
        assertThat(right).extracting(AccessRight::isRead, BOOLEAN).isTrue();
        assertThat(right).extracting(AccessRight::isWrite, BOOLEAN).isFalse();
        assertThat(right).extracting(AccessRight::isExecute, BOOLEAN).isFalse();

        right = AccessRight.writeOnly();
        assertThat(right).extracting(AccessRight::isRead, BOOLEAN).isFalse();
        assertThat(right).extracting(AccessRight::isWrite, BOOLEAN).isTrue();
        assertThat(right).extracting(AccessRight::isExecute, BOOLEAN).isFalse();

        right = AccessRight.executeOnly();
        assertThat(right).extracting(AccessRight::isRead, BOOLEAN).isFalse();
        assertThat(right).extracting(AccessRight::isWrite, BOOLEAN).isFalse();
        assertThat(right).extracting(AccessRight::isExecute, BOOLEAN).isTrue();

        right = AccessRight.readWrite();
        assertThat(right).extracting(AccessRight::isRead, BOOLEAN).isTrue();
        assertThat(right).extracting(AccessRight::isWrite, BOOLEAN).isTrue();
        assertThat(right).extracting(AccessRight::isExecute, BOOLEAN).isFalse();

        right = AccessRight.readExecute();
        assertThat(right).extracting(AccessRight::isRead, BOOLEAN).isTrue();
        assertThat(right).extracting(AccessRight::isWrite, BOOLEAN).isFalse();
        assertThat(right).extracting(AccessRight::isExecute, BOOLEAN).isTrue();

        right = AccessRight.writeExecute();
        assertThat(right).extracting(AccessRight::isRead, BOOLEAN).isFalse();
        assertThat(right).extracting(AccessRight::isWrite, BOOLEAN).isTrue();
        assertThat(right).extracting(AccessRight::isExecute, BOOLEAN).isTrue();

        right = AccessRight.full();
        assertThat(right).extracting(AccessRight::isRead, BOOLEAN).isTrue();
        assertThat(right).extracting(AccessRight::isWrite, BOOLEAN).isTrue();
        assertThat(right).extracting(AccessRight::isExecute, BOOLEAN).isTrue();
    }
}
