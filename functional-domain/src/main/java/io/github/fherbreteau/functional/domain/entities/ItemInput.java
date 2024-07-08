package io.github.fherbreteau.functional.domain.entities;

import java.io.InputStream;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Objects.isNull;

public final class ItemInput {

    private final Item item;
    private final String name;
    private final User user;
    private final Group group;
    private final AccessRight ownerAccess;
    private final AccessRight groupAccess;
    private final AccessRight otherAccess;
    private final InputStream inputStream;
    private final String contentType;
    private final Item destination;

    private ItemInput(Builder builder) {
        this.item = builder.item;
        this.name = builder.name;
        this.user = builder.user;
        this.group = builder.group;
        this.ownerAccess = builder.ownerAccess;
        this.groupAccess = builder.groupAccess;
        this.otherAccess = builder.otherAccess;
        this.inputStream = builder.inputStream;
        this.contentType = builder.contentType;
        this.destination = builder.destination;
    }

    public Item getItem() {
        return item;
    }

    public String getName() {
        return name;
    }

    public User getUser() {
        return user;
    }

    public Group getGroup() {
        return group;
    }

    public AccessRight getOwnerAccess() {
        return ownerAccess;
    }

    public AccessRight getGroupAccess() {
        return groupAccess;
    }

    public AccessRight getOtherAccess() {
        return otherAccess;
    }

    public boolean hasAccess() {
        return Stream.of(ownerAccess, groupAccess, otherAccess).anyMatch(Objects::nonNull);
    }

    public InputStream getContent() {
        return inputStream;
    }

    public String getContentType() {
        return contentType;
    }

    public Item getDestination() {
        return destination;
    }

    public static Builder builder(Item item) {
        return new Builder(item);
    }

    @Override
    public String toString() {
        return "Input{" +
                "item=" + item +
                ", name='" + name + '\'' +
                ", user=" + getName(user) +
                ", group=" + getName(group) +
                ", ownerAccess=" + ownerAccess +
                ", groupAccess=" + groupAccess +
                ", otherAccess=" + otherAccess +
                ", contentType=" + contentType +
                ", destination=" + destination +
                '}';
    }

    private String getName(User user) {
        return isNull(user) ? null : user.getName();
    }

    private String getName(Group group) {
        return isNull(group) ? null : group.getName();
    }

    public static final class Builder {

        private final Item item;
        private String name;
        private User user;
        private Group group;
        private AccessRight ownerAccess;
        private AccessRight groupAccess;
        private AccessRight otherAccess;
        private InputStream inputStream;
        private String contentType;
        public Item destination;

        private Builder(Item item) {
            this.item = item;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withUser(User user) {
            this.user = user;
            return this;
        }

        public Builder withGroup(Group group) {
            this.group = group;
            return this;
        }

        public Builder withOwnerAccess(AccessRight ownerAccess) {
            this.ownerAccess = ownerAccess;
            return this;
        }

        public Builder withGroupAccess(AccessRight groupAccess) {
            this.groupAccess = groupAccess;
            return this;
        }

        public Builder withOtherAccess(AccessRight otherAccess) {
            this.otherAccess = otherAccess;
            return this;
        }

        public Builder withContent(InputStream inputStream) {
            this.inputStream = inputStream;
            return this;
        }

        public Builder withContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder withDestination(Item destination) {
            this.destination = destination;
            return this;
        }

        public ItemInput build() {
            return new ItemInput(this);
        }
    }
}
