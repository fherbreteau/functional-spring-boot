package io.github.fherbreteau.functional.rules.update;

import static com.authzed.api.v1.LookupPermissionship.LOOKUP_PERMISSIONSHIP_HAS_PERMISSION;
import static com.authzed.api.v1.RelationshipUpdate.Operation.OPERATION_CREATE;
import static com.authzed.api.v1.RelationshipUpdate.Operation.OPERATION_DELETE;
import static io.github.fherbreteau.functional.rules.Entities.GROUP;
import static io.github.fherbreteau.functional.rules.Entities.USER;
import static io.github.fherbreteau.functional.rules.update.Relations.ADMIN;
import static io.github.fherbreteau.functional.rules.update.Relations.MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import com.authzed.api.v1.*;
import com.authzed.api.v1.PermissionsServiceGrpc.PermissionsServiceBlockingStub;
import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.rules.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserUpdaterTest {

    @Mock
    private PermissionsServiceBlockingStub permissionsService;

    @InjectMocks
    private UserUpdaterImpl userUpdater;

    @Captor
    private ArgumentCaptor<WriteRelationshipsRequest> requestCaptor;

    @Captor
    private ArgumentCaptor<LookupSubjectsRequest> lookupCaptor;

    @Captor
    private ArgumentCaptor<DeleteRelationshipsRequest> deleteCaptor;

    private UUID userId;

    private User user;

    private UUID groupId;

    private Group group;

    @BeforeEach
    void setup() {
        groupId = UUID.randomUUID();
        group = Group.builder("group")
                .withGroupId(groupId)
                .build();
        userId = UUID.randomUUID();
        user = User.builder("user")
                .withUserId(userId)
                .addGroups(List.of(group))
                .build();
    }

    @Test
    void shouldPublishToSpiceDbWhenCreatingUser() {
        // Arrange
        when(permissionsService.writeRelationships(any()))
                .thenReturn(WriteRelationshipsResponse.newBuilder()
                        .setWrittenAt(ZedToken.newBuilder()
                                .setToken("token"))
                        .build());

        // Act
        User result = userUpdater.createUser(user);

        // Assert
        assertThat(result).isEqualTo(user);
        verify(permissionsService).writeRelationships(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .extracting(WriteRelationshipsRequest::getUpdatesList, list(RelationshipUpdate.class))
                .singleElement()
                .extracting(RelationshipUpdate::getOperation, Utils::getRelation, Utils::getSubjectType, Utils::getSubjectId, Utils::getResourceType, Utils::getResourceId)
                .containsExactly(OPERATION_CREATE, MEMBER, USER, userId.toString(), GROUP, groupId.toString());
    }

    @Test
    void shouldPublishToSpiceDbWhenCreatingRoot() {
        // Arrange
        when(permissionsService.writeRelationships(any()))
                .thenReturn(WriteRelationshipsResponse.newBuilder()
                        .setWrittenAt(ZedToken.newBuilder()
                                .setToken("token"))
                        .build());
        User root = User.root();
        UUID rootId = root.getUserId();

        // Act
        User result = userUpdater.createUser(root);

        // Assert
        assertThat(result).isEqualTo(root);
        verify(permissionsService).writeRelationships(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .extracting(WriteRelationshipsRequest::getUpdatesList, list(RelationshipUpdate.class))
                .hasSize(2)
                .extracting(RelationshipUpdate::getOperation, Utils::getRelation, Utils::getSubjectType, Utils::getSubjectId, Utils::getResourceType, Utils::getResourceId)
                .containsExactly(
                        tuple(OPERATION_CREATE, ADMIN, USER, rootId.toString(), USER, rootId.toString()),
                        tuple(OPERATION_CREATE, MEMBER, USER, rootId.toString(), GROUP, rootId.toString()));
    }

    @Test
    void shouldHandleErrorFromSpiceDbWhenCreatingUser() {
        // Arrange
        when(permissionsService.writeRelationships(any()))
                .thenThrow(RuntimeException.class);
        // Act
        userUpdater.createUser(user);
        // Assert
        verify(permissionsService).writeRelationships(any());
    }

    @Test
    void shouldPublishToSpiceDbWhenUpdatingUserWithIdChanged() {
        // Arrange
        when(permissionsService.writeRelationships(any()))
                .thenReturn(WriteRelationshipsResponse.newBuilder()
                        .setWrittenAt(ZedToken.newBuilder()
                                .setToken("token"))
                        .build());
        UUID oldUserId = UUID.randomUUID();
        User oldUser = user.copy().withUserId(oldUserId).build();

        // Act
        User result = userUpdater.updateUser(oldUser, user);

        // Assert
        assertThat(result).isEqualTo(user);
        verify(permissionsService).writeRelationships(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .extracting(WriteRelationshipsRequest::getUpdatesList, list(RelationshipUpdate.class))
                .hasSize(2)
                .extracting(RelationshipUpdate::getOperation, Utils::getRelation, Utils::getSubjectType, Utils::getSubjectId, Utils::getResourceType, Utils::getResourceId)
                .containsExactly(
                        tuple(OPERATION_DELETE, MEMBER, USER, oldUserId.toString(), GROUP, groupId.toString()),
                        tuple(OPERATION_CREATE, MEMBER, USER, userId.toString(), GROUP, groupId.toString()));
    }

    @Test
    void shouldPublishToSpiceDbWhenUpdatingUserWithGroupChange() {
        // Arrange
        when(permissionsService.writeRelationships(any()))
                .thenReturn(WriteRelationshipsResponse.newBuilder()
                        .setWrittenAt(ZedToken.newBuilder()
                                .setToken("token"))
                        .build());
        UUID oldGroupId = UUID.randomUUID();
        Group oldGroup = Group.builder("oldGroup").withGroupId(oldGroupId).build();
        User oldUser = user.copy().withGroups(List.of(group, oldGroup)).build();

        // Act
        User result = userUpdater.updateUser(oldUser, user);

        // Assert
        assertThat(result).isEqualTo(user);
        verify(permissionsService).writeRelationships(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .extracting(WriteRelationshipsRequest::getUpdatesList, list(RelationshipUpdate.class))
                .singleElement()
                .extracting(RelationshipUpdate::getOperation, Utils::getRelation, Utils::getSubjectType, Utils::getSubjectId, Utils::getResourceType, Utils::getResourceId)
                .containsExactly(OPERATION_DELETE, MEMBER, USER, userId.toString(), GROUP, oldGroupId.toString());
    }

    @Test
    void shouldHandleErrorFromSpiceDbWhenUpdatingUser() {
        // Arrange
        when(permissionsService.writeRelationships(any()))
                .thenThrow(RuntimeException.class);
        // Act
        userUpdater.updateUser(user.copy().build(), user);
        // Assert
        verify(permissionsService).writeRelationships(any());
    }

    @Test
    void shouldPublishToSpiceDbWhenDeletingUser() {
        // Arrange
        when(permissionsService.writeRelationships(any()))
                .thenReturn(WriteRelationshipsResponse.newBuilder()
                        .setWrittenAt(ZedToken.newBuilder()
                                .setToken("token"))
                        .build());

        // Act
        userUpdater.deleteUser(user);

        // Assert
        verify(permissionsService).writeRelationships(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .extracting(WriteRelationshipsRequest::getUpdatesList, list(RelationshipUpdate.class))
                .singleElement()
                .extracting(RelationshipUpdate::getOperation, Utils::getRelation, Utils::getSubjectType, Utils::getSubjectId, Utils::getResourceType, Utils::getResourceId)
                .containsExactly(OPERATION_DELETE, MEMBER, USER, userId.toString(), GROUP, groupId.toString());
    }

    @Test
    void shouldPublishToSpiceDbWhenDeletingRoot() {
        // Arrange
        when(permissionsService.writeRelationships(any()))
                .thenReturn(WriteRelationshipsResponse.newBuilder()
                        .setWrittenAt(ZedToken.newBuilder()
                                .setToken("token"))
                        .build());
        User root = User.root();
        UUID rootId = root.getUserId();

        // Act
        userUpdater.deleteUser(root);

        // Assert
        verify(permissionsService).writeRelationships(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .extracting(WriteRelationshipsRequest::getUpdatesList, list(RelationshipUpdate.class))
                .hasSize(2)
                .extracting(RelationshipUpdate::getOperation, Utils::getRelation, Utils::getSubjectType, Utils::getSubjectId, Utils::getResourceType, Utils::getResourceId)
                .containsExactly(
                        tuple(OPERATION_DELETE, ADMIN, USER, rootId.toString(), USER, rootId.toString()),
                        tuple(OPERATION_DELETE, MEMBER, USER, rootId.toString(), GROUP, rootId.toString()));
    }

    @Test
    void shouldHandleErrorFromSpiceDbWhenDeletingUser() {
        // Arrange
        when(permissionsService.writeRelationships(any()))
                .thenThrow(RuntimeException.class);
        // Act
        userUpdater.deleteUser(user);
        // Assert
        verify(permissionsService).writeRelationships(any());
    }

    @Test
    void shouldDoNothingWhenCreatingGroup() {
        // Arrange
        // Act
        Group result = userUpdater.createGroup(group);
        // Assert
        assertThat(result).isEqualTo(group);
        verifyNoInteractions(permissionsService);
    }

    @Test
    void shouldPublishToSpiceDbWhenUpdatingGroupWithIdChange() {
        // Arrange
        when(permissionsService.lookupSubjects(any()))
                .thenReturn(List.of(LookupSubjectsResponse.newBuilder()
                        .setSubject(ResolvedSubject.newBuilder()
                                .setSubjectObjectId(userId.toString())
                                .setPermissionship(LOOKUP_PERMISSIONSHIP_HAS_PERMISSION)
                                .build())
                        .build()).iterator());
        when(permissionsService.writeRelationships(any()))
                .thenReturn(WriteRelationshipsResponse.newBuilder()
                        .setWrittenAt(ZedToken.newBuilder()
                                .setToken("token"))
                        .build());

        UUID oldGroupId = UUID.randomUUID();
        Group oldGroup = group.copy().withGroupId(oldGroupId).build();

        // Act
        Group result = userUpdater.updateGroup(oldGroup, group);

        // Assert
        assertThat(result).isEqualTo(group);
        verify(permissionsService).lookupSubjects(lookupCaptor.capture());
        assertThat(lookupCaptor.getValue())
                .extracting(LookupSubjectsRequest::getPermission, Utils::getResourceType, Utils::getResourceId)
                .containsExactly(MEMBER, GROUP, oldGroupId.toString());
        verify(permissionsService).writeRelationships(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .extracting(WriteRelationshipsRequest::getUpdatesList, list(RelationshipUpdate.class))
                .hasSize(2)
                .extracting(RelationshipUpdate::getOperation, Utils::getRelation, Utils::getSubjectType, Utils::getSubjectId, Utils::getResourceType, Utils::getResourceId)
                .containsExactly(
                        tuple(OPERATION_DELETE, MEMBER, USER, userId.toString(), GROUP, oldGroupId.toString()),
                        tuple(OPERATION_CREATE, MEMBER, USER, userId.toString(), GROUP, groupId.toString()));
    }

    @Test
    void shouldDoNothingWhenErrorOccurredWhenLookingUpUser() {
        // Arrange
        when(permissionsService.lookupSubjects(any()))
                .thenThrow(RuntimeException.class);
        when(permissionsService.writeRelationships(any()))
                .thenReturn(WriteRelationshipsResponse.newBuilder()
                        .setWrittenAt(ZedToken.newBuilder()
                                .setToken("token"))
                        .build());

        UUID oldGroupId = UUID.randomUUID();
        Group oldGroup = group.copy().withGroupId(oldGroupId).build();

        // Act
        Group result = userUpdater.updateGroup(oldGroup, group);

        // Assert
        assertThat(result).isEqualTo(group);
        verify(permissionsService).lookupSubjects(any());
        verify(permissionsService).writeRelationships(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .extracting(WriteRelationshipsRequest::getUpdatesList, list(RelationshipUpdate.class))
                .isEmpty();
    }

    @Test
    void shouldDoNothingWhenUpdatingGroupWithNameChange() {
        // Arrange
        when(permissionsService.writeRelationships(any()))
                .thenReturn(WriteRelationshipsResponse.newBuilder()
                        .setWrittenAt(ZedToken.newBuilder()
                                .setToken("token"))
                        .build());
        Group oldGroup = group.copy().withName("oldName").build();

        // Act
        Group result = userUpdater.updateGroup(oldGroup, group);

        // Assert
        assertThat(result).isEqualTo(group);
        verify(permissionsService).writeRelationships(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .extracting(WriteRelationshipsRequest::getUpdatesList, list(RelationshipUpdate.class))
                .isEmpty();
    }

    @ParameterizedTest
    @EnumSource(names = {"DELETION_PROGRESS_UNSPECIFIED", "DELETION_PROGRESS_COMPLETE", "DELETION_PROGRESS_PARTIAL"})
    void shouldDelegateToSpiceDbWhenCheckingDeleteGroupPermission(DeleteRelationshipsResponse.DeletionProgress progress) {
        // Arrange
        when(permissionsService.deleteRelationships(any()))
                .thenReturn(DeleteRelationshipsResponse.newBuilder()
                        .setDeletionProgress(progress)
                        .setDeletedAt(ZedToken.newBuilder()
                                .setToken("token"))
                        .build());

        // Act
        userUpdater.deleteGroup(group);

        // Assert
        verify(permissionsService).deleteRelationships(deleteCaptor.capture());
        assertThat(deleteCaptor.getValue())
                .extracting(DeleteRelationshipsRequest::getRelationshipFilter)
                .extracting(RelationshipFilter::getOptionalRelation, RelationshipFilter::getResourceType, RelationshipFilter::getOptionalResourceId)
                .containsExactly(MEMBER, GROUP, groupId.toString());
    }

    @Test
    void shouldHandleErrorFromSpiceDbWhenDeletingGroup() {
        // Arrange
        when(permissionsService.deleteRelationships(any()))
                .thenThrow(RuntimeException.class);
        // Act
        userUpdater.deleteGroup(group);
        // Assert
        verify(permissionsService).deleteRelationships(any());
    }

}
