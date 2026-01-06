package io.github.fherbreteau.functional.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = ItemDTO.Builder.class)
public final class ItemDTO {

    private final String name;
    private final String owner;
    private final String group;
    private final String access;
    private final LocalDateTime created;
    private final LocalDateTime modified;
    private final LocalDateTime accessed;
    @JsonProperty("content-type")
    private final String contentType;

    private ItemDTO(Builder builder) {
        name = builder.name;
        owner = builder.owner;
        group = builder.group;
        access = builder.access;
        created = builder.created;
        modified = builder.modified;
        accessed = builder.accessed;
        contentType = builder.contentType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public String getGroup() {
        return group;
    }

    public String getAccess() {
        return access;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public LocalDateTime getModified() {
        return modified;
    }

    public LocalDateTime getAccessed() {
        return accessed;
    }

    public String getContentType() {
        return contentType;
    }

    public static final class Builder {
        private String name;
        private String owner;
        private String group;
        private String access;
        private LocalDateTime created;
        private LocalDateTime modified;
        private LocalDateTime accessed;
        private String contentType;

        private Builder() {
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withOwner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder withGroup(String group) {
            this.group = group;
            return this;
        }

        public Builder withAccess(String access) {
            this.access = access;
            return this;
        }

        public Builder withCreated(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public Builder withModified(LocalDateTime modified) {
            this.modified = modified;
            return this;
        }

        public Builder withAccessed(LocalDateTime accessed) {
            this.accessed = accessed;
            return this;
        }

        public Builder withContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public ItemDTO build() {
            return new ItemDTO(this);
        }
    }
}
