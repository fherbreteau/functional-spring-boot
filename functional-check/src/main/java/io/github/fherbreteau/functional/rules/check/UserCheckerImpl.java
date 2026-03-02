package io.github.fherbreteau.functional.rules.check;

import static com.authzed.api.v1.CheckPermissionResponse.Permissionship.PERMISSIONSHIP_HAS_PERMISSION;
import static io.github.fherbreteau.functional.rules.Entities.*;
import static io.github.fherbreteau.functional.rules.check.Permissions.*;

import java.util.UUID;

import com.authzed.api.v1.*;
import com.authzed.api.v1.PermissionsServiceGrpc.PermissionsServiceBlockingStub;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.driven.rules.UserChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserCheckerImpl implements UserChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserCheckerImpl.class);

    private final PermissionsServiceBlockingStub permissionsService;

    public UserCheckerImpl(PermissionsServiceBlockingStub permissionsService) {
        this.permissionsService = permissionsService;
    }

    @Override
    public boolean canCreateUser(String name, User actor) {
        LOGGER.debug("Checking canCreateUser({}, {})", name, actor);
        CheckPermissionRequest request = createRequest(CREATE, actor.getUserId());
        try {
            CheckPermissionResponse response = permissionsService.checkPermission(request);
            return response.getPermissionship() == PERMISSIONSHIP_HAS_PERMISSION;
        } catch (Exception e) {
            LOGGER.error("Error while checking create user permission on {} by {} ", name, actor, e);
            return false;
        }
    }

    @Override
    public boolean canUpdateUser(String name, User actor) {
        LOGGER.debug("Checking canUpdateUser({}, {})", name, actor);
        CheckPermissionRequest request = createRequest(UPDATE, actor.getUserId());
        try {
            CheckPermissionResponse response = permissionsService.checkPermission(request);
            return response.getPermissionship() == PERMISSIONSHIP_HAS_PERMISSION;
        } catch (Exception e) {
            LOGGER.error("Error while checking update user permission on {} by {} ", name, actor, e);
            return false;
        }
    }

    @Override
    public boolean canDeleteUser(String name, User actor) {
        LOGGER.debug("Checking canDeleteUser({}, {})", name, actor);
        CheckPermissionRequest request = createRequest(DELETE, actor.getUserId());
        try {
            CheckPermissionResponse response = permissionsService.checkPermission(request);
            return response.getPermissionship() == PERMISSIONSHIP_HAS_PERMISSION;
        } catch (Exception e) {
            LOGGER.error("Error while checking delete user permission on {} by {} ", name, actor, e);
            return false;
        }
    }

    @Override
    public boolean canCreateGroup(String name, User actor) {
        LOGGER.debug("Checking canCreateGroup({}, {})", name, actor);
        CheckPermissionRequest request = createRequest(CREATE, actor.getUserId());
        try {
            CheckPermissionResponse response = permissionsService.checkPermission(request);
            return response.getPermissionship() == PERMISSIONSHIP_HAS_PERMISSION;
        } catch (Exception e) {
            LOGGER.error("Error while checking create group permission on {} by {} ", name, actor, e);
            return false;
        }
    }

    @Override
    public boolean canUpdateGroup(String name, User actor) {
        LOGGER.debug("Checking canUpdateGroup({}, {})", name, actor);
        CheckPermissionRequest request = createRequest(UPDATE, actor.getUserId());
        try {
            CheckPermissionResponse response = permissionsService.checkPermission(request);
            return response.getPermissionship() == PERMISSIONSHIP_HAS_PERMISSION;
        } catch (Exception e) {
            LOGGER.error("Error while checking update group permission on {} by {} ", name, actor, e);
            return false;
        }
    }

    @Override
    public boolean canDeleteGroup(String name, User actor) {
        LOGGER.debug("Checking canDeleteGroup({}, {})", name, actor);
        CheckPermissionRequest request = createRequest(DELETE, actor.getUserId());
        try {
            CheckPermissionResponse response = permissionsService.checkPermission(request);
            return response.getPermissionship() == PERMISSIONSHIP_HAS_PERMISSION;
        } catch (Exception e) {
            LOGGER.error("Error while checking delete group permission on {} by {} ", name, actor, e);
            return false;
        }
    }

    private CheckPermissionRequest createRequest(String permission, UUID userId) {
        return CheckPermissionRequest.newBuilder()
                .setPermission(permission)
                .setResource(ObjectReference.newBuilder()
                        .setObjectId(userId.toString())
                        .setObjectType(USER))
                .setSubject(SubjectReference.newBuilder()
                        .setObject(ObjectReference.newBuilder()
                                .setObjectId(userId.toString())
                                .setObjectType(USER)))
                .build();
    }
}
