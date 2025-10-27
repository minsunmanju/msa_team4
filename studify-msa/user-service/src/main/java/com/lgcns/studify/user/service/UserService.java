package com.lgcns.studify.user.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lgcns.studify.user.entity.User;
import com.lgcns.studify.user.enums.UserStatus;
import com.lgcns.studify.user.dto.UserDtos.Create;
import com.lgcns.studify.user.dto.UserDtos.Response;
import com.lgcns.studify.user.dto.UserDtos.Update;
import com.lgcns.studify.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Response createUser(Create request) {
        // 이메일, 닉네임 중복 검사
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        if (request.nickname() != null && userRepository.existsByNickname(request.nickname())) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);
        return toResponse(savedUser);
    }

    public List<Response> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Response getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return toResponse(user);
    }

    public Response getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return toResponse(user);
    }

    @Transactional
    public Response updateUser(Long id, Update request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 닉네임 중복 검사 (자신 제외)
        if (request.nickname() != null && !request.nickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.nickname())) {
                throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
            }
            user.setNickname(request.nickname());
        }

        // 상태 변경
        if (request.status() != null) {
            user.setStatus(request.status());
        }

        // 비밀번호 변경
        if (request.password() != null && !request.password().trim().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        User updatedUser = userRepository.save(user);
        return toResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // Soft delete
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    public boolean checkEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean checkNicknameExists(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    private Response toResponse(User user) {
        return new Response(
            user.getId(),
            user.getEmail(),
            user.getNickname(),
            user.getStatus(),
            user.getLastLoginAt(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}