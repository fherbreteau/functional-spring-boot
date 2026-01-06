package io.github.fherbreteau.functional.model;

import java.util.List;
import java.util.UUID;

import tools.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = UserDTO.Builder.class)
public final class UserDTO {

    private final UUID uid;
    private final String name;
    private final List<GroupDTO> groups;

    private UserDTO(Builder builder) {
        this.uid = builder.uid;
        this.name = builder.name;
        this.groups = builder.groups;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public List<GroupDTO> getGroups() {
        return groups;
    }

    public static final class Builder {
        private UUID uid;
        private String name;
        private List<GroupDTO> groups;

        private Builder() {

        }

        public Builder withUid(UUID uid) {
            this.uid = uid;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withGroups(List<GroupDTO> groups) {
            this.groups = groups;
            return this;
        }

        public UserDTO build() {
            return new UserDTO(this);
        }
    }
}
