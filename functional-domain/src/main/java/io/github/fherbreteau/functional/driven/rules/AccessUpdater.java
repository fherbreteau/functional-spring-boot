package io.github.fherbreteau.functional.driven.rules;

import io.github.fherbreteau.functional.domain.entities.AccessRight;
import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.Item;
import io.github.fherbreteau.functional.domain.entities.User;

public interface AccessUpdater {
    <I extends Item> I createItem(I item);

    <I extends Item> I updateOwner(I item, User oldOwner);

    <I extends Item> I updateGroup(I item, Group oldGroup);

    <I extends Item> I updateOwnerAccess(I item, AccessRight oldOwnerAccess);

    <I extends Item> I updateGroupAccess(I item, AccessRight oldGroupAccess);

    <I extends Item> I updateOtherAccess(I item, AccessRight oldOtherAccess);

    <I extends Item> void deleteItem(I item);
}
