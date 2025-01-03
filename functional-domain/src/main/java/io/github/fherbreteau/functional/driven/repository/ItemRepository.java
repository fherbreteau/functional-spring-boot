package io.github.fherbreteau.functional.driven.repository;

import java.util.List;
import java.util.Optional;

import io.github.fherbreteau.functional.domain.entities.Folder;
import io.github.fherbreteau.functional.domain.entities.Item;
import io.github.fherbreteau.functional.domain.entities.User;

public interface ItemRepository {
    boolean exists(Folder parent, String name);

    boolean exists(Item item);

    <I extends Item> I create(I item);

    <I extends Item> I update(I item);

    List<Item> findByParentAndUser(Folder folder, User actor);

    Optional<Item> findByNameAndParentAndUser(String name, Folder folder, User actor);

    <I extends Item> void delete(I item);
}
