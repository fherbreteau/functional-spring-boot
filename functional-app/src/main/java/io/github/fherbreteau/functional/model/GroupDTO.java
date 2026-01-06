package io.github.fherbreteau.functional.model;

import java.util.UUID;

import tools.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = GroupDTO.Builder.class)
public final class GroupDTO {
    private final UUID gid;
    private final String name;

    private GroupDTO(Builder builder) {
        this.gid = builder.gid;
        this.name = builder.name;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getGid() {
        return gid;
    }

    public String getName() {
        return name;
    }

    public static final class Builder {

        private UUID gid;
        private String name;

        private Builder() {
        }

        public Builder withGid(UUID gid) {
            this.gid = gid;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public GroupDTO build() {
            return new GroupDTO(this);
        }
    }
}
