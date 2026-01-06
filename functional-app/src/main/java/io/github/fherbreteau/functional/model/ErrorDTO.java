package io.github.fherbreteau.functional.model;

import java.util.List;

import tools.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = ErrorDTO.Builder.class)
public final class ErrorDTO {
    private final String type;
    private final String message;
    private final List<String> reasons;

    private ErrorDTO(Builder builder) {
        type = builder.type;
        message = builder.message;
        reasons = builder.reasons;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public static final class Builder {
        private String type;
        private String message;
        private List<String> reasons;

        private Builder() {
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder withReasons(List<String> reasons) {
            this.reasons = reasons;
            return this;
        }

        public ErrorDTO build() {
            return new ErrorDTO(this);
        }
    }
}
