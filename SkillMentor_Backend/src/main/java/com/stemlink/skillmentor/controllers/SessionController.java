package com.stemlink.skillmentor.controllers;

import com.stemlink.skillmentor.dto.request.SessionRequestDTO;
import com.stemlink.skillmentor.dto.response.SessionResponseDTO;
import com.stemlink.skillmentor.entities.Session;
import com.stemlink.skillmentor.security.UserPrincipal;
import com.stemlink.skillmentor.services.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/v1/sessions")
@RequiredArgsConstructor
@Validated
@PreAuthorize("isAuthenticated()")
public class SessionController extends AbstractController {

    private final SessionService sessionService;

    // Admin only
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<SessionResponseDTO>> getAllSessions(Pageable pageable) {

        Page<Session> sessions = sessionService.getAllSessions(pageable);

        Page<SessionResponseDTO> response = sessions.map(this::toSessionResponseDTO);

        return sendOkResponse(response);
    }

    // Admin or Mentor
    @GetMapping("{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MENTOR')")
    public ResponseEntity<SessionResponseDTO> getSessionById(@PathVariable Long id) {

        Session session = sessionService.getSessionById(id);

        return sendOkResponse(toSessionResponseDTO(session));
    }

    // Admin creates sessions manually
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SessionResponseDTO> createSession(
            @Valid @RequestBody SessionRequestDTO sessionDTO) {

        Session session = sessionService.createNewSession(sessionDTO);

        return sendCreatedResponse(toSessionResponseDTO(session));
    }

    // Admin updates sessions
    @PutMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SessionResponseDTO> updateSession(
            @PathVariable Long id,
            @Valid @RequestBody SessionRequestDTO updatedSessionDTO) {

        Session session = sessionService.updateSessionById(id, updatedSessionDTO);

        return sendOkResponse(toSessionResponseDTO(session));
    }

    // Admin delete
    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {

        sessionService.deleteSession(id);

        return sendNoContentResponse();
    }

    // Student enroll to a session
    @PostMapping("/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SessionResponseDTO> enroll(
            @RequestBody SessionRequestDTO sessionDTO,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Session session = sessionService.enrollSession(userPrincipal, sessionDTO);

        return sendCreatedResponse(toSessionResponseDTO(session));
    }

    // Logged student sessions
    @GetMapping("/my-sessions")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<SessionResponseDTO>> getMySessions(Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        List<Session> sessions =
                sessionService.getSessionsByStudentEmail(userPrincipal.getEmail());

        List<SessionResponseDTO> response = sessions.stream()
                .map(this::toSessionResponseDTO)
                .collect(Collectors.toList());

        return sendOkResponse(response);
    }

    // Mapper
    private SessionResponseDTO toSessionResponseDTO(Session session) {

        SessionResponseDTO sessionResponseDTO = new SessionResponseDTO();

        sessionResponseDTO.setId(session.getId());
        sessionResponseDTO.setMentorName(session.getMentor().getFirstName() + " " +
                session.getMentor().getLastName());

        sessionResponseDTO.setMentorProfileImageUrl(session.getMentor().getProfileImageUrl());
        sessionResponseDTO.setSubjectName(session.getSubject().getSubjectName());
        sessionResponseDTO.setSessionAt(session.getSessionAt());
        sessionResponseDTO.setDurationMinutes(session.getDurationMinutes());

        sessionResponseDTO.setSessionStatus(session.getSessionStatus().name());
        sessionResponseDTO.setPaymentStatus(session.getPaymentStatus().name());

        sessionResponseDTO.setMeetingLink(session.getMeetingLink());

        return sessionResponseDTO;
    }
}