package io.github.fherbreteau.functional.domain.command.factory.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.BOOLEAN;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import io.github.fherbreteau.functional.domain.command.CheckCommand;
import io.github.fherbreteau.functional.domain.command.Command;
import io.github.fherbreteau.functional.domain.command.factory.UserCommandFactory;
import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.domain.entities.UserCommandType;
import io.github.fherbreteau.functional.domain.entities.UserInput;
import io.github.fherbreteau.functional.driven.PasswordProtector;
import io.github.fherbreteau.functional.driven.repository.GroupRepository;
import io.github.fherbreteau.functional.driven.repository.UserRepository;
import io.github.fherbreteau.functional.driven.rules.UserChecker;
import io.github.fherbreteau.functional.driven.rules.UserUpdater;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserCommandFactoriesTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private UserChecker userChecker;
    @Mock
    private UserUpdater userUpdater;
    @Mock
    private PasswordProtector passwordProtector;
    @Mock
    private User actor;

    @Test
    void shouldCreateAUserWithGivenInformation() {
        UserCommandFactory<User> factory = new CreateUserCommandFactory();
        UUID userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UserInput input = UserInput.builder("user")
                .withUserId(userId)
                .withGroupId(groupId)
                .withPassword("password")
                .build();

        Group group = Group.builder("group").withGroupId(groupId).build();

        given(userChecker.canCreateUser("user", actor)).willReturn(true);

        given(groupRepository.exists(groupId)).willReturn(true);
        given(groupRepository.findById(groupId)).willReturn(group);

        given(userRepository.exists("user")).willReturn(false);
        given(userRepository.create(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(userRepository.updatePassword(any(), eq("password")))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(passwordProtector.validate("password")).willReturn(List.of());
        given(passwordProtector.protect("password")).willAnswer(invocation -> invocation.getArgument(0));
        given(userUpdater.createUser(any())).willAnswer(invocation -> invocation.getArgument(0));

        CheckCommand<User> checkCommand = factory.createCommand(userRepository, groupRepository, userChecker,
                userUpdater, passwordProtector, UserCommandType.USERADD, input);
        Command<Output<User>> executeCommand = checkCommand.execute(actor);
        Output<User> output = executeCommand.execute(actor);

        assertThat(output).extracting(Output::getValue, type(User.class))
                .isNotNull()
                .extracting(User::getName)
                .isEqualTo("user");
        assertThat(output).extracting(Output::getValue, type(User.class))
                .isNotNull()
                .extracting(User::getUserId)
                .isEqualTo(userId);
        assertThat(output).extracting(Output::getValue, type(User.class))
                .isNotNull()
                .extracting(User::getGroup, type(Group.class))
                .isEqualTo(group);
        verify(userRepository).updatePassword(any(), eq("password"));
    }

    @Test
    void shouldModifyAUserWithGivenInformation() {
        UserCommandFactory<User> factory = new UpdateUserCommandFactory();
        UUID userId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UserInput input = UserInput.builder("user")
                .withUserId(userId)
                .withGroupId(groupId)
                .withPassword("password")
                .withNewName("user1")
                .build();

        User user = User.builder("user").build();
        Group group = Group.builder("group").withGroupId(groupId).build();

        given(userChecker.canUpdateUser("user", actor)).willReturn(true);
        given(userRepository.exists("user")).willReturn(true);
        given(userRepository.exists(userId)).willReturn(false);
        given(userRepository.exists("user1")).willReturn(false);
        given(userRepository.findByName("user")).willReturn(user);
        given(userRepository.update(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(userRepository.updatePassword(any(), eq("password")))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(groupRepository.exists(groupId)).willReturn(true);
        given(groupRepository.findById(groupId)).willReturn(group);
        given(passwordProtector.validate("password")).willReturn(List.of());
        given(passwordProtector.protect("password")).willAnswer(invocation -> invocation.getArgument(0));
        given(userUpdater.updateUser(any(), any())).willAnswer(invocation -> invocation.getArgument(1));

        CheckCommand<User> checkCommand = factory.createCommand(userRepository, groupRepository, userChecker,
                userUpdater, passwordProtector, UserCommandType.USERMOD, input);
        Command<Output<User>> executeCommand = checkCommand.execute(actor);
        Output<User> output = executeCommand.execute(actor);

        assertThat(output).extracting(Output::getValue, type(User.class))
                .isNotNull()
                .extracting(User::getName)
                .isEqualTo("user1");
        assertThat(output).extracting(Output::getValue, type(User.class))
                .isNotNull()
                .extracting(User::getUserId)
                .isEqualTo(userId);
        assertThat(output).extracting(Output::getValue, type(User.class))
                .isNotNull()
                .extracting(User::getGroup, type(Group.class))
                .isEqualTo(group);
        verify(userRepository).updatePassword(any(), eq("password"));
    }

    @Test
    void shouldModifyAUserWithGivenGroupName() {
        UserCommandFactory<User> factory = new UpdateUserCommandFactory();
        UUID groupId = UUID.randomUUID();
        UserInput input = UserInput.builder("user")
                .withGroups(List.of("group1"))
                .build();

        User user = User.builder("user").build();
        Group group = Group.builder("group1").withGroupId(groupId).build();

        given(userChecker.canUpdateUser("user", actor)).willReturn(true);
        given(userRepository.exists("user")).willReturn(true);
        given(userRepository.findByName("user")).willReturn(user);
        given(groupRepository.exists("group1")).willReturn(true);
        given(groupRepository.findByName("group1")).willReturn(group);
        given(userRepository.update(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(userUpdater.updateUser(any(), any())).willAnswer(invocation -> invocation.getArgument(1));

        CheckCommand<User> checkCommand = factory.createCommand(userRepository, groupRepository, userChecker,
                userUpdater, passwordProtector, UserCommandType.USERMOD, input);
        Command<Output<User>> executeCommand = checkCommand.execute(actor);
        Output<User> output = executeCommand.execute(actor);

        assertThat(output).extracting(Output::getValue, type(User.class))
                .isNotNull()
                .extracting(User::getName)
                .isEqualTo("user");
        assertThat(output).extracting(Output::getValue, type(User.class))
                .isNotNull()
                .extracting(User::getGroup, type(Group.class))
                .isEqualTo(group);
    }

    @Test
    void shouldDeleteAnUser() {
        UserCommandFactory<Void> factory = new DeleteUserCommandFactory();
        UserInput input = UserInput.builder("user").build();
        User user = User.builder("user").build();

        given(userChecker.canDeleteUser("user", actor)).willReturn(true);
        given(userRepository.exists("user")).willReturn(true);
        given(userRepository.findByName("user")).willReturn(user);

        CheckCommand<Void> checkCommand = factory.createCommand(userRepository, groupRepository, userChecker,
                userUpdater, passwordProtector, UserCommandType.USERDEL, input);
        Command<Output<Void>> executeCommand = checkCommand.execute(actor);
        Output<Void> output = executeCommand.execute(actor);

        assertThat(output).extracting(Output::getValue)
                .isNull();
    }

    @Test
    void shouldGetAUserByName() {
        UserCommandFactory<User> factory = new GetUserCommandFactory();
        UserInput input = UserInput.builder("user").build();
        User user = User.builder("user").build();
        given(userRepository.exists("user")).willReturn(true);
        given(userRepository.findByName("user")).willReturn(user);

        CheckCommand<User> checkCommand = factory.createCommand(userRepository, groupRepository, userChecker,
                userUpdater, passwordProtector, UserCommandType.ID, input);
        Command<Output<User>> executeCommand = checkCommand.execute(actor);
        Output<User> output = executeCommand.execute(actor);

        assertThat(output).extracting(Output::getValue, type(User.class))
                .isNotNull()
                .extracting(User::getName)
                .isEqualTo("user");
    }

    @Test
    void shouldGetCurrentUser() {
        UserCommandFactory<User> factory = new GetUserCommandFactory();
        UserInput input = UserInput.builder(null).build();
        when(actor.getName()).thenReturn("actor");

        CheckCommand<User> checkCommand = factory.createCommand(userRepository, groupRepository, userChecker,
                userUpdater, passwordProtector, UserCommandType.ID, input);
        Command<Output<User>> executeCommand = checkCommand.execute(actor);
        Output<User> output = executeCommand.execute(actor);

        assertThat(output).extracting(Output::getValue, type(User.class))
                .isNotNull()
                .extracting(User::getName)
                .isEqualTo("actor");
    }

    @Test
    void shouldCreateAGroupWithGivenInformation() {
        UserCommandFactory<Group> factory = new CreateGroupCommandFactory();
        UUID groupId = UUID.randomUUID();
        UserInput input = UserInput.builder("group").withGroupId(groupId).build();

        given(userChecker.canCreateGroup("group", actor)).willReturn(true);
        given(groupRepository.exists("group")).willReturn(false);
        given(groupRepository.exists(groupId)).willReturn(false);
        given(groupRepository.create(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(userUpdater.createGroup(any())).willAnswer(invocation -> invocation.getArgument(0));

        CheckCommand<Group> checkCommand = factory.createCommand(userRepository, groupRepository, userChecker,
                userUpdater, passwordProtector, UserCommandType.GROUPADD, input);
        Command<Output<Group>> executeCommand = checkCommand.execute(actor);
        Output<Group> output = executeCommand.execute(actor);

        assertThat(output).extracting(Output::getValue, type(Group.class))
                .isNotNull()
                .extracting(Group::getName)
                .isEqualTo("group");
        assertThat(output).extracting(Output::getValue, type(Group.class))
                .isNotNull()
                .extracting(Group::getGroupId)
                .isEqualTo(groupId);
    }

    @Test
    void shouldModifyAGroupWithGivenInformation() {
        UserCommandFactory<Group> factory = new UpdateGroupCommandFactory();
        UUID groupId = UUID.randomUUID();
        UserInput input = UserInput.builder("group").withGroupId(groupId).withNewName("group1").build();

        Group group = Group.builder("group").build();

        given(userChecker.canUpdateGroup("group", actor)).willReturn(true);
        given(groupRepository.exists("group")).willReturn(true);
        given(groupRepository.exists("group1")).willReturn(false);
        given(groupRepository.exists(groupId)).willReturn(false);
        given(groupRepository.findByName("group")).willReturn(group);
        given(groupRepository.update(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(userUpdater.updateGroup(any(), any())).willAnswer(invocation -> invocation.getArgument(1));

        CheckCommand<Group> checkCommand = factory.createCommand(userRepository, groupRepository, userChecker,
                userUpdater, passwordProtector, UserCommandType.GROUPMOD, input);
        Command<Output<Group>> executeCommand = checkCommand.execute(actor);
        Output<Group> output = executeCommand.execute(actor);

        assertThat(output).extracting(Output::getValue, type(Group.class))
                .isNotNull()
                .extracting(Group::getName)
                .isEqualTo("group1");
        assertThat(output).extracting(Output::getValue, type(Group.class))
                .isNotNull()
                .extracting(Group::getGroupId)
                .isEqualTo(groupId);
    }

    @Test
    void shouldDeleteAGroup() {
        UserCommandFactory<Void> factory = new DeleteGroupCommandFactory();
        UserInput input = UserInput.builder("group").withForce(true).build();
        Group group = Group.builder("group").build();

        given(userChecker.canDeleteGroup("group", actor)).willReturn(true);
        given(groupRepository.exists("group")).willReturn(true);
        given(groupRepository.findByName("group")).willReturn(group);

        CheckCommand<Void> checkCommand = factory.createCommand(userRepository, groupRepository, userChecker,
                userUpdater, passwordProtector, UserCommandType.GROUPDEL, input);
        Command<Output<Void>> executeCommand = checkCommand.execute(actor);
        Output<Void> output = executeCommand.execute(actor);

        assertThat(output).extracting(Output::isSuccess, BOOLEAN)
                .isTrue();
        verify(userRepository).removeGroupFromUser(group);
    }

    @Test
    void shouldGetGroupsOfAUserByName() {
        UserCommandFactory<List<Group>> factory = new GetGroupCommandFactory();
        UserInput input = UserInput.builder("user").build();
        User user = User.builder("user").withGroup(Group.builder("group").build()).build();
        given(userRepository.exists("user")).willReturn(true);
        given(userRepository.findByName("user")).willReturn(user);

        CheckCommand<List<Group>> checkCommand = factory.createCommand(userRepository, groupRepository, userChecker,
                userUpdater, passwordProtector, UserCommandType.GROUPS, input);
        Command<Output<List<Group>>> executeCommand = checkCommand.execute(actor);
        Output<List<Group>> output = executeCommand.execute(actor);

        assertThat(output).extracting(Output::getValue, list(Group.class))
                .isNotNull()
                .hasSize(1)
                .first()
                .extracting(Group::getName)
                .isEqualTo("group");
    }
}
