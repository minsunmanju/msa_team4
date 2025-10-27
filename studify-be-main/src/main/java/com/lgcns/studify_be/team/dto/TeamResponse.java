package com.lgcns.studify_be.team.dto;


import java.time.LocalDateTime;

public record TeamResponse(
        Long id,
        String name,
        Long ownerId,
        String description,
        String visibility,
        Integer maxMembers,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
