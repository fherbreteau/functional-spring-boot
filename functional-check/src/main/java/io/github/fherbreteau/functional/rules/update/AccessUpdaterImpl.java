package io.github.fherbreteau.functional.rules.update;

import static io.github.fherbreteau.functional.rules.Entities.*;
import static io.github.fherbreteau.functional.rules.check.Permissions.READ;
import static io.github.fherbreteau.functional.rules.check.Permissions.WRITE;
import static io.github.fherbreteau.functional.rules.check.Permissions.EXECUTE;
import static io.github.fherbreteau.functional.rules.update.Relations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.authzed.api.v1.*;
import com.authzed.api.v1.PermissionsServiceGrpc.PermissionsServiceBlockingStub;
import io.github.fherbreteau.functional.domain.entities.AccessRight;
import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.Item;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driven.rules.AccessUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccessUpdaterImpl implements AccessUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessUpdaterImpl.class);

    private final PermissionsServiceBlockingStub permissionsService;

    public AccessUpdaterImpl(PermissionsServiceBlockingStub permissionsService) {
        this.permissionsService = permissionsService;
    }

    @Override
    public <I extends Item> I createItem(I item) {
        LOGGER.debug("Create access for item {}", item);
        UUID itemHandle = item.getHandle();
        UUID ownerId = item.getOwner().getUserId();
        UUID groupId = item.getGroup().getGroupId();
        List<RelationshipUpdate> updates = new ArrayList<>();
        createAccess(OWNER, itemHandle, item.getOwnerAccess(), ownerId, updates);
        createAccess(GROUP, itemHandle, item.getGroupAccess(), groupId, updates);
        createAccess(OTHER, itemHandle, item.getOtherAccess(), null, updates);
        String token = publishRelations(updates);
        LOGGER.info("Item {} created at {}", item, token);
        return item;
    }

    @Override
    public <I extends Item> I updateOwner(I item, User oldOwner) {
        LOGGER.debug("Update owner access for item {} from owner {}", item, oldOwner);
        UUID itemHandle = item.getHandle();
        UUID oldOwnerId = oldOwner.getUserId();
        UUID ownerId = item.getOwner().getUserId();
        List<RelationshipUpdate> updates = new ArrayList<>();
        deleteAccess(OWNER, itemHandle, item.getOwnerAccess(), oldOwnerId, updates);
        createAccess(OWNER, itemHandle, item.getOwnerAccess(), ownerId, updates);
        String token = publishRelations(updates);
        LOGGER.info("Item {} owner updated at {}", item, token);
        return item;
    }

    @Override
    public <I extends Item> I updateGroup(I item, Group oldGroup) {
        LOGGER.debug("Update group access for item {} from group {}", item, oldGroup);
        UUID itemHandle = item.getHandle();
        UUID oldGroupId = oldGroup.getGroupId();
        UUID groupId = item.getGroup().getGroupId();
        List<RelationshipUpdate> updates = new ArrayList<>();
        deleteAccess(GROUP, itemHandle, item.getGroupAccess(), oldGroupId, updates);
        createAccess(GROUP, itemHandle, item.getGroupAccess(), groupId, updates);
        String token = publishRelations(updates);
        LOGGER.info("Item {} group updated at {}", item, token);
        return item;
    }

    @Override
    public <I extends Item> I updateOwnerAccess(I item, AccessRight oldOwnerAccess) {
        LOGGER.debug("Update owner access for item {} from {}", item, oldOwnerAccess);
        UUID itemHandle = item.getHandle();
        UUID ownerId = item.getOwner().getUserId();
        List<RelationshipUpdate> updates = new ArrayList<>();
        updateAccess(OWNER, itemHandle, oldOwnerAccess, item.getOwnerAccess(), ownerId, updates);
        String token = publishRelations(updates);
        LOGGER.info("Item {} access owner updated at {}", item, token);
        return item;
    }

    @Override
    public <I extends Item> I updateGroupAccess(I item, AccessRight oldGroupAccess) {
        LOGGER.debug("Update group access for item {} from {}", item, oldGroupAccess);
        UUID itemHandle = item.getHandle();
        UUID groupId = item.getGroup().getGroupId();
        List<RelationshipUpdate> updates = new ArrayList<>();
        updateAccess(GROUP, itemHandle, oldGroupAccess, item.getGroupAccess(), groupId, updates);
        String token = publishRelations(updates);
        LOGGER.info("Item {} access group updated at {}", item, token);
        return item;
    }

    @Override
    public <I extends Item> I updateOtherAccess(I item, AccessRight oldOtherAccess) {
        LOGGER.debug("Update other access for item {} from {}", item, oldOtherAccess);
        UUID itemHandle = item.getHandle();
        List<RelationshipUpdate> updates = new ArrayList<>();
        updateAccess(OTHER, itemHandle, oldOtherAccess, item.getOtherAccess(), null, updates);
        String token = publishRelations(updates);
        LOGGER.info("Item {} access other updated at {}", item, token);
        return item;
    }

    @Override
    public <I extends Item> void deleteItem(I item) {
        LOGGER.debug("Delete access for item {}", item);
        UUID itemHandle = item.getHandle();
        UUID ownerId = item.getOwner().getUserId();
        UUID groupId = item.getGroup().getGroupId();
        List<RelationshipUpdate> updates = new ArrayList<>();
        deleteAccess(OWNER, itemHandle, item.getOwnerAccess(), ownerId, updates);
        deleteAccess(GROUP, itemHandle, item.getGroupAccess(), groupId, updates);
        deleteAccess(OTHER, itemHandle, item.getOtherAccess(), null, updates);
        String token = publishRelations(updates);
        LOGGER.info("Item {} deleted at {}", item, token);
    }

    private void createAccess(String relationType, UUID itemId, AccessRight accessRight, UUID subject, List<RelationshipUpdate> updates) {
        String subjectType = Objects.equals(relationType, GROUP) ? GROUP : USER;
        String subjectId = Objects.isNull(subject) ? OTHER_ID : subject.toString();
        if (accessRight.isRead()) {
            createRelation(getRelationName(relationType, READ), subjectType, subjectId, itemId.toString(), updates);
        }
        if (accessRight.isWrite()) {
            createRelation(getRelationName(relationType, WRITE), subjectType, subjectId, itemId.toString(), updates);
        }
        if (accessRight.isExecute()) {
            createRelation(getRelationName(relationType, EXECUTE), subjectType, subjectId, itemId.toString(), updates);
        }
    }

    private void updateAccess(String relationType, UUID itemId, AccessRight oldAccessRight, AccessRight newAccessRight, UUID subject, List<RelationshipUpdate> updates) {
        String subjectType = Objects.equals(relationType, GROUP) ? GROUP : USER;
        String subjectId = Objects.isNull(subject) ? OTHER_ID : subject.toString();
        updateRelation(getRelationName(relationType, READ), subjectType, subjectId, itemId.toString(), oldAccessRight.isRead(), newAccessRight.isRead(), updates);
        updateRelation(getRelationName(relationType, WRITE), subjectType, subjectId, itemId.toString(), oldAccessRight.isWrite(), newAccessRight.isWrite(), updates);
        updateRelation(getRelationName(relationType, EXECUTE), subjectType, subjectId, itemId.toString(), oldAccessRight.isExecute(), newAccessRight.isExecute(), updates);
    }

    private void deleteAccess(String relationType, UUID itemId, AccessRight accessRight, UUID subject, List<RelationshipUpdate> updates) {
        String subjectType = Objects.equals(relationType, GROUP) ? GROUP : USER;
        String subjectId = Objects.isNull(subject) ? OTHER_ID : subject.toString();
        if (accessRight.isRead()) {
            deleteRelation(getRelationName(relationType, READ), subjectType, subjectId, itemId.toString(), updates);
        }
        if (accessRight.isWrite()) {
            deleteRelation(getRelationName(relationType, WRITE), subjectType, subjectId, itemId.toString(), updates);
        }
        if (accessRight.isExecute()) {
            deleteRelation(getRelationName(relationType, EXECUTE), subjectType, subjectId, itemId.toString(), updates);
        }
    }

    private String getRelationName(String relationType, String operation) {
        return String.format("%s_%s", relationType, operation);
    }

    private void updateRelation(String relation, String subjectType, String subjectId, String itemId, boolean oldValue, boolean newValue, List<RelationshipUpdate> updates) {
        if (oldValue != newValue) {
            if (oldValue) {
                deleteRelation(relation, subjectType, subjectId, itemId, updates);
            } else {
                createRelation(relation, subjectType, subjectId, itemId, updates);
            }
        }
    }

    private void createRelation(String relation, String subjectType, String subjectId, String resourceId, List<RelationshipUpdate> updates) {
        RelationshipUpdate update = RelationshipUpdate.newBuilder()
                .setOperation(RelationshipUpdate.Operation.OPERATION_CREATE)
                .setRelationship(Relationship.newBuilder()
                        .setRelation(relation)
                        .setResource(ObjectReference.newBuilder()
                                .setObjectType(ITEM)
                                .setObjectId(resourceId))
                        .setSubject(SubjectReference.newBuilder()
                                .setObject(ObjectReference.newBuilder()
                                        .setObjectType(subjectType)
                                        .setObjectId(subjectId))))
                .build();
        updates.add(update);
    }

    private void deleteRelation(String relation, String subjectType, String subjectId, String resourceId, List<RelationshipUpdate> updates) {
        RelationshipUpdate update = RelationshipUpdate.newBuilder()
                .setOperation(RelationshipUpdate.Operation.OPERATION_DELETE)
                .setRelationship(Relationship.newBuilder()
                        .setRelation(relation)
                        .setResource(ObjectReference.newBuilder()
                                .setObjectType(ITEM)
                                .setObjectId(resourceId))
                        .setSubject(SubjectReference.newBuilder()
                                .setObject(ObjectReference.newBuilder()
                                        .setObjectType(subjectType)
                                        .setObjectId(subjectId))))
                .build();
        updates.add(update);
    }

    private String publishRelations(List<RelationshipUpdate> relations) {
        WriteRelationshipsRequest request = WriteRelationshipsRequest.newBuilder()
                .addAllUpdates(relations)
                .build();
        try {
            WriteRelationshipsResponse response = permissionsService.writeRelationships(request);
            return response.getWrittenAt().getToken();
        } catch (Exception e) {
            LOGGER.error("Error while publishing relations", e);
            return null;
        }
    }
}
