package io.github.fherbreteau.functional.infra.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;

import java.util.UUID;

import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driven.repository.GroupRepository;
import io.github.fherbreteau.functional.driven.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@JdbcTest
@ActiveProfiles("test")
@ContextConfiguration(classes = { JdbcGroupRepository.class, JdbcUserGroupRepository.class, JdbcUserRepository.class })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JdbcGroupRepositoryTest {

    private static final UUID ROOT_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final String ROOT_NAME = "root";

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldCheckExistenceOfRootGroupByName() {
        assertThat(groupRepository.exists(ROOT_NAME)).isTrue();
    }

    @Test
    void shouldCheckExistenceOfRootGroupById() {
        assertThat(groupRepository.exists(ROOT_ID)).isTrue();
    }

    @Test
    void shouldExtractRootGroupFromName() {
        Group group = groupRepository.findByName(ROOT_NAME);
        assertThat(group)
                .extracting(Group::getGroupId, Group::getName)
                .containsExactly(ROOT_ID, ROOT_NAME);
    }

    @Test
    void shouldExtractRootGroupFromId() {
        Group group = groupRepository.findById(ROOT_ID);
        assertThat(group)
                .extracting(Group::getGroupId, Group::getName)
                .containsExactly(ROOT_ID, ROOT_NAME);
    }

    @Test
    void shouldCreateNewGroup() {
        UUID groupId = UUID.fromString("f7af0c66-2c03-49b4-9d4d-a4aad754675c");
        Group group = Group.builder("Test3").withGroupId(groupId).build();
        assertThat(groupRepository.create(group)).isEqualTo(group);
        assertThat(groupRepository.exists(groupId)).isTrue();
        assertThat(groupRepository.exists("Test3")).isTrue();
    }

    @Test
    void shouldUpdateExistingGroup() {
        UUID groupId = UUID.fromString("38f2056e-d43e-4a30-88bf-cc4835dc7373");
        Group group = groupRepository.findById(groupId);
        group = group.copy().withName("to_update_by_name").build();
        assertThat(groupRepository.update(group)).isEqualTo(group);
        assertThat(groupRepository.exists(groupId)).isTrue();
        assertThat(groupRepository.exists("to_update_by_id")).isFalse();

        UUID newId = UUID.fromString("dbac7a88-da28-49d0-80e5-533c5913c0f2");
        group = group.copy().withGroupId(newId).build();
        assertThat(groupRepository.update(group)).isEqualTo(group);
        assertThat(groupRepository.exists("to_update_by_name")).isTrue();
        assertThat(groupRepository.exists(groupId)).isFalse();

        UUID userId = UUID.fromString("bc321002-b703-424b-9c3f-d47bf15be632");
        assertThat(userRepository.findById(userId))
                .extracting(User::getGroups, list(Group.class))
                .singleElement()
                .extracting(Group::getName, Group::getGroupId)
                .containsExactly("to_update_by_name", newId);

        assertThat(groupRepository.update(group)).isEqualTo(group);
    }

    @Test
    void shouldDeleteExistingGroup() {
        UUID groupId = UUID.fromString("afa62e92-2239-40bf-a0d0-606f20e00b00");
        Group group = Group.builder("group_delete").withGroupId(groupId).build();
        groupRepository.delete(group);
        assertThat(groupRepository.exists(groupId)).isFalse();
    }
}
