package com.stemlink.skillmentor.controllers;



import com.stemlink.skillmentor.dto.request.MentorRequestDTO;
import com.stemlink.skillmentor.dto.response.MentorResponseDTO;
import com.stemlink.skillmentor.entities.Mentor;
import com.stemlink.skillmentor.security.UserPrincipal;
import com.stemlink.skillmentor.services.MentorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.stemlink.skillmentor.constants.UserRoles.*;

@RestController
@RequestMapping(path = "/api/v1/mentors")
@RequiredArgsConstructor
@Validated
//@PreAuthorize("isAuthenticated()") // Allow all authenticated users to access mentor endpoints, but specific actions are further restricted by method-level security annotations
public class MentorController extends AbstractController {

    private final MentorService mentorService;
    private final ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<Page<MentorResponseDTO>> getAllMentors(
            @RequestParam(required = false) String name,
            Pageable pageable) {

        Page<Mentor> mentors = mentorService.getAllMentors(name, pageable);
        Page<MentorResponseDTO> dtoPage = mentors.map(
                mentor -> modelMapper.map(mentor, MentorResponseDTO.class)
        );
        return sendOkResponse(dtoPage);
    }

    @GetMapping("{id}")
    public ResponseEntity<MentorResponseDTO> getMentorById(@PathVariable Long id) {
        Mentor mentor = mentorService.getMentorById(id);
        MentorResponseDTO dto = modelMapper.map(mentor, MentorResponseDTO.class);
        return sendOkResponse(dto);
    }

    @GetMapping("/profile/{mentorId}")
    public ResponseEntity<MentorResponseDTO> getMentorByMentorId(
            @PathVariable String mentorId) {

        Mentor mentor = mentorService.getMentorByMentorId(mentorId);

        MentorResponseDTO dto = modelMapper.map(mentor, MentorResponseDTO.class);

        return sendOkResponse(dto);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('" + ROLE_ADMIN + "', '" + ROLE_MENTOR + "')")
    public ResponseEntity<MentorResponseDTO> createMentor(@Valid @RequestBody MentorRequestDTO mentorRequestDTO, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Mentor mentor = modelMapper.map(mentorRequestDTO, Mentor.class);

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin || mentorRequestDTO.getMentorId() == null) {
            // MENTOR role, or ADMIN without explicit identity fields in body → use JWT claims
            mentor.setMentorId(userPrincipal.getId());
            mentor.setFirstName(userPrincipal.getFirstName());
            mentor.setLastName(userPrincipal.getLastName());
            mentor.setEmail(userPrincipal.getEmail());
        }
        // else: ADMIN provided mentorId (+ firstName/lastName/email) in body → ModelMapper already mapped them

        Mentor createdMentor = mentorService.createNewMentor(mentor);
        MentorResponseDTO mentorResponseDTO = modelMapper.map(createdMentor, MentorResponseDTO.class);

        return sendCreatedResponse(mentorResponseDTO);
    }

    @PutMapping("{id}")
    @PreAuthorize("hasAnyRole('" + ROLE_ADMIN + "', '" + ROLE_MENTOR + "')")
    public ResponseEntity<MentorResponseDTO> updateMentor(
            @PathVariable Long id,
            @Valid @RequestBody MentorRequestDTO updatedMentorDTO,
            Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Mentor existingMentor = mentorService.getMentorById(id);

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !existingMentor.getMentorId().equals(userPrincipal.getId())) {
            throw new AccessDeniedException("You are not allowed to update this mentor");
        }

        Mentor mentor = modelMapper.map(updatedMentorDTO, Mentor.class);

        Mentor updatedMentor = mentorService.updateMentorById(id, mentor);

        MentorResponseDTO responseDTO = modelMapper.map(updatedMentor, MentorResponseDTO.class);

        return sendOkResponse(responseDTO);
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('" + ROLE_ADMIN + "')")
    public ResponseEntity<Void> deleteMentor(@PathVariable Long id) {
        mentorService.deleteMentor(id);
        return sendNoContentResponse();
    }
}

