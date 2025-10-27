package com.lgcns.studify_be.user.dto;

import com.lgcns.studify_be.user.User;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthorDTO {

    private Long id;
    private String nickname;

    public static AuthorDTO fromEntity(User user) {
        return AuthorDTO.builder()
                        .id(user.getId())
                        .nickname(user.getNickname())
                        .build();
    }
}
