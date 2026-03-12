package com.stemlink.skillmentor.dto.request;

import lombok.Data;

@Data
public class ReviewRequestDTO {

    private Long mentorId;
    private int rating;
    private String comment;

}
