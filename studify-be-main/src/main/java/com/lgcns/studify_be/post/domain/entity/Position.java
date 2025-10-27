package com.lgcns.studify_be.post.domain.entity;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Position {
    BE("be"),
    FE("fe"),
    PM("pm"),
    DESIGNER("designer"),
    AI("ai"),
    ANDROID("andoid"),
    IOS("ios"),
    WEB("web");

    private final String value;

    Position(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Position from(String position) {
        if (position == null) return null;
        return Position.valueOf(position.toUpperCase());
    }

    // List<String>을 Enum 리스트로 변환
    public static List<Position> fromList(List<String> positions) {
        if (positions == null) return null;
        return positions.stream()
                        .map(Position::from)
                        .collect(Collectors.toList());
    }
}