package com.stemlink.skillmentor.controllers;

import com.stemlink.skillmentor.dto.request.ReviewRequestDTO;
import com.stemlink.skillmentor.dto.response.ReviewResponseDTO;
import com.stemlink.skillmentor.services.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ReviewResponseDTO createReview(@RequestBody ReviewRequestDTO request) {
        return reviewService.createReview(request);
    }

    @GetMapping("/mentor/{mentorId}")
    public List<ReviewResponseDTO> getMentorReviews(@PathVariable Long mentorId) {
        return reviewService.getReviewsByMentor(mentorId);
    }
}
