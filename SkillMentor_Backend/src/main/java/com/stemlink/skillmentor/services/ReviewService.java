package com.stemlink.skillmentor.services;

import com.stemlink.skillmentor.dto.request.ReviewRequestDTO;
import com.stemlink.skillmentor.dto.response.ReviewResponseDTO;

import java.util.List;

public interface ReviewService {

    ReviewResponseDTO createReview(ReviewRequestDTO request);
    List<ReviewResponseDTO> getReviewsByMentor(Long mentorId);
}
