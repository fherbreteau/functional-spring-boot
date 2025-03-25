package io.github.fherbreteau.functional.rules.check;

import static io.github.fherbreteau.functional.rules.Entities.*;
import static io.github.fherbreteau.functional.rules.check.Permissions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.authzed.api.v1.*;
import com.authzed.api.v1.CheckPermissionResponse.Permissionship;
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
class UserCheckerTest {

    @Mock
    private PermissionsServiceGrpc.PermissionsServiceBlockingStub permissionsService;

    private UUID userId;

    private User actor;

    @InjectMocks
    private UserCheckerImpl userChecker;

    @Captor
    private ArgumentCaptor<CheckPermissionRequest> requestCaptor;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
        actor = User.builder("user")
                .withUserId(userId)
                .build();
    }

    @ParameterizedTest
    @CsvSource(value = {"PERMISSIONSHIP_UNSPECIFIED,false", "PERMISSIONSHIP_NO_PERMISSION,false",
        "PERMISSIONSHIP_HAS_PERMISSION,true", "PERMISSIONSHIP_CONDITIONAL_PERMISSION,false"})
    void shouldDelegateToSpiceDbWhenCheckingCreateUserPermission(Permissionship permissionship, boolean expectedResult) {
        // Arrange
        when(permissionsService.checkPermission(any()))
                .thenReturn(CheckPermissionResponse.newBuilder()
                        .setPermissionship(permissionship)
                        .build());

        // Act
        boolean result = userChecker.canCreateUser("new_user", actor);

        // Assert
        assertThat(result).isEqualTo(expectedResult);
        verify(permissionsService).checkPermission(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getPermission)
                .isEqualTo(CREATE);
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getResource, type(ObjectReference.class))
                .extracting(ObjectReference::getObjectType, ObjectReference::getObjectId)
                .containsExactly(USER, userId.toString());
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getSubject, type(SubjectReference.class))
                .extracting(SubjectReference::getObject)
                .extracting(ObjectReference::getObjectType, ObjectReference::getObjectId)
                .containsExactly(USER, userId.toString());
    }

    @Test
    void shouldHandleErrorWhenCheckingCreateUserPermission() {
        // Arrange
        when(permissionsService.checkPermission(any()))
                .thenThrow(RuntimeException.class);

        // Act
        boolean result = userChecker.canCreateUser("new_user", actor);

        // Assert
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @CsvSource(value = {"PERMISSIONSHIP_UNSPECIFIED,false", "PERMISSIONSHIP_NO_PERMISSION,false",
        "PERMISSIONSHIP_HAS_PERMISSION,true", "PERMISSIONSHIP_CONDITIONAL_PERMISSION,false"})
    void shouldDelegateToSpiceDbWhenCheckingUpdateUserPermission(Permissionship permissionship, boolean expectedResult) {
        // Arrange
        when(permissionsService.checkPermission(any()))
                .thenReturn(CheckPermissionResponse.newBuilder()
                        .setPermissionship(permissionship)
                        .build());

        // Act
        boolean result = userChecker.canUpdateUser("new_user", actor);

        // Assert
        assertThat(result).isEqualTo(expectedResult);
        verify(permissionsService).checkPermission(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getPermission)
                .isEqualTo(UPDATE);
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getResource, type(ObjectReference.class))
                .extracting(ObjectReference::getObjectType, ObjectReference::getObjectId)
                .containsExactly(USER, userId.toString());
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getSubject, type(SubjectReference.class))
                .extracting(SubjectReference::getObject)
                .extracting(ObjectReference::getObjectType, ObjectReference::getObjectId)
                .containsExactly(USER, userId.toString());
    }

    @Test
    void shouldHandleErrorWhenCheckingUpdateUserPermission() {
        // Arrange
        when(permissionsService.checkPermission(any()))
                .thenThrow(RuntimeException.class);

        // Act
        boolean result = userChecker.canUpdateUser("new_user", actor);

        // Assert
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @CsvSource(value = {"PERMISSIONSHIP_UNSPECIFIED,false", "PERMISSIONSHIP_NO_PERMISSION,false",
        "PERMISSIONSHIP_HAS_PERMISSION,true", "PERMISSIONSHIP_CONDITIONAL_PERMISSION,false"})
    void shouldDelegateToSpiceDbWhenCheckingDeleteUserPermission(Permissionship permissionship, boolean expectedResult) {
        // Arrange
        when(permissionsService.checkPermission(any()))
                .thenReturn(CheckPermissionResponse.newBuilder()
                        .setPermissionship(permissionship)
                        .build());

        // Act
        boolean result = userChecker.canDeleteUser("new_user", actor);

        // Assert
        assertThat(result).isEqualTo(expectedResult);
        verify(permissionsService).checkPermission(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getPermission)
                .isEqualTo(DELETE);
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getResource, type(ObjectReference.class))
                .extracting(ObjectReference::getObjectType, ObjectReference::getObjectId)
                .containsExactly(USER, userId.toString());
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getSubject, type(SubjectReference.class))
                .extracting(SubjectReference::getObject)
                .extracting(ObjectReference::getObjectType, ObjectReference::getObjectId)
                .containsExactly(USER, userId.toString());
    }

    @Test
    void shouldHandleErrorWhenCheckingDeleteUserPermission() {
        // Arrange
        when(permissionsService.checkPermission(any()))
                .thenThrow(RuntimeException.class);

        // Act
        boolean result = userChecker.canDeleteUser("new_user", actor);

        // Assert
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @CsvSource(value = {"PERMISSIONSHIP_UNSPECIFIED,false", "PERMISSIONSHIP_NO_PERMISSION,false",
        "PERMISSIONSHIP_HAS_PERMISSION,true", "PERMISSIONSHIP_CONDITIONAL_PERMISSION,false"})
    void shouldDelegateToSpiceDbWhenCheckingCreateGroupPermission(Permissionship permissionship, boolean expectedResult) {
        // Arrange
        when(permissionsService.checkPermission(any()))
                .thenReturn(CheckPermissionResponse.newBuilder()
                        .setPermissionship(permissionship)
                        .build());

        // Act
        boolean result = userChecker.canCreateGroup("new_group", actor);

        // Assert
        assertThat(result).isEqualTo(expectedResult);
        verify(permissionsService).checkPermission(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getPermission)
                .isEqualTo(CREATE);
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getResource, type(ObjectReference.class))
                .extracting(ObjectReference::getObjectType, ObjectReference::getObjectId)
                .containsExactly(USER, userId.toString());
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getSubject, type(SubjectReference.class))
                .extracting(SubjectReference::getObject)
                .extracting(ObjectReference::getObjectType, ObjectReference::getObjectId)
                .containsExactly(USER, userId.toString());
    }

    @Test
    void shouldHandleErrorWhenCheckingCreateGroupPermission() {
        // Arrange
        when(permissionsService.checkPermission(any()))
                .thenThrow(RuntimeException.class);

        // Act
        boolean result = userChecker.canCreateGroup("new_group", actor);

        // Assert
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @CsvSource(value = {"PERMISSIONSHIP_UNSPECIFIED,false", "PERMISSIONSHIP_NO_PERMISSION,false",
        "PERMISSIONSHIP_HAS_PERMISSION,true", "PERMISSIONSHIP_CONDITIONAL_PERMISSION,false"})
    void shouldDelegateToSpiceDbWhenCheckingUpdateGroupPermission(Permissionship permissionship, boolean expectedResult) {
        // Arrange
        when(permissionsService.checkPermission(any()))
                .thenReturn(CheckPermissionResponse.newBuilder()
                        .setPermissionship(permissionship)
                        .build());

        // Act
        boolean result = userChecker.canUpdateGroup("new_group", actor);

        // Assert
        assertThat(result).isEqualTo(expectedResult);
        verify(permissionsService).checkPermission(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getPermission)
                .isEqualTo(UPDATE);
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getResource, type(ObjectReference.class))
                .extracting(ObjectReference::getObjectType, ObjectReference::getObjectId)
                .containsExactly(USER, userId.toString());
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getSubject, type(SubjectReference.class))
                .extracting(SubjectReference::getObject)
                .extracting(ObjectReference::getObjectType, ObjectReference::getObjectId)
                .containsExactly(USER, userId.toString());
    }

    @Test
    void shouldHandleErrorWhenCheckingUpdateGroupPermission() {
        // Arrange
        when(permissionsService.checkPermission(any()))
                .thenThrow(RuntimeException.class);

        // Act
        boolean result = userChecker.canUpdateGroup("new_group", actor);

        // Assert
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @CsvSource(value = {"PERMISSIONSHIP_UNSPECIFIED,false", "PERMISSIONSHIP_NO_PERMISSION,false",
        "PERMISSIONSHIP_HAS_PERMISSION,true", "PERMISSIONSHIP_CONDITIONAL_PERMISSION,false"})
    void shouldDelegateToSpiceDbWhenCheckingDeleteGroupPermission(Permissionship permissionship, boolean expectedResult) {
        // Arrange
        when(permissionsService.checkPermission(any()))
                .thenReturn(CheckPermissionResponse.newBuilder()
                        .setPermissionship(permissionship)
                        .build());

        // Act
        boolean result = userChecker.canDeleteGroup("new_group", actor);

        // Assert
        assertThat(result).isEqualTo(expectedResult);
        verify(permissionsService).checkPermission(requestCaptor.capture());
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getPermission)
                .isEqualTo(DELETE);
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getResource, type(ObjectReference.class))
                .extracting(ObjectReference::getObjectType, ObjectReference::getObjectId)
                .containsExactly(USER, userId.toString());
        assertThat(requestCaptor.getValue())
                .extracting(CheckPermissionRequest::getSubject, type(SubjectReference.class))
                .extracting(SubjectReference::getObject)
                .extracting(ObjectReference::getObjectType, ObjectReference::getObjectId)
                .containsExactly(USER, userId.toString());
    }

    @Test
    void shouldHandleErrorWhenCheckingDeleteGroupPermission() {
        // Arrange
        when(permissionsService.checkPermission(any()))
                .thenThrow(RuntimeException.class);

        // Act
        boolean result = userChecker.canDeleteGroup("new_group", actor);

        // Assert
        assertThat(result).isFalse();
    }
}
