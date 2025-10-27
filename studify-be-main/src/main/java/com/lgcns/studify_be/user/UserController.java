package com.lgcns.studify_be.user;

import com.lgcns.studify_be.user.dto.UserDtos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/studify/api/v1/users") // ✅ 모든 엔드포인트 기본 경로
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    // ✅ 회원가입 (공식 생성 API)
    @PostMapping
    public ResponseEntity<UserDtos.Response> create(@Valid @RequestBody UserDtos.Create req) {
        User u = service.create(req); // UserService 호출 → 중복체크 + 비번해시
        return ResponseEntity
                // ✅ Location 헤더: 생성된 유저의 조회 URI
                .created(java.net.URI.create("/studify/api/v1/users/" + u.getId()))
                .body(toRes(u)); // 응답 DTO 변환
    }

    // ✅ 단건 조회
    @GetMapping("/{id}")
    public UserDtos.Response get(@PathVariable Long id) { 
        return toRes(service.get(id)); 
    }

    // ✅ 수정 (닉네임, 상태, 비밀번호 변경 가능)
    @PutMapping("/{id}")
    public UserDtos.Response update(@PathVariable Long id, @Valid @RequestBody UserDtos.Update req) {
        return toRes(service.update(id, req));
    }

    // ✅ 삭제
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { 
        service.delete(id); 
    }

    // ✅ 전체 리스트 페이징 조회
    @GetMapping
    public Page<UserDtos.Response> list(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        return service.list(PageRequest.of(page, size)).map(this::toRes);
    }

    // ✅ 엔티티 → DTO 변환 (응답 전용)
    private UserDtos.Response toRes(User u) {
        return new UserDtos.Response(
                u.getId(),
                u.getEmail(),
                u.getNickname(),
                u.getStatus(),
                u.getLastLoginAt(),
                u.getCreatedAt(),
                u.getUpdatedAt()
        );
    }
}
