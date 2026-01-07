package io.github.fherbreteau.functional.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.BOOLEAN;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class UserGroupTest {

    @Test
    void groupShouldBeCorrectlyConfigured() {
        UUID groupId = UUID.randomUUID();
        Group group = Group.builder("group").withGroupId(groupId).build();

        assertThat(group).extracting(Group::getName).isEqualTo("group");
        assertThat(group).extracting(Group::getGroupId).isEqualTo(groupId);
        assertThat(group).extracting(Group::isRoot, BOOLEAN).isFalse();
        assertThat(group).asString().isEqualTo("group(" + groupId + ")");
        assertThat(Group.builder("group").withGroupId(groupId).build()).isEqualTo(group)
                .hasSameHashCodeAs(group);

        assertThat(Group.root()).extracting(Group::isRoot, BOOLEAN).isTrue();
        assertThat(User.root()).extracting(User::isSuperUser, BOOLEAN).isTrue();
    }

    @Test
    void userShouldBeCorrectlyConfigured() {
        UUID groupId = UUID.randomUUID();
        Group group = Group.builder("group").withGroupId(groupId).build();
        UUID userId = UUID.randomUUID();
        User user = User.builder("user").withUserId(userId).withGroup(group).build();

        assertThat(user).extracting(User::getName).isEqualTo("user");
        assertThat(user).extracting(User::getUserId).isEqualTo(userId);
        assertThat(user).extracting(User::getGroup).isEqualTo(group);
        assertThat(user).extracting(User::getGroups, LIST).hasSize(1)
                .first(type(Group.class)).isEqualTo(group);
        assertThat(user).extracting(User::isSuperUser, BOOLEAN).isFalse();
        assertThat(user).asString().isEqualTo("user(" + userId + ")");

        assertThat(User.builder("user").withUserId(userId).withGroup(group).build())
                .isEqualTo(user).hasSameHashCodeAs(user);

        user = User.builder("user").withGroup(Group.builder("group").build()).build();
        assertThat(user).extracting(User::getName).isEqualTo("user");
        assertThat(user).extracting(User::getUserId).isNotEqualTo(userId);
        group = user.getGroup();
        assertThat(group).extracting(Group::getName).isEqualTo("group");
        assertThat(group).extracting(Group::getGroupId).isNotEqualTo(groupId);

        assertThat((Object) user).isNotEqualTo(group);
        assertThat((Object) group).isNotEqualTo(user);
    }

    @Test
    void multiGroupShouldBeCorrectlyConfigured() {
        UUID groupId = UUID.randomUUID();
        Group group = Group.builder("group").withGroupId(groupId).build();

        Group group2 = Group.builder("group2").build();
        User user = User.builder("name").withGroups(List.of(group, group2)).build();
        assertThat(user).extracting(User::getGroup).isEqualTo(group);
        assertThat(user).extracting(User::getGroups, LIST).hasSize(2)
                .first(type(Group.class)).isEqualTo(group);
        assertThat(user).extracting(User::getGroups, LIST).hasSize(2)
                .last(type(Group.class)).isEqualTo(group2);
    }

    @Test
    void shouldNotEqualsOrSameHashcode() {
        Group group = Group.builder("group").build();
        User user1 = User.builder("user").withGroup(group).build();
        User user2 = User.builder("user").withGroup(group).build();
        assertThat(user1).isNotEqualTo(user2)
                .doesNotHaveSameHashCodeAs(user2);

        UUID userId = UUID.randomUUID();
        user1 = User.builder("user1").withUserId(userId).withGroup(group).build();
        user2 = User.builder("user2").withUserId(userId).withGroup(group).build();
        assertThat(user1).isNotEqualTo(user2)
                .doesNotHaveSameHashCodeAs(user2);

        Group group1 = Group.builder("group").build();
        Group group2 = Group.builder("group").build();
        user1 = User.builder("user").withUserId(userId).withGroup(group1).build();
        user2 = User.builder("user").withUserId(userId).withGroup(group2).build();
        assertThat(user1).isNotEqualTo(user2)
                .doesNotHaveSameHashCodeAs(user2);

        assertThat(group1).isNotEqualTo(group2)
                .doesNotHaveSameHashCodeAs(group2);

        UUID groupId = UUID.randomUUID();
        group1 = Group.builder("group1").withGroupId(groupId).build();
        group2 = Group.builder("group2").withGroupId(groupId).build();
        assertThat(group1).isNotEqualTo(group2);
    }

    @Test
    void shouldCheckRequiredParameters() {
        User.Builder userBuilder = User.builder(null).withUserId(UUID.randomUUID());
        assertThatThrownBy(userBuilder::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name is required");
        userBuilder = User.builder("");
        assertThatThrownBy(userBuilder::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name must not be empty");
        userBuilder = User.builder("name").withUserId(UUID.randomUUID()).withGroups(List.of());
        assertThatThrownBy(userBuilder::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("groups must contain at least one group");

        Group.Builder groupBuilder = Group.builder(null);
        assertThatThrownBy(groupBuilder::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name is required");
        groupBuilder = Group.builder("");
        assertThatThrownBy(groupBuilder::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name must not be empty");
    }

    @Test
    void shouldHandleGroupUpdateProperly() {
        UUID groupId1 = UUID.randomUUID();
        Group group1 = Group.builder("group1").withGroupId(groupId1).build();
        UUID groupId2 = UUID.randomUUID();
        Group group2 = Group.builder("group2").withGroupId(groupId2).build();
        UUID groupId3 = UUID.randomUUID();
        Group group3 = Group.builder("group3").withGroupId(groupId3).build();

        User user = User.builder("user").withGroups(List.of(group1, group2)).build();
        user = user.copy().addGroups(List.of(group2, group3)).build();

        assertThat(user).extracting(User::getGroups, list(Group.class))
                .containsExactly(group1, group2, group3);
    }
}
