package com.stemlink.skillmentor.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewResponseDTO {

    private Long id;
    private int rating;
    private String comment;
    private Long mentorId;
    private Long studentId;
    private LocalDateTime createdAt;
}
