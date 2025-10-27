package com.lgcns.studify_be.user;

import com.lgcns.studify_be.user.dto.UserDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service 
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repo;
    private final PasswordEncoder encoder;

    @Transactional
    public User create(UserDtos.Create dto) {
        // ✅ 이메일 중복 체크
        if (repo.existsByEmail(dto.email())) 
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");

        // ✅ 비밀번호 해시 처리 후 User 생성
        User u = User.builder()
                .email(dto.email())
                .passwordHash(encoder.encode(dto.password()))
                .nickname(dto.nickname())
                .status(UserStatus.ACTIVE)
                .build();

        return repo.save(u); // 저장
    }

    @Transactional(readOnly = true)
    public User get(Long id) {
        // ✅ 단건 조회 (없으면 예외)
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + id));
    }

    @Transactional
    public User update(Long id, UserDtos.Update dto) {
        User u = get(id);

        // ✅ 닉네임 업데이트
        if (dto.nickname() != null) u.setNickname(dto.nickname());

        // ✅ 상태 업데이트
        if (dto.status() != null)   u.setStatus(dto.status());

        // ✅ 비밀번호 업데이트 시 해시 처리
        if (dto.password() != null) u.setPasswordHash(encoder.encode(dto.password()));

        return u; // 더티체킹
    }

    @Transactional 
    public void delete(Long id) { 
        // ✅ 삭제
        repo.delete(get(id)); 
    }

    @Transactional(readOnly = true)
    public Page<User> list(Pageable pageable) { 
        // ✅ 페이징 리스트 조회
        return repo.findAll(pageable); 
    }
}
