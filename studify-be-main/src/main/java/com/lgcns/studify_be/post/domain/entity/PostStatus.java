package com.lgcns.studify_be.post.domain.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PostStatus {
    OPEN("open"),
    CLOSED("closed");

    private final String value;

    PostStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PostStatus from(String status) {
        if (status == null) return null;
        return PostStatus.valueOf(status.toUpperCase());
    }
}
