package io.github.fherbreteau.functional.rules.check;

import static com.authzed.api.v1.CheckPermissionResponse.Permissionship.PERMISSIONSHIP_HAS_PERMISSION;
import static io.github.fherbreteau.functional.rules.Entities.*;
import static io.github.fherbreteau.functional.rules.check.Permissions.*;

import com.authzed.api.v1.*;
import com.authzed.api.v1.PermissionsServiceGrpc.PermissionsServiceBlockingStub;
import io.github.fherbreteau.functional.domain.entities.Item;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driven.rules.AccessChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccessCheckerImpl implements AccessChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessCheckerImpl.class);

    private final PermissionsServiceBlockingStub permissionsService;

    public AccessCheckerImpl(PermissionsServiceBlockingStub permissionsService) {
        this.permissionsService = permissionsService;
    }

    @Override
    public <T extends Item> boolean canRead(T item, User actor) {
        LOGGER.debug("Checking canRead({}, {})", item, actor);
        CheckPermissionRequest request = CheckPermissionRequest.newBuilder()
                .setPermission(READ)
                .setResource(ObjectReference.newBuilder()
                        .setObjectId(item.getHandle().toString())
                        .setObjectType(ITEM))
                .setSubject(SubjectReference.newBuilder()
                        .setObject(ObjectReference.newBuilder()
                                .setObjectId(actor.getUserId().toString())
                                .setObjectType(USER)))
                .build();
        try {
            CheckPermissionResponse response = permissionsService.checkPermission(request);
            return response.getPermissionship() == PERMISSIONSHIP_HAS_PERMISSION;
        } catch (Exception e) {
            LOGGER.error("Error while checking read permission on {} by {} ", item, actor, e);
            return false;
        }
    }

    @Override
    public <T extends Item> boolean canWrite(T item, User actor) {
        LOGGER.debug("Checking canWrite({}, {})", item, actor);
        CheckPermissionRequest request = CheckPermissionRequest.newBuilder()
                .setPermission(WRITE)
                .setResource(ObjectReference.newBuilder()
                        .setObjectId(item.getHandle().toString())
                        .setObjectType(ITEM))
                .setSubject(SubjectReference.newBuilder()
                        .setObject(ObjectReference.newBuilder()
                                .setObjectId(actor.getUserId().toString())
                                .setObjectType(USER)))
                .build();
        try {
            CheckPermissionResponse response = permissionsService.checkPermission(request);
            return response.getPermissionship() == PERMISSIONSHIP_HAS_PERMISSION;
        } catch (Exception e) {
            LOGGER.error("Error while checking write permission on {} by {} ", item, actor, e);
            return false;
        }
    }

    @Override
    public <T extends Item> boolean canExecute(T item, User actor) {
        LOGGER.debug("Checking canExecute({}, {})", item, actor);
        CheckPermissionRequest request = CheckPermissionRequest.newBuilder()
                .setPermission(EXECUTE)
                .setResource(ObjectReference.newBuilder()
                        .setObjectId(item.getHandle().toString())
                        .setObjectType(ITEM))
                .setSubject(SubjectReference.newBuilder()
                        .setObject(ObjectReference.newBuilder()
                                .setObjectId(actor.getUserId().toString())
                                .setObjectType(USER)))
                .build();
        try {
            CheckPermissionResponse response = permissionsService.checkPermission(request);
            return response.getPermissionship() == PERMISSIONSHIP_HAS_PERMISSION;
        } catch (Exception e) {
            LOGGER.error("Error while checking execute permission on {} by {} ", item, actor, e);
            return false;
        }
    }

    @Override
    public <T extends Item> boolean canChangeMode(T item, User actor) {
        LOGGER.debug("Checking canChangeMode({}, {})", item, actor);
        CheckPermissionRequest request = CheckPermissionRequest.newBuilder()
                .setPermission(CHANGE_MODE)
                .setResource(ObjectReference.newBuilder()
                        .setObjectId(item.getHandle().toString())
                        .setObjectType(ITEM))
                .setSubject(SubjectReference.newBuilder()
                        .setObject(ObjectReference.newBuilder()
                                .setObjectId(actor.getUserId().toString())
                                .setObjectType(USER)))
                .build();
        try {
            CheckPermissionResponse response = permissionsService.checkPermission(request);
            return response.getPermissionship() == PERMISSIONSHIP_HAS_PERMISSION;
        } catch (Exception e) {
            LOGGER.error("Error while checking change mode permission on {} by {} ", item, actor, e);
            return false;
        }
    }

    @Override
    public <T extends Item> boolean canChangeOwner(T item, User actor) {
        LOGGER.debug("Checking canChangeOwner({}, {})", item, actor);
        CheckPermissionRequest request = CheckPermissionRequest.newBuilder()
                .setPermission(CHANGE_OWNER)
                .setResource(ObjectReference.newBuilder()
                        .setObjectId(item.getHandle().toString())
                        .setObjectType(ITEM))
                .setSubject(SubjectReference.newBuilder()
                        .setObject(ObjectReference.newBuilder()
                                .setObjectId(actor.getUserId().toString())
                                .setObjectType(USER)))
                .build();
        try {
            CheckPermissionResponse response = permissionsService.checkPermission(request);
            return response.getPermissionship() == PERMISSIONSHIP_HAS_PERMISSION;
        } catch (Exception e) {
            LOGGER.error("Error while checking change owner permission on {} by {} ", item, actor, e);
            return false;
        }
    }

    @Override
    public <T extends Item> boolean canChangeGroup(T item, User actor) {
        LOGGER.debug("Checking canChangeGroup({}, {})", item, actor);
        CheckPermissionRequest request = CheckPermissionRequest.newBuilder()
                .setPermission(CHANGE_GROUP)
                .setResource(ObjectReference.newBuilder()
                        .setObjectId(item.getHandle().toString())
                        .setObjectType(ITEM))
                .setSubject(SubjectReference.newBuilder()
                        .setObject(ObjectReference.newBuilder()
                                .setObjectId(actor.getUserId().toString())
                                .setObjectType(USER)))
                .build();
        try {
            CheckPermissionResponse response = permissionsService.checkPermission(request);
            return response.getPermissionship() == PERMISSIONSHIP_HAS_PERMISSION;
        } catch (Exception e) {
            LOGGER.error("Error while checking change group permission on {} by {} ", item, actor, e);
            return false;
        }
    }
}
