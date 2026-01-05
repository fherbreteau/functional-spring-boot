package io.github.fherbreteau.functional.driving;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.BOOLEAN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import io.github.fherbreteau.functional.domain.command.CheckCommand;
import io.github.fherbreteau.functional.domain.command.Command;
import io.github.fherbreteau.functional.domain.command.CompositeUserCommandFactory;
import io.github.fherbreteau.functional.domain.entities.*;
import io.github.fherbreteau.functional.domain.user.UserManager;
import io.github.fherbreteau.functional.driving.impl.UserServiceImpl;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    private UserService userService;
    @Mock
    private UserManager userManager;
    @Mock
    private CompositeUserCommandFactory commandFactory;
    @Mock
    private CheckCommand<Object> checkCommand;
    @Mock
    private Command<Output<Object>> executeCommand;
    @Mock
    private User actor;

    @BeforeEach
    void setup() {
        userService = new UserServiceImpl(userManager, commandFactory);
    }

    @Test
    void findUserByNameShouldDelegateToUserService() {
        // GIVEN
        given(userManager.findUserByName("name")).willReturn(Output.success(User.builder("name").build()));
        // WHEN
        Output<User> result = userService.findUserByName("name");
        // THEN
        assertThat(result).extracting(Output::isSuccess, BOOLEAN)
                .isTrue();
    }

    @Test
    void findGroupByNameShouldDelegateToUserService() {
        // GIVEN
        given(userManager.findGroupByName("name")).willReturn(Output.success(Group.builder("name").build()));
        // WHEN
        Output<Group> result = userService.findGroupByName("name");
        // THEN
        assertThat(result).extracting(Output::isSuccess, BOOLEAN)
                .isTrue();
    }

    @Test
    void testUserPasswordExtraction() {
        // GIVEN
        User user = User.builder("user").build();
        given(userManager.getPassword(user)).willReturn(Output.success("password"));
        // WHEN
        Output<String> result = userService.getUserPassword(user);
        // THEN
        assertThat(result).extracting(Output::isSuccess, BOOLEAN)
                .isTrue();
    }

    @Test
    void testProcessKnownCommandShouldOutputAResult() {
        // Given
        given(commandFactory.createCommand(any(), any())).willReturn(checkCommand);
        given(checkCommand.execute(actor)).willReturn(executeCommand);
        given(executeCommand.execute(actor)).willReturn(Output.success(new Object()));
        // When
        Output<User> result = userService.processCommand(UserCommandType.USERADD, actor, UserInput.builder("user").build());
        assertThat(result).isNotNull()
                .extracting(Output::isSuccess)
                .asInstanceOf(InstanceOfAssertFactories.BOOLEAN)
                .isTrue();
        assertThat(result)
                .extracting(Output::getValue)
                .isNotNull();
        assertThat(result).extracting(Output::isFailure)
                .asInstanceOf(InstanceOfAssertFactories.BOOLEAN)
                .isFalse();
        assertThat(result)
                .extracting(Output::getFailure)
                .isNull();
    }
}
