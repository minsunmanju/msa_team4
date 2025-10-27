package com.lgcns.studify_be.post.domain.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Category {
    STUDY("study"),
    PROJECT("project");

    private final String value;

    Category(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Category from(String category) {
        if (category == null) return null;
        return Category.valueOf(category.toUpperCase());
    }
}
