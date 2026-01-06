package io.github.fherbreteau.functional.mapper;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import io.github.fherbreteau.functional.domain.entities.AccessRight;
import io.github.fherbreteau.functional.domain.entities.File;
import io.github.fherbreteau.functional.domain.entities.Group;
import io.github.fherbreteau.functional.domain.entities.Item;
import io.github.fherbreteau.functional.domain.entities.User;
import io.github.fherbreteau.functional.model.GroupDTO;
import io.github.fherbreteau.functional.model.ItemDTO;
import io.github.fherbreteau.functional.model.UserDTO;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class EntityMapper {

    public List<ItemDTO> mapToItemList(@Nonnull Object value) {
        if (value instanceof Collection<?> coll) {
            return coll.stream()
                    .map(this::mapToItem)
                    .filter(Objects::nonNull)
                    .toList();
        }
        return of(value)
                .map(this::mapToItem)
                .map(List::of)
                .orElse(List.of());
    }

    public List<GroupDTO> mapToGroupList(@Nonnull Object value) {
        if (value instanceof Collection<?> coll) {
            return coll.stream()
                    .map(this::mapToGroup)
                    .filter(Objects::nonNull)
                    .toList();
        }
        return of(value)
                .map(this::mapToGroup)
                .map(List::of)
                .orElse(List.of());
    }

    public ItemDTO mapToItem(@Nonnull Object value) {
        if (value instanceof Item item) {
            String access = formatAccess(item.isFolder(), item.getOwnerAccess(), item.getGroupAccess(),
                    item.getOtherAccess());
            ItemDTO.Builder builder = ItemDTO.builder()
                    .withName(item.getName())
                    .withOwner(item.getOwner().getName())
                    .withGroup(item.getGroup().getName())
                    .withAccess(access)
                    .withCreated(item.getCreated())
                    .withModified(item.getLastModified())
                    .withAccessed(item.getLastAccessed());
            if (item instanceof File file) {
                return builder.withContentType(file.getContentType())
                        .build();
            }
            return builder.build();
        }
        return null;
    }

    public UserDTO mapToUser(@Nonnull Object value) {
        if (value instanceof User user) {
            return UserDTO.builder()
                    .withUid(user.getUserId())
                    .withName(user.getName())
                    .withGroups(mapToGroupList(user.getGroups()))
                    .build();
        }
        return null;
    }

    public GroupDTO mapToGroup(@Nonnull Object value) {
        if (value instanceof Group group) {
            return GroupDTO.builder()
                    .withGid(group.getGroupId())
                    .withName(group.getName())
                    .build();
        }
        return null;
    }

    public ResponseEntity<InputStreamResource> mapStream(@Nonnull Object value, @Nullable String contentType) {
        MediaType mediaType = ofNullable(contentType)
                .map(MediaType::parseMediaType)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        if (value instanceof InputStream stream) {
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(new InputStreamResource(stream));
        }
        return ResponseEntity.unprocessableContent().build();
    }

    private String formatAccess(boolean folder, AccessRight ownerAccess, AccessRight groupAccess,
                                AccessRight otherAccess) {
        return (folder ? "d" : "-") + ownerAccess + groupAccess + otherAccess;
    }
}
