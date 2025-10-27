package com.lgcns.studify_be.team;

import com.lgcns.studify_be.team.dto.TeamCreateRequest;
import com.lgcns.studify_be.team.dto.TeamResponse;
import com.lgcns.studify_be.team.dto.TeamUpdateRequest;
import com.lgcns.studify_be.team.TeamService;  // ← team 패키지

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.net.URI;

@RestController
@RequestMapping("/api/teams")  // 경로 통일 (원하면 /studify/api/v1/teams 로 바꿔도 됨)
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @Operation(summary = "팀 생성", description = "새로운 팀을 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "생성됨",
            content = @Content(schema = @Schema(implementation = TeamResponse.class))),
        @ApiResponse(responseCode = "400", description = "요청 검증 실패")
    })
    @PostMapping
    public ResponseEntity<TeamResponse> create(@RequestBody @Valid TeamCreateRequest req) {
        TeamResponse res = teamService.create(req); // 서비스가 TeamResponse 반환
        URI location = URI.create("/api/teams/" + res.id());
        return ResponseEntity.created(location).body(res);
    }

    @Operation(summary = "팀 단건 조회", description = "ID로 특정 팀을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공",
            content = @Content(schema = @Schema(implementation = TeamResponse.class))),
        @ApiResponse(responseCode = "404", description = "대상 없음")
    })
    @GetMapping("/{id}")
    public TeamResponse get(@PathVariable Long id) {
        return teamService.getOne(id); // ← 서비스 메서드명 getOne 으로 통일
    }

    @Operation(summary = "팀 목록 조회(페이징)", description = "페이지/사이즈/정렬을 적용하여 팀 목록을 조회합니다.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "성공") })
    @GetMapping
    public Page<TeamResponse> list(
            @ParameterObject
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return teamService.getPage(pageable); // ← 서비스 메서드명 getPage 로 통일
    }

    @Operation(summary = "팀 수정", description = "팀 정보를 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정됨",
            content = @Content(schema = @Schema(implementation = TeamResponse.class))),
        @ApiResponse(responseCode = "400", description = "요청 검증 실패"),
        @ApiResponse(responseCode = "404", description = "대상 없음")
    })
    @PutMapping("/{id}")
    public TeamResponse update(@PathVariable Long id, @RequestBody @Valid TeamUpdateRequest req) {
        return teamService.update(id, req);
    }

    @Operation(summary = "팀 삭제", description = "팀을 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제됨"),
        @ApiResponse(responseCode = "404", description = "대상 없음")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        teamService.delete(id);
    }
    
}
