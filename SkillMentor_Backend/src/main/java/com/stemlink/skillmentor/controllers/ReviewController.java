package com.stemlink.skillmentor.controllers;

import com.stemlink.skillmentor.dto.request.ReviewRequestDTO;
import com.stemlink.skillmentor.dto.response.ReviewResponseDTO;
import jakarta.validation.Valid;
import com.stemlink.skillmentor.security.UserPrincipal;
import com.stemlink.skillmentor.services.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController extends AbstractController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ReviewResponseDTO> createReview(
            @Valid @RequestBody ReviewRequestDTO request,
            Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        ReviewResponseDTO dto = reviewService.createReview(request, userPrincipal.getEmail());
        return sendCreatedResponse(dto);
    }

    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<List<ReviewResponseDTO>> getMentorReviews(@PathVariable Long mentorId) {
        return sendOkResponse(reviewService.getReviewsByMentor(mentorId));
    }
}
