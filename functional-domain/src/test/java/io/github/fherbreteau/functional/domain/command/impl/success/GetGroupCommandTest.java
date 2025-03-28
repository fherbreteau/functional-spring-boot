package io.github.fherbreteau.functional.domain.command.impl.success;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.UUID;

import io.github.fherbreteau.functional.domain.command.Command;
import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driven.repository.GroupRepository;
import io.github.fherbreteau.functional.driven.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetGroupCommandTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private User actor;

    @Test
    void shouldGetGroupsOfAUserById() {
        UUID userId = UUID.randomUUID();
        User user = User.builder("user").withUserId(userId).withGroup(Group.builder("group").build()).build();
        given(userRepository.findById(userId)).willReturn(user);

        Command<Output<List<Group>>> executeCommand = new GetGroupCommand(userRepository, groupRepository, null, userId);
        Output<List<Group>> output = executeCommand.execute(actor);

        assertThat(output).extracting(Output::getValue, list(Group.class))
                .isNotNull()
                .hasSize(1)
                .first()
                .extracting(Group::getName)
                .isEqualTo("group");
    }

    @Test
    void shouldGetGroupsOfActor() {
        given(actor.getGroups()).willReturn(List.of(Group.builder("group").build()));

        Command<Output<List<Group>>> executeCommand = new GetGroupCommand(userRepository, groupRepository, null, null);
        Output<List<Group>> output = executeCommand.execute(actor);

        assertThat(output).extracting(Output::getValue, list(Group.class))
                .isNotNull()
                .hasSize(1)
                .first()
                .extracting(Group::getName)
                .isEqualTo("group");
    }
}
