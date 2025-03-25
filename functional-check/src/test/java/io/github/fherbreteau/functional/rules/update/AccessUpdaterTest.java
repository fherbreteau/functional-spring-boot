package io.github.fherbreteau.functional.rules.update;

import static com.authzed.api.v1.RelationshipUpdate.Operation.OPERATION_CREATE;
import static com.authzed.api.v1.RelationshipUpdate.Operation.OPERATION_DELETE;
import static io.github.fherbreteau.functional.rules.Entities.*;
import static io.github.fherbreteau.functional.rules.update.Relations.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.authzed.api.v1.PermissionsServiceGrpc.PermissionsServiceBlockingStub;
import com.authzed.api.v1.RelationshipUpdate;
import com.authzed.api.v1.WriteRelationshipsRequest;
import com.authzed.api.v1.WriteRelationshipsResponse;
import com.authzed.api.v1.ZedToken;
import io.github.fherbreteau.functional.domain.entities.*;
import io.github.fherbreteau.functional.rules.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessUpdaterTest {

    @Mock
    private PermissionsServiceBlockingStub permissionsService;

    @InjectMocks
    private AccessUpdaterImpl accessUpdater;

    @Captor
    private ArgumentCaptor<WriteRelationshipsRequest> requestCaptor;

    private UUID fileHandle;

    private Item item;

    private UUID userId;

    private UUID groupId;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
        User owner = User.builder("user")
                .withUserId(userId)
                .build();
        groupId = UUID.randomUUID();
        Group group = Group.builder("group")
                .withGroupId(groupId)
                .build();

        fileHandle = UUID.randomUUID();
        item = File.builder()
                .withName("file")
                .withHandle(fileHandle)
                .withParent(Folder.getRoot())
                .withOwner(owner)
                .withGroup(group)
                .withOwnerAccess(AccessRight.full())
                .withGroupAccess(AccessRight.readOnly())
                .withOtherAccess(AccessRight.writeExecute())
                .build();
    }

    @Test
    void shouldPublishToSpiceDbWhenCreatingItem() {
        // Arrange
        when(permissionsService.writeRelationships(any()))
                .thenReturn(WriteRelationshipsResponse.newBuilder()
                        .setWrittenAt(ZedToken.newBuilder()
                                .setToken("token"))
                        .build());
        // Act
        Item result = accessUpdater.createItem(item);
        // Assert
        assertThat(result).isEqualTo(item);
        verify(permissionsService).writeRelationships(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .extracting(WriteRelationshipsRequest::getUpdatesList, list(RelationshipUpdate.class))
                .hasSize(6)
                .extracting(RelationshipUpdate::getOperation, Utils::getRelation, Utils::getSubjectType, Utils::getSubjectId, Utils::getResourceType, Utils::getResourceId)
                .containsExactly(
                        tuple(OPERATION_CREATE, OWNER_READ, USER, userId.toString(), ITEM, fileHandle.toString()),
                        tuple(OPERATION_CREATE, OWNER_WRITE, USER, userId.toString(), ITEM, fileHandle.toString()),
                        tuple(OPERATION_CREATE, OWNER_EXECUTE, USER, userId.toString(), ITEM, fileHandle.toString()),
                        tuple(OPERATION_CREATE, GROUP_READ, GROUP, groupId.toString(), ITEM, fileHandle.toString()),
                        tuple(OPERATION_CREATE, OTHER_WRITE, USER, OTHER_ID, ITEM, fileHandle.toString()),
                        tuple(OPERATION_CREATE, OTHER_EXECUTE, USER, OTHER_ID, ITEM, fileHandle.toString()));

    }

    @Test
    void shouldHandleErrorFromSpiceDbWhenCreatingItem() {
        // Arrange
        when(permissionsService.writeRelationships(any()))
                .thenThrow(RuntimeException.class);
        // Act
        Item result = accessUpdater.createItem(item);
        // Assert
        assertThat(result).isEqualTo(item);
    }

    @Test
    void shouldPublishToSpiceDbWhenUpdatingOwner() {
        // Arrange
        when(permissionsService.writeRelationships(any()))
                .thenReturn(WriteRelationshipsResponse.newBuilder()
                        .setWrittenAt(ZedToken.newBuilder()
                                .setToken("token"))
                        .build());
        UUID oldUserId = UUID.randomUUID();
        User oldOwner = User.builder("oldUser").withUserId(oldUserId).build();
        // Act
        Item result = accessUpdater.updateOwner(item, oldOwner);
        // Assert
        assertThat(result).isEqualTo(item);
        verify(permissionsService).writeRelationships(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .extracting(WriteRelationshipsRequest::getUpdatesList, list(RelationshipUpdate.class))
                .hasSize(6)
                .extracting(RelationshipUpdate::getOperation, Utils::getRelation, Utils::getSubjectType, Utils::getSubjectId, Utils::getResourceType, Utils::getResourceId)
                .containsExactly(
                        tuple(OPERATION_DELETE, OWNER_READ, USER, oldUserId.toString(), ITEM, fileHandle.toString()),
                        tuple(OPERATION_DELETE, OWNER_WRITE, USER, oldUserId.toString(), ITEM, fileHandle.toString()),
                        tuple(OPERATION_DELETE, OWNER_EXECUTE, USER, oldUserId.toString(), ITEM, fileHandle.toString()),
                        tuple(OPERATION_CREATE, OWNER_READ, USER, userId.toString(), ITEM, fileHandle.toString()),
                        tuple(OPERATION_CREATE, OWNER_WRITE, USER, userId.toString(), ITEM, fileHandle.toString()),
                        tuple(OPERATION_CREATE, OWNER_EXECUTE, USER, userId.toString(), ITEM, fileHandle.toString()));
    }

    @Test
    void shouldHandleErrorFromSpiceDbWhenUpdatingOwner() {
        // Arrange
        when(permissionsService.writeRelationships(any()))
                .thenThrow(RuntimeException.class);
        UUID oldUserId = UUID.randomUUID();
        User oldOwner = User.builder("oldUser").withUserId(oldUserId).build();
        // Act
        Item result = accessUpdater.updateOwner(item, oldOwner);
        // Assert
        assertThat(result).isEqualTo(item);
    }

    @Test
    void shouldPublishToSpiceDbWhenUpdatingGroup() {
        // Arrange
        when(permissionsService.writeRelationships(any()))
                .thenReturn(WriteRelationshipsResponse.newBuilder()
                        .setWrittenAt(ZedToken.newBuilder()
                                .setToken("token"))
                        .build());
        UUID oldGroupId = UUID.randomUUID();
        Group oldGroup = Group.builder("oldGroup").withGroupId(oldGroupId).build();
        // Act
        Item result = accessUpdater.updateGroup(item, oldGroup);
        // Assert
        assertThat(result).isEqualTo(item);
        verify(permissionsService).writeRelationships(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .extracting(WriteRelationshipsRequest::getUpdatesList, list(RelationshipUpdate.class))
                .hasSize(2)
                .extracting(RelationshipUpdate::getOperation, Utils::getRelation, Utils::getSubjectType, Utils::getSubjectId, Utils::getResourceType, Utils::getResourceId)
                .containsExactly(
                        tuple(OPERATION_DELETE, GROUP_READ, GROUP, oldGroupId.toString(), ITEM, fileHandle.toString()),
                        tuple(OPERATION_CREATE, GROUP_READ, GROUP, groupId.toString(), ITEM, fileHandle.toString()));
    }

    @Test
    void shouldHandleErrorFromSpiceDbWhenUpdatingGroup() {
        // Arrange
        when(permissionsService.writeRelationships(any()))
                .thenThrow(RuntimeException.class);
        UUID oldGroupId = UUID.randomUUID();
        Group oldGroup = Group.builder("oldGroup").withGroupId(oldGroupId).build();
        // Act
        Item result = accessUpdater.updateGroup(item, oldGroup);
        // Assert
        assertThat(result).isEqualTo(item);
    }

    @Test
    void shouldPublishToSpiceDbWhenUpdatingOwnerAccess() {
        // Arrange
        when(permissionsService.writeRelationships(any()))
                .thenReturn(WriteRelationshipsResponse.newBuilder()
                        .setWrittenAt(ZedToken.newBuilder()
                                .setToken("token"))
                        .build());
        AccessRight oldAccess = AccessRight.none();
        // Act
        Item result = accessUpdater.updateOwnerAccess(item, oldAccess);
        // Assert
        assertThat(result).isEqualTo(item);
        verify(permissionsService).writeRelationships(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .extracting(WriteRelationshipsRequest::getUpdatesList, list(RelationshipUpdate.class))
                .hasSize(3)
                .extracting(RelationshipUpdate::getOperation, Utils::getRelation, Utils::getSubjectType, Utils::getSubjectId, Utils::getResourceType, Utils::getResourceId)
                .containsExactly(
                        tuple(OPERATION_CREATE, OWNER_READ, USER, userId.toString(), ITEM, fileHandle.toString()),
                        tuple(OPERATION_CREATE, OWNER_WRITE, USER, userId.toString(), ITEM, fileHandle.toString()),
                        tuple(OPERATION_CREATE, OWNER_EXECUTE, USER, userId.toString(), ITEM, fileHandle.toString()));
    }

    @Test
    void shouldHandleErrorFromSpiceDbWhenUpdatingOwnerAccess() {
        // Arrange
        when(permissionsService.writeRelationships(any()))
                .thenThrow(RuntimeException.class);
        AccessRight oldAccess = AccessRight.none();
        // Act
        Item result = accessUpdater.updateOwnerAccess(item, oldAccess);
        // Assert
        assertThat(result).isEqualTo(item);
    }

    @Test
    void shouldPublishToSpiceDbWhenUpdatingGroupAccess() {
        // Arrange
        when(permissionsService.writeRelationships(any()))
                .thenReturn(WriteRelationshipsResponse.newBuilder()
                        .setWrittenAt(ZedToken.newBuilder()
                                .setToken("token"))
                        .build());
        AccessRight oldAccess = AccessRight.full();
        // Act
        Item result = accessUpdater.updateGroupAccess(item, oldAccess);
        // Assert
        assertThat(result).isEqualTo(item);
        verify(permissionsService).writeRelationships(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .extracting(WriteRelationshipsRequest::getUpdatesList, list(RelationshipUpdate.class))
                .hasSize(2)
                .extracting(RelationshipUpdate::getOperation, Utils::getRelation, Utils::getSubjectType, Utils::getSubjectId, Utils::getResourceType, Utils::getResourceId)
                .containsExactly(
                        tuple(OPERATION_DELETE, GROUP_WRITE, GROUP, groupId.toString(), ITEM, fileHandle.toString()),
                        tuple(OPERATION_DELETE, GROUP_EXECUTE, GROUP, groupId.toString(), ITEM, fileHandle.toString()));
    }

    @Test
    void shouldHandleErrorFromSpiceDbWhenUpdatingGroupAccess() {
        // Arrange
        when(permissionsService.writeRelationships(any()))
                .thenThrow(RuntimeException.class);
        AccessRight oldAccess = AccessRight.none();
        // Act
        Item result = accessUpdater.updateGroupAccess(item, oldAccess);
        // Assert
        assertThat(result).isEqualTo(item);
    }

    @Test
    void shouldPublishToSpiceDbWhenUpdatingOtherAccess() {
        // Arrange
        when(permissionsService.writeRelationships(any()))
                .thenReturn(WriteRelationshipsResponse.newBuilder()
                        .setWrittenAt(ZedToken.newBuilder()
                                .setToken("token"))
                        .build());
        AccessRight oldAccess = AccessRight.full();
        // Act
        Item result = accessUpdater.updateOtherAccess(item, oldAccess);
        // Assert
        assertThat(result).isEqualTo(item);
        verify(permissionsService).writeRelationships(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .extracting(WriteRelationshipsRequest::getUpdatesList, list(RelationshipUpdate.class))
                .singleElement()
                .extracting(RelationshipUpdate::getOperation, Utils::getRelation, Utils::getSubjectType, Utils::getSubjectId, Utils::getResourceType, Utils::getResourceId)
                .containsExactly(OPERATION_DELETE, OTHER_READ, USER, OTHER_ID, ITEM, fileHandle.toString());
    }

    @Test
    void shouldHandleErrorFromSpiceDbWhenUpdatingOtherAccess() {
        // Arrange
        when(permissionsService.writeRelationships(any()))
                .thenThrow(RuntimeException.class);
        AccessRight oldAccess = AccessRight.none();
        // Act
        Item result = accessUpdater.updateOtherAccess(item, oldAccess);
        // Assert
        assertThat(result).isEqualTo(item);
    }

    @Test
    void shouldPublishToSpiceDbWhenDeletingItem() {
        // Arrange
        when(permissionsService.writeRelationships(any()))
                .thenReturn(WriteRelationshipsResponse.newBuilder()
                        .setWrittenAt(ZedToken.newBuilder()
                                .setToken("token"))
                        .build());
        // Act
        accessUpdater.deleteItem(item);
        // Assert
        verify(permissionsService).writeRelationships(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .extracting(WriteRelationshipsRequest::getUpdatesList, list(RelationshipUpdate.class))
                .hasSize(6)
                .extracting(RelationshipUpdate::getOperation, Utils::getRelation, Utils::getSubjectType, Utils::getSubjectId, Utils::getResourceType, Utils::getResourceId)
                .containsExactly(
                        tuple(OPERATION_DELETE, OWNER_READ, USER, userId.toString(), ITEM, fileHandle.toString()),
                        tuple(OPERATION_DELETE, OWNER_WRITE, USER, userId.toString(), ITEM, fileHandle.toString()),
                        tuple(OPERATION_DELETE, OWNER_EXECUTE, USER, userId.toString(), ITEM, fileHandle.toString()),
                        tuple(OPERATION_DELETE, GROUP_READ, GROUP, groupId.toString(), ITEM, fileHandle.toString()),
                        tuple(OPERATION_DELETE, OTHER_WRITE, USER, OTHER_ID, ITEM, fileHandle.toString()),
                        tuple(OPERATION_DELETE, OTHER_EXECUTE, USER, OTHER_ID, ITEM, fileHandle.toString()));
    }

    @Test
    void shouldHandleErrorFromSpiceDbWhenDeletingItem() {
        // Arrange
        when(permissionsService.writeRelationships(any()))
                .thenThrow(RuntimeException.class);
        // Act
        accessUpdater.createItem(item);
        // Assert
        verify(permissionsService).writeRelationships(any());
    }
}
