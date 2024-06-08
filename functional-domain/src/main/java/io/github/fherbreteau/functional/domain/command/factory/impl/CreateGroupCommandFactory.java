package io.github.fherbreteau.functional.domain.command.factory.impl;

import io.github.fherbreteau.functional.domain.command.CheckCommand;
import io.github.fherbreteau.functional.domain.command.factory.UserCommandFactory;
import io.github.fherbreteau.functional.domain.command.impl.check.CheckCreateGroupCommand;
import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.UserCommandType;
import io.github.fherbreteau.functional.domain.entities.UserInput;
import io.github.fherbreteau.functional.driven.*;
import io.github.fherbreteau.functional.driven.repository.GroupRepository;
import io.github.fherbreteau.functional.driven.rules.UserChecker;
import io.github.fherbreteau.functional.driven.repository.UserRepository;
import io.github.fherbreteau.functional.driven.rules.UserUpdater;

import static java.util.Objects.nonNull;

public class CreateGroupCommandFactory implements UserCommandFactory<Group> {
    @Override
    public boolean supports(UserCommandType type, UserInput userInput) {
        return type == UserCommandType.GROUPADD && isValid(userInput);
    }

    private boolean isValid(UserInput userInput) {
        return nonNull(userInput.getName());
    }

    @Override
    public CheckCommand<Group> createCommand(UserRepository repository, GroupRepository groupRepository,
                                             UserChecker userChecker, UserUpdater userUpdater,
                                             PasswordProtector passwordProtector, UserCommandType type,
                                             UserInput userInput) {
        return new CheckCreateGroupCommand(repository, groupRepository, userChecker, userUpdater,
                userInput.getName(), userInput.getGroupId());
    }
}
