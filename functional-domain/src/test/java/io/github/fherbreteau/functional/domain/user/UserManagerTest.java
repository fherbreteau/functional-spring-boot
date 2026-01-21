package io.github.fherbreteau.functional.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.BOOLEAN;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.mockito.BDDMockito.given;

import java.util.UUID;

import io.github.fherbreteau.functional.domain.entities.Failure;
import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driven.repository.GroupRepository;
import io.github.fherbreteau.functional.driven.repository.UserRepository;
import io.github.fherbreteau.functional.driven.rules.UserUpdater;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserManagerTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserUpdater userUpdater;
    @Mock
    private GroupRepository groupRepository;

    private UserManager userManager;

    @BeforeEach
    void setup() {
        userManager = new UserManager(userRepository, userUpdater, groupRepository);
    }

    @Test
    void shouldLookupUserByName() {
        // GIVEN
        given(userRepository.exists("user")).willReturn(true);
        User user = User.builder("user").build();
        given(userRepository.findByName("user")).willReturn(user);
        // WHEN
        Output<User> result = userManager.findUserByName("user");
        // THEN
        assertThat(result).isNotNull()
                .extracting(Output::isSuccess, InstanceOfAssertFactories.BOOLEAN)
                .isTrue();
        assertThat(result)
                .extracting(Output::getValue)
                .isEqualTo(user);
    }

    @Test
    void shouldReturnUnErrorWhenUserLookupFails() {
        // GIVEN
        given(userRepository.exists("user")).willReturn(false);
        // WHEN
        Output<User> result = userManager.findUserByName("user");
        // THEN
        assertThat(result).isNotNull()
                .extracting(Output::isFailure, InstanceOfAssertFactories.BOOLEAN)
                .isTrue();
        assertThat(result)
                .extracting(Output::getFailure)
                .extracting(Failure::getMessage)
                .isEqualTo("user not found");
        assertThat(result)
                .extracting(Output::getFailure)
                .extracting(Failure::getReasons, list(String.class))
                .isEmpty();
    }

    @Test
    void shouldLookupGroupByName() {
        // GIVEN
        given(groupRepository.exists("group")).willReturn(true);
        Group group = Group.builder("group").build();
        given(groupRepository.findByName("group")).willReturn(group);
        // WHEN
        Output<Group> result = userManager.findGroupByName("group");
        // THEN
        assertThat(result).isNotNull()
                .extracting(Output::isSuccess, InstanceOfAssertFactories.BOOLEAN)
                .isTrue();
        assertThat(result)
                .extracting(Output::getValue)
                .isEqualTo(group);
    }

    @Test
    void shouldReturnUnErrorWhenGroupLookupFails() {
        // GIVEN
        given(groupRepository.exists("group")).willReturn(false);
        // WHEN
        Output<Group> result = userManager.findGroupByName("group");
        // THEN
        assertThat(result).isNotNull()
                .extracting(Output::isFailure, InstanceOfAssertFactories.BOOLEAN)
                .isTrue();
        assertThat(result)
                .extracting(Output::getFailure)
                .extracting(Failure::getMessage)
                .isEqualTo("group not found");
        assertThat(result)
                .extracting(Output::getFailure)
                .extracting(Failure::getReasons, list(String.class))
                .isEmpty();
    }

    @Test
    void shouldSuccessWhenUserPasswordIsFound() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        User user = User.builder("user").withUserId(userId).build();
        given(userRepository.exists(userId)).willReturn(true);
        given(userRepository.getPassword(user)).willReturn("password");
        // WHEN
        Output<String> result = userManager.getPassword(user);
        // THEN
        assertThat(result).extracting(Output::isSuccess, BOOLEAN).isTrue();
        assertThat(result).extracting(Output::getValue).isEqualTo("password");
    }

    @Test
    void shouldFailGettingPasswordWhenUserNotFound() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        User user = User.builder("user").withUserId(userId).build();
        given(userRepository.exists(userId)).willReturn(false);
        // WHEN
        Output<String> result = userManager.getPassword(user);
        // THEN
        assertThat(result).extracting(Output::isFailure, BOOLEAN).isTrue();
        assertThat(result)
                .extracting(Output::getFailure)
                .extracting(Failure::getMessage)
                .isEqualTo("User user not found");
    }

    @Test
    void shouldFailGettingPasswordWhenPasswordEmpty() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        User user = User.builder("user").withUserId(userId).build();
        given(userRepository.exists(userId)).willReturn(true);
        given(userRepository.getPassword(user)).willReturn("");
        // WHEN
        Output<String> result = userManager.getPassword(user);
        // THEN
        assertThat(result).extracting(Output::isFailure, BOOLEAN).isTrue();
        assertThat(result)
                .extracting(Output::getFailure)
                .extracting(Failure::getMessage)
                .isEqualTo("Password for user user not found");
    }

    @Test
    void shouldFailGettingPasswordWhenPasswordIsNull() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        User user = User.builder("user").withUserId(userId).build();
        given(userRepository.exists(userId)).willReturn(true);
        given(userRepository.getPassword(user)).willReturn(null);
        // WHEN
        Output<String> result = userManager.getPassword(user);
        // THEN
        assertThat(result).extracting(Output::isFailure, BOOLEAN).isTrue();
        assertThat(result)
                .extracting(Output::getFailure)
                .extracting(Failure::getMessage)
                .isEqualTo("Password for user user not found");
    }
}
