package io.github.fherbreteau.functional.rules.check;

import static io.github.fherbreteau.functional.rules.Entities.ITEM;
import static io.github.fherbreteau.functional.rules.Entities.USER;
import static io.github.fherbreteau.functional.rules.check.Permissions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.authzed.api.v1.*;
import com.authzed.api.v1.CheckPermissionResponse.Permissionship;
import io.github.fherbreteau.functional.domain.entities.File;
import io.github.fherbreteau.functional.domain.entities.Folder;
import io.github.fherbreteau.functional.domain.entities.Item;
import io.github.fherbreteau.functional.domain.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessCheckerTest {

    @Mock
    private PermissionsServiceGrpc.PermissionsServiceBlockingStub permissionsService;

    private UUID fileHandle;

    private Item item;

    private UUID userId;

    private User actor;

    @InjectMocks
    private AccessCheckerImpl accessChecker;

    @Captor
    private ArgumentCaptor<CheckPermissionRequest> requestCaptor;

    @BeforeEach
    void setUp() {
        fileHandle = UUID.randomUUID();
        item = File.builder()
                .withName("file")
                .withHandle(fileHandle)
                .withParent(Folder.getRoot())
                .withOwner(User.root())
                .build();
        userId = UUID.randomUUID();
        actor = User.builder("user")
                .withUserId(userId)
                .build();
    }

    @ParameterizedTest
    @CsvSource(value = {"PERMISSIONSHIP_UNSPECIFIED,false", "PERMISSIONSHIP_NO_PERMISSION,false",
        "PERMISSIONSHIP_HAS_PERMISSION,true", "PERMISSIONSHIP_CONDITIONAL_PERMISSION,false"})
    void shouldDelegateToSpiceDbWhenCheckingReadPermission(Permissionship permissionship, boolean expectedResult) {
        // Arrange
        when(permissionsService.checkPermission(any()))
                .thenReturn(CheckPermissionResponse.newBuilder()
                        .setPermissionship(permissionship)
                        .build());

        // Act
        boolean result = accessChecker.canRead(item, actor);

        // Assert
        assertThat(result).isEqualTo(expectedResult);
        verify(permissionsService).checkPermission(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getPermission)
                .isEqualTo(READ);
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getResource, type(ObjectReference.class))
                .extracting(ObjectReference::getObjectType, ObjectReference::getObjectId)
                .containsExactly(ITEM, fileHandle.toString());
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getSubject, type(SubjectReference.class))
                .extracting(SubjectReference::getObject)
                .extracting(ObjectReference::getObjectType, ObjectReference::getObjectId)
                .containsExactly(USER, userId.toString());
    }

    @Test
    void shouldHandleErrorWhenCheckingReadPermission() {
        // Arrange
        when(permissionsService.checkPermission(any()))
                .thenThrow(RuntimeException.class);

        // Act
        boolean result = accessChecker.canRead(item, actor);

        // Assert
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @CsvSource(value = {"PERMISSIONSHIP_UNSPECIFIED,false", "PERMISSIONSHIP_NO_PERMISSION,false",
        "PERMISSIONSHIP_HAS_PERMISSION,true", "PERMISSIONSHIP_CONDITIONAL_PERMISSION,false"})
    void shouldDelegateToSpiceDbWhenCheckingWritePermission(Permissionship permissionship, boolean expectedResult) {
        // Arrange
        when(permissionsService.checkPermission(any()))
                .thenReturn(CheckPermissionResponse.newBuilder()
                        .setPermissionship(permissionship)
                        .build());

        // Act
        boolean result = accessChecker.canWrite(item, actor);

        // Assert
        assertThat(result).isEqualTo(expectedResult);
        verify(permissionsService).checkPermission(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getPermission)
                .isEqualTo(WRITE);
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getResource, type(ObjectReference.class))
                .extracting(ObjectReference::getObjectType, ObjectReference::getObjectId)
                .containsExactly(ITEM, fileHandle.toString());
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getSubject, type(SubjectReference.class))
                .extracting(SubjectReference::getObject)
                .extracting(ObjectReference::getObjectType, ObjectReference::getObjectId)
                .containsExactly(USER, userId.toString());
    }

    @Test
    void shouldHandleErrorWhenCheckingWritePermission() {
        // Arrange
        when(permissionsService.checkPermission(any()))
                .thenThrow(RuntimeException.class);

        // Act
        boolean result = accessChecker.canWrite(item, actor);

        // Assert
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @CsvSource(value = {"PERMISSIONSHIP_UNSPECIFIED,false", "PERMISSIONSHIP_NO_PERMISSION,false",
        "PERMISSIONSHIP_HAS_PERMISSION,true", "PERMISSIONSHIP_CONDITIONAL_PERMISSION,false"})
    void shouldDelegateToSpiceDbWhenCheckingExecutePermission(Permissionship permissionship, boolean expectedResult) {
        // Arrange
        when(permissionsService.checkPermission(any()))
                .thenReturn(CheckPermissionResponse.newBuilder()
                        .setPermissionship(permissionship)
                        .build());

        // Act
        boolean result = accessChecker.canExecute(item, actor);

        // Assert
        assertThat(result).isEqualTo(expectedResult);
        verify(permissionsService).checkPermission(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getPermission)
                .isEqualTo(EXECUTE);
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getResource, type(ObjectReference.class))
                .extracting(ObjectReference::getObjectType, ObjectReference::getObjectId)
                .containsExactly(ITEM, fileHandle.toString());
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getSubject, type(SubjectReference.class))
                .extracting(SubjectReference::getObject)
                .extracting(ObjectReference::getObjectType, ObjectReference::getObjectId)
                .containsExactly(USER, userId.toString());
    }

    @Test
    void shouldHandleErrorWhenCheckingExecutePermission() {
        // Arrange
        when(permissionsService.checkPermission(any()))
                .thenThrow(RuntimeException.class);

        // Act
        boolean result = accessChecker.canExecute(item, actor);

        // Assert
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @CsvSource(value = {"PERMISSIONSHIP_UNSPECIFIED,false", "PERMISSIONSHIP_NO_PERMISSION,false",
        "PERMISSIONSHIP_HAS_PERMISSION,true", "PERMISSIONSHIP_CONDITIONAL_PERMISSION,false"})
    void shouldDelegateToSpiceDbWhenCheckingChangeModePermission(Permissionship permissionship, boolean expectedResult) {
        // Arrange
        when(permissionsService.checkPermission(any()))
                .thenReturn(CheckPermissionResponse.newBuilder()
                        .setPermissionship(permissionship)
                        .build());

        // Act
        boolean result = accessChecker.canChangeMode(item, actor);

        // Assert
        assertThat(result).isEqualTo(expectedResult);
        verify(permissionsService).checkPermission(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getPermission)
                .isEqualTo(CHANGE_MODE);
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getResource, type(ObjectReference.class))
                .extracting(ObjectReference::getObjectType, ObjectReference::getObjectId)
                .containsExactly(ITEM, fileHandle.toString());
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getSubject, type(SubjectReference.class))
                .extracting(SubjectReference::getObject)
                .extracting(ObjectReference::getObjectType, ObjectReference::getObjectId)
                .containsExactly(USER, userId.toString());
    }

    @Test
    void shouldHandleErrorWhenCheckingChangeModePermission() {
        // Arrange
        when(permissionsService.checkPermission(any()))
                .thenThrow(RuntimeException.class);

        // Act
        boolean result = accessChecker.canChangeMode(item, actor);

        // Assert
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @CsvSource(value = {"PERMISSIONSHIP_UNSPECIFIED,false", "PERMISSIONSHIP_NO_PERMISSION,false",
        "PERMISSIONSHIP_HAS_PERMISSION,true", "PERMISSIONSHIP_CONDITIONAL_PERMISSION,false"})
    void shouldDelegateToSpiceDbWhenCheckingChangeOwnerPermission(Permissionship permissionship, boolean expectedResult) {
        // Arrange
        when(permissionsService.checkPermission(any()))
                .thenReturn(CheckPermissionResponse.newBuilder()
                        .setPermissionship(permissionship)
                        .build());

        // Act
        boolean result = accessChecker.canChangeOwner(item, actor);

        // Assert
        assertThat(result).isEqualTo(expectedResult);
        verify(permissionsService).checkPermission(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getPermission)
                .isEqualTo(CHANGE_OWNER);
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getResource, type(ObjectReference.class))
                .extracting(ObjectReference::getObjectType, ObjectReference::getObjectId)
                .containsExactly(ITEM, fileHandle.toString());
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getSubject, type(SubjectReference.class))
                .extracting(SubjectReference::getObject)
                .extracting(ObjectReference::getObjectType, ObjectReference::getObjectId)
                .containsExactly(USER, userId.toString());
    }

    @Test
    void shouldHandleErrorWhenCheckingChangeOwnerPermission() {
        // Arrange
        when(permissionsService.checkPermission(any()))
                .thenThrow(RuntimeException.class);

        // Act
        boolean result = accessChecker.canChangeOwner(item, actor);

        // Assert
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @CsvSource(value = {"PERMISSIONSHIP_UNSPECIFIED,false", "PERMISSIONSHIP_NO_PERMISSION,false",
        "PERMISSIONSHIP_HAS_PERMISSION,true", "PERMISSIONSHIP_CONDITIONAL_PERMISSION,false"})
    void shouldDelegateToSpiceDbWhenCheckingChangeGroupPermission(Permissionship permissionship, boolean expectedResult) {
        // Arrange
        when(permissionsService.checkPermission(any()))
                .thenReturn(CheckPermissionResponse.newBuilder()
                        .setPermissionship(permissionship)
                        .build());

        // Act
        boolean result = accessChecker.canChangeGroup(item, actor);

        // Assert
        assertThat(result).isEqualTo(expectedResult);
        verify(permissionsService).checkPermission(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getPermission)
                .isEqualTo(CHANGE_GROUP);
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getResource, type(ObjectReference.class))
                .extracting(ObjectReference::getObjectType, ObjectReference::getObjectId)
                .containsExactly(ITEM, fileHandle.toString());
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getSubject, type(SubjectReference.class))
                .extracting(SubjectReference::getObject)
                .extracting(ObjectReference::getObjectType, ObjectReference::getObjectId)
                .containsExactly(USER, userId.toString());
    }

    @Test
    void shouldHandleErrorWhenCheckingCheckGroupPermission() {
        // Arrange
        when(permissionsService.checkPermission(any()))
                .thenThrow(RuntimeException.class);

        // Act
        boolean result = accessChecker.canChangeGroup(item, actor);

        // Assert
        assertThat(result).isFalse();
    }
}
