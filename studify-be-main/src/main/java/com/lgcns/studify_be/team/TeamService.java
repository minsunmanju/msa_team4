package com.lgcns.studify_be.team;

import com.lgcns.studify_be.team.dto.TeamCreateRequest;
import com.lgcns.studify_be.team.dto.TeamUpdateRequest;
import com.lgcns.studify_be.team.dto.TeamResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;

    // ===== CREATE =====
    @Transactional
    public TeamResponse create(TeamCreateRequest req) {
        Team t = new Team();
        t.setName(req.name());
        t.setOwnerId(req.ownerId());
        t.setDescription(req.description());
        t.setMaxMembers(req.maxMembers());

        t.setVisibility("PUBLIC");
        t.setStatus("ACTIVE");

        return toResponse(teamRepository.save(t));
    }

    // ===== READ (단건 DTO) =====
    @Transactional(readOnly = true)
    public TeamResponse getOne(Long id) {
        Team t = teamRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found: " + id));
        return toResponse(t);
    }

    // ===== READ (전체 리스트 DTO) =====
    @Transactional(readOnly = true)
    public List<TeamResponse> getAll() {
        return teamRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    // ===== READ (페이징 DTO) =====
    @Transactional(readOnly = true)
    public Page<TeamResponse> getPage(Pageable pageable) {
        return teamRepository.findAll(pageable).map(this::toResponse);
    }

    // ===== UPDATE =====
    @Transactional
    public TeamResponse update(Long id, TeamUpdateRequest req) {
        Team t = teamRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found: " + id));

        t.setName(req.name());
        t.setOwnerId(req.ownerId());
        t.setDescription(req.description());
        t.setMaxMembers(req.maxMembers());

        return toResponse(t);
    }

    // ===== DELETE =====
    @Transactional
    public void delete(Long id) {
        Team t = teamRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found: " + id));
        teamRepository.delete(t);
    }

    // ===== Mapper: Entity -> DTO =====
    private TeamResponse toResponse(Team t) {
        return new TeamResponse(
                t.getId(),
                t.getName(),
                t.getOwnerId(),
                t.getDescription(),
                t.getVisibility(),
                t.getMaxMembers(),
                t.getStatus(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }
}
