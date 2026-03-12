package com.stemlink.skillmentor.services.impl;

import com.stemlink.skillmentor.Repositories.MentorRepository;
import com.stemlink.skillmentor.Repositories.ReviewRepository;
import com.stemlink.skillmentor.dto.request.ReviewRequestDTO;
import com.stemlink.skillmentor.dto.response.ReviewResponseDTO;
import com.stemlink.skillmentor.entities.Mentor;
import com.stemlink.skillmentor.entities.Review;
import com.stemlink.skillmentor.services.ReviewService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final MentorRepository mentorRepository;
    private final ModelMapper modelMapper;

    @Override
    public ReviewResponseDTO createReview(ReviewRequestDTO request) {

        Mentor mentor = mentorRepository.findById(request.getMentorId())
                .orElseThrow(() -> new RuntimeException("Mentor not found"));

        Review review = new Review();
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setMentor(mentor);
        review.setCreatedAt(LocalDateTime.now());

        Review saved = reviewRepository.save(review);

        return modelMapper.map(saved, ReviewResponseDTO.class);
    }

    @Override
    public List<ReviewResponseDTO> getReviewsByMentor(Long mentorId) {

        Mentor mentor = mentorRepository.findById(mentorId)
                .orElseThrow(() -> new RuntimeException("Mentor not found"));

        List<Review> reviews = reviewRepository.findByMentorId(mentor.getId());

        return reviews.stream()
                .map(review -> modelMapper.map(review, ReviewResponseDTO.class))
                .toList();
    }

}
