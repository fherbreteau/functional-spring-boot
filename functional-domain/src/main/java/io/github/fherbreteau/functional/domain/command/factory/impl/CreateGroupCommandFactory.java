package io.github.fherbreteau.functional.domain.command.factory.impl;

import static java.lang.System.Logger.Level.DEBUG;
import static java.util.Objects.nonNull;

import io.github.fherbreteau.functional.domain.command.CheckCommand;
import io.github.fherbreteau.functional.domain.command.factory.UserCommandFactory;
import io.github.fherbreteau.functional.domain.command.impl.check.CheckCreateGroupCommand;
import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.UserCommandType;
import io.github.fherbreteau.functional.domain.entities.UserInput;
import io.github.fherbreteau.functional.driven.PasswordProtector;
import io.github.fherbreteau.functional.driven.repository.GroupRepository;
import io.github.fherbreteau.functional.driven.repository.UserRepository;
import io.github.fherbreteau.functional.driven.rules.UserChecker;
import io.github.fherbreteau.functional.driven.rules.UserUpdater;

public class CreateGroupCommandFactory implements UserCommandFactory<Group> {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());

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
        logger.log(DEBUG, "Creating check command");
        return new CheckCreateGroupCommand(repository, groupRepository, userChecker, userUpdater,
                userInput.getName(), userInput.getGroupId());
    }
}
