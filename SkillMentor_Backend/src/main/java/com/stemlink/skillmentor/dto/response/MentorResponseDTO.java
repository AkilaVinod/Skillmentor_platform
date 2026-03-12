package com.stemlink.skillmentor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;


@Data
public class MentorResponseDTO {

    private Long id;

    private String mentorId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

    private String title;
    private String profession;
    private String company;
    private int experienceYears;

    private String bio;
    private String profileImageUrl;

    private Integer positiveReviews;
    private Integer totalEnrollments;
    private Boolean isCertified;
    private String startYear;

    private Date createdAt;
    private Date updatedAt;
}
