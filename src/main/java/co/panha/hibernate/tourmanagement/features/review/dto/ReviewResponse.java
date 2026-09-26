package co.panha.hibernate.tourmanagement.features.review.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ការវាយតម្លៃដែលត្រឡប់ទៅ client។
 *
 * @param customerName ឈ្មោះ<b>បិទបាំងផ្នែក</b> ឧ. {@code "Sok D***"} — ការវាយតម្លៃជា
 *                     ទិន្នន័យសាធារណៈ ដូច្នេះមិនបង្ហាញឈ្មោះពេញរបស់អតិថិជនទេ។
 * @param bookingCode  លេខកូដការកក់ — ភស្តុតាងថាអ្នកវាយតម្លៃធ្លាប់ទៅដំណើរពិត។
 */
public record ReviewResponse(
        Long id,
        String bookingCode,
        Long tourId,
        String tourTitle,
        String customerName,
        String customerAvatarUrl,
        Integer rating,
        Integer guideRating,
        Integer valueRating,
        String title,
        String comment,
        List<String> imageUrls,
        String adminReply,
        LocalDateTime repliedAt,
        LocalDateTime createdAt
) {
}
