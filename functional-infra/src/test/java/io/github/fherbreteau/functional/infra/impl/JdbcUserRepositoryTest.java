package io.github.fherbreteau.functional.infra.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.list;

import java.util.List;
import java.util.UUID;

import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driven.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@JdbcTest
@ActiveProfiles("test")
@ContextConfiguration(classes = { JdbcUserRepository.class, JdbcUserGroupRepository.class })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JdbcUserRepositoryTest {

    private static final UUID ROOT_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final String ROOT_NAME = "root";

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldCheckExistenceOfRootUserByName() {
        assertThat(userRepository.exists(ROOT_NAME)).isTrue();
    }

    @Test
    void shouldCheckExistenceOfRootUserById() {
        assertThat(userRepository.exists(ROOT_ID)).isTrue();
    }

    @Test
    void shouldExtractRootUserFromName() {
        User user = userRepository.findByName(ROOT_NAME);
        assertThat(user)
                .extracting(User::getUserId, User::getName)
                .containsExactly(ROOT_ID, ROOT_NAME);
        assertThat(user)
                .extracting(User::getGroups, list(Group.class))
                .singleElement()
                .isEqualTo(Group.builder("root").withGroupId(ROOT_ID).build());
    }

    @Test
    void shouldExtractRootUserFromId() {
        User user = userRepository.findById(ROOT_ID);
        assertThat(user)
                .extracting(User::getUserId, User::getName)
                .containsExactly(ROOT_ID, ROOT_NAME);
        assertThat(user)
                .extracting(User::getGroups, list(Group.class))
                .singleElement()
                .isEqualTo(Group.builder("root").withGroupId(ROOT_ID).build());
    }

    @Test
    void shouldNotExtractUserWithNoGroup() {
        UUID userId = UUID.fromString("1115a887-fec2-44fe-9f4d-73d8d6fec46b");
        assertThatThrownBy(() -> userRepository.findById(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("groups must contain at least one group");
    }

    @Test
    void shouldCreateAnUser() {
        UUID userId = UUID.fromString("987210f6-ce79-4a4c-9222-54bcd34a8ee2");
        UUID group1Id = UUID.fromString("22bdb905-73d4-479e-99fc-62d46ad27d67");
        UUID group2Id = UUID.fromString("afa62e92-2239-40bf-a0d0-606f20e00b00");
        UUID group3Id = UUID.fromString("bc321002-b703-424b-9c3f-d47bf15be632");
        User userToCreate = User.builder("Test3")
                .withUserId(userId)
                .withGroups(List.of(
                        Group.builder("Test").withGroupId(group1Id).build(),
                        Group.builder("group_delete").withGroupId(group2Id).build(),
                        Group.builder("User").withGroupId(group3Id).build()
                ))
                .build();
        assertThat(userRepository.create(userToCreate)).isEqualTo(userToCreate);
        assertThat(userRepository.exists("Test3")).isTrue();
        assertThat(userRepository.exists(userId)).isTrue();

        assertThat(userRepository.findByName("Test3")).isEqualTo(userToCreate);
    }

    @Test
    void shouldUpdateAnExistingUser() {
        UUID userId = UUID.fromString("bc321002-b703-424b-9c3f-d47bf15be632");
        User userToUpdate = User.builder("User")
                .withUserId(userId)
                .build()
                .copy().withName("User2").build();
        assertThat(userRepository.update(userToUpdate)).isEqualTo(userToUpdate);
        assertThat(userRepository.exists("User")).isFalse();
        assertThat(userRepository.exists(userId)).isTrue();

        UUID newUUID = UUID.fromString("4a9e0664-df93-433a-a953-c59826abc89b");
        userToUpdate = userToUpdate.copy().withUserId(newUUID).build();
        assertThat(userRepository.update(userToUpdate)).isEqualTo(userToUpdate);
        assertThat(userRepository.exists(newUUID)).isTrue();
        assertThat(userRepository.exists(userId)).isFalse();
        UUID groupId = UUID.fromString("38f2056e-d43e-4a30-88bf-cc4835dc7373");
        assertThat(userRepository.findById(newUUID))
                .extracting(User::getGroups, list(Group.class))
                .singleElement()
                .extracting(Group::getName, Group::getGroupId)
                .containsExactly("to_update_by_id", groupId);
    }

    @Test
    void shouldDeleteExistingUser() {
        UUID userId = UUID.fromString("1115a887-fec2-44fe-9f4d-73d8d6fec46b");
        User user = User.builder("user_delete").withUserId(userId).build();
        userRepository.delete(user);
        assertThat(userRepository.exists(userId)).isFalse();
    }

    @Test
    void shouldLookForUserWithASpecificGroupName() {
        assertThat(userRepository.hasUserWithGroup("Test3")).isFalse();
        assertThat(userRepository.hasUserWithGroup("root")).isTrue();
    }

    @Test
    void shouldValidateUserPassword() {
        UUID userId = UUID.fromString("bc321002-b703-424b-9c3f-d47bf15be632");
        User user = User.builder("User").withUserId(userId).build();
        assertThat(userRepository.getPassword(user))
                .isEqualTo("password");
    }

    @Test
    void shouldUpdateUserPassword() {
        UUID userId = UUID.fromString("22bdb905-73d4-479e-99fc-62d46ad27d67");
        User user = User.builder("Test").withUserId(userId).build();
        assertThat(userRepository.updatePassword(user, "password")).isEqualTo(user);
        assertThat(userRepository.getPassword(user))
                .isEqualTo("password");
    }

    @Test
    void shouldRemoveGroupFromUser() {
        UUID groupId = UUID.fromString("22bdb905-73d4-479e-99fc-62d46ad27d67");
        Group group = Group.builder("Test").withGroupId(groupId).build();
        userRepository.removeGroupFromUser(group);
        assertThat(userRepository.findByName("Test"))
                .extracting(User::getGroups, list(Group.class))
                .singleElement()
                .isEqualTo(Group.builder("User")
                        .withGroupId(UUID.fromString("bc321002-b703-424b-9c3f-d47bf15be632"))
                        .build());
    }
}
