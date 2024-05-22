package io.github.fherbreteau.functional.domain.command.impl.check;

import io.github.fherbreteau.functional.domain.command.impl.error.UserErrorCommand;
import io.github.fherbreteau.functional.domain.command.impl.success.UserGetCommand;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.domain.entities.UserCommandType;
import io.github.fherbreteau.functional.domain.entities.UserInput;
import io.github.fherbreteau.functional.driven.GroupRepository;
import io.github.fherbreteau.functional.driven.PasswordProtector;
import io.github.fherbreteau.functional.driven.UserChecker;
import io.github.fherbreteau.functional.driven.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.nonNull;

public class CheckUserGetCommand extends AbstractCheckUserCommand<UserGetCommand> {
    private final String name;
    private final UUID userId;

    public CheckUserGetCommand(UserRepository repository, GroupRepository groupRepository, UserChecker userChecker,
                               PasswordProtector passwordProtector, String name, UUID userId) {
        super(repository, groupRepository, userChecker, passwordProtector);
        this.name = name;
        this.userId = userId;
    }

    @Override
    protected List<String> checkAccess(User actor) {
        List<String> reasons = new ArrayList<>();
        if (nonNull(name) && !userRepository.exists(name)) {
            reasons.add(String.format("%s is missing", name));
        }
        if (nonNull(userId) && !userRepository.exists(userId)) {
            reasons.add(String.format("%s is missing", userId));
        }
        return reasons;
    }

    @Override
    protected UserGetCommand createSuccess() {
        return new UserGetCommand(userRepository, groupRepository, passwordProtector, name, userId);
    }

    @Override
    protected UserErrorCommand createError(List<String> reasons) {
        UserInput input = UserInput.builder(name).withUserId(userId).build();
        return new UserErrorCommand(UserCommandType.ID, input, reasons);
    }
}
