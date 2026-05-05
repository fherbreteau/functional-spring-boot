package io.github.fherbreteau.functional.domain.command.impl.check;

import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.github.fherbreteau.functional.domain.command.impl.error.UserErrorCommand;
import io.github.fherbreteau.functional.domain.command.impl.success.UpdateUserCommand;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.domain.entities.UserCommandType;
import io.github.fherbreteau.functional.domain.entities.UserInput;
import io.github.fherbreteau.functional.driven.PasswordProtector;
import io.github.fherbreteau.functional.driven.repository.GroupRepository;
import io.github.fherbreteau.functional.driven.repository.UserRepository;
import io.github.fherbreteau.functional.driven.rules.UserChecker;
import io.github.fherbreteau.functional.driven.rules.UserUpdater;

public class CheckUpdateUserCommand extends AbstractCheckUserCommand<User, UpdateUserCommand> {
    private final PasswordProtector passwordProtector;
    private final String name;
    private final UUID userId;
    private final UUID groupId;
    private final List<String> groups;
    private final String newName;
    private final String password;
    private final UserCommandType type;
    private final UserInput input;

    public CheckUpdateUserCommand(UserRepository userRepository, GroupRepository groupRepository,
                                  UserChecker userChecker, UserUpdater userUpdater, PasswordProtector passwordProtector,
                                  UserCommandType type, UserInput input) {
        super(userRepository, groupRepository, userChecker, userUpdater);
        this.passwordProtector = passwordProtector;
        this.name = input.getName();
        this.userId = input.getUserId();
        this.groupId = input.getGroupId();
        this.groups = input.getGroups();
        this.newName = input.getNewName();
        this.password = input.getPassword();
        this.type = type;
        this.input = input;
    }

    @Override
    protected List<String> checkAccess(User actor) {
        List<String> reasons = new ArrayList<>();
        if (!userChecker.canUpdateUser(name, actor)) {
            reasons.add(String.format("%s can't update user %s", actor, name));
        }
        if (!userRepository.exists(name)) {
            reasons.add(String.format("user %s is missing", name));
        }
        if (nonNull(userId) && userRepository.exists(userId)) {
            reasons.add(String.format("user with %s already exists", userId));
        }
        if (nonNull(newName) && userRepository.exists(newName)) {
            reasons.add(String.format("user %s already exists", newName));
        }
        if (nonNull(groupId) && !groupRepository.exists(groupId)) {
            reasons.add(String.format("group with id %s is missing", groupId));
        }
        if (!groups.isEmpty() && groups.stream().anyMatch(g -> !groupRepository.exists(g))) {
            reasons.add(String.format("one of %s is missing", String.join(", ", groups)));
        }
        if (nonNull(password)) {
            reasons.addAll(passwordProtector.validate(name, password));
        }
        return reasons;
    }

    @Override
    protected UpdateUserCommand createSuccess() {
        logger.debug("Creating execute command");
        return new UpdateUserCommand(userRepository, groupRepository, userUpdater, passwordProtector, input);
    }

    @Override
    protected UserErrorCommand<User> createError(List<String> reasons) {
        return new UserErrorCommand<>(type, input, reasons);
    }
}
