package io.github.fherbreteau.functional.model;

import java.util.List;
import java.util.UUID;

import tools.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = InputUserDTO.Builder.class)
public final class InputUserDTO {
    private final UUID uid;
    private final String name;
    private final UUID gid;
    private final List<String> groups;
    private final String password;

    private InputUserDTO(Builder builder) {
        this.uid = builder.uid;
        this.name = builder.name;
        this.gid = builder.gid;
        this.groups = builder.groups;
        this.password = builder.password;
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

    public UUID getGid() {
        return gid;
    }

    public List<String> getGroups() {
        return groups;
    }

    public String getPassword() {
        return password;
    }

    public static final class Builder {
        private UUID uid;
        private String name;
        private UUID gid;
        private List<String> groups;
        private String password;

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

        public Builder withGid(UUID gid) {
            this.gid = gid;
            return this;
        }

        public Builder withGroups(List<String> groups) {
            this.groups = groups;
            return this;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public InputUserDTO build() {
            if (groups == null) {
                this.groups = List.of();
            }
            return new InputUserDTO(this);
        }
    }
}
