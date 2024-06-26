package io.github.fherbreteau.functional.mapper;

import io.github.fherbreteau.functional.domain.entities.*;
import io.github.fherbreteau.functional.model.GroupDTO;
import io.github.fherbreteau.functional.model.ItemDTO;
import io.github.fherbreteau.functional.model.UserDTO;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static java.util.Optional.ofNullable;

@Service
public class EntityMapper {

    public List<ItemDTO> mapToItemList(Object value) {
        if (value instanceof Collection<?> coll) {
            return coll.stream()
                    .map(this::mapToItem)
                    .filter(Objects::nonNull)
                    .toList();
        }
        return ofNullable(value)
                .map(this::mapToItem)
                .map(List::of)
                .orElse(List.of());
    }

    public List<GroupDTO> mapToGroupList(Object value) {
        if (value instanceof Collection<?> coll) {
            return coll.stream()
                    .map(this::mapToGroup)
                    .filter(Objects::nonNull)
                    .toList();
        }
        return ofNullable(value)
                .map(this::mapToGroup)
                .map(List::of)
                .orElse(List.of());
    }

    public ItemDTO mapToItem(Object value) {
        if (value instanceof Item item) {
            String access = formatAccess(item.isFolder(), item.getOwnerAccess(), item.getGroupAccess(),
                    item.getOtherAccess());
            ItemDTO.Builder builder = ItemDTO.buidler()
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

    public UserDTO mapToUser(Object value) {
        if (value instanceof User user) {
            return UserDTO.builder()
                    .withUid(user.getUserId())
                    .withName(user.getName())
                    .withGroups(mapToGroupList(user.getGroups()))
                    .build();
        }
        return null;
    }

    public GroupDTO mapToGroup(Object value) {
        if (value instanceof Group group) {
            return GroupDTO.builder()
                    .withGid(group.getGroupId())
                    .withName(group.getName())
                    .build();
        }
        return null;
    }

    public ResponseEntity<InputStreamResource> mapStream(Object value, String contentType) {
        MediaType mediaType = ofNullable(contentType)
                .map(MediaType::parseMediaType)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        if (value instanceof InputStream stream) {
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(new InputStreamResource(stream));
        }
        return ResponseEntity.unprocessableEntity().build();
    }

    private String formatAccess(boolean folder, AccessRight ownerAccess, AccessRight groupAccess,
                                AccessRight otherAccess) {
        return (folder ? "d" : "-") + ownerAccess + groupAccess + otherAccess;
    }
}
