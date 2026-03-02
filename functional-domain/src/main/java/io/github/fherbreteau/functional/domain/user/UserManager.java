package io.github.fherbreteau.functional.domain.user;

import static java.lang.String.format;

import java.util.Objects;

import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.Output;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driven.repository.GroupRepository;
import io.github.fherbreteau.functional.driven.repository.UserRepository;
import io.github.fherbreteau.functional.driven.rules.UserUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserManager {
    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    private final UserRepository userRepository;
    private final UserUpdater userUpdater;
    private final GroupRepository groupRepository;

    public UserManager(UserRepository userRepository, UserUpdater userUpdater, GroupRepository groupRepository) {
        this.userRepository = userRepository;
        this.userUpdater = userUpdater;
        this.groupRepository = groupRepository;
    }

    public Output<User> findUserByName(String name) {
        if (userRepository.exists(name)) {
            return Output.success(userRepository.findByName(name));
        } else {
            return Output.failure(format("%s not found", name));
        }
    }

    public Output<Group> findGroupByName(String name) {
        if (groupRepository.exists(name)) {
            return Output.success(groupRepository.findByName(name));
        } else {
            return Output.failure(format("%s not found", name));
        }
    }

    public Output<String> getPassword(User user) {
        logger.debug("Getting password for {}", user);
        if (userRepository.exists(user.getUserId())) {
            String password = userRepository.getPassword(user);
            if (Objects.nonNull(password) && !password.isEmpty()) {
                return Output.success(password);
            }
            return Output.failure(format("Password for user %s not found", user.getName()));
        }
        return Output.failure(format("User %s not found", user.getName()));
    }

    public void createRootUser(User user) {
        logger.debug("Initializing user {}", user);
        userUpdater.createGroup(user.getGroup());
        userUpdater.createUser(user);
    }
}
