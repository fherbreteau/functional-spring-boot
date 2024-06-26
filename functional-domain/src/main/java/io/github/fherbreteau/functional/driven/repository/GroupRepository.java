package io.github.fherbreteau.functional.driven.repository;

import io.github.fherbreteau.functional.domain.entities.Group;

import java.util.UUID;

public interface GroupRepository {

    boolean exists(String name);

    Group findByName(String name);

    boolean exists(UUID groupId);

    Group findById(UUID groupId);

    Group create(Group group);

    Group update(Group group);

    void delete(Group group);
}
