package co.panha.hibernate.tourmanagement.features.review.dto;

import java.time.LocalDateTime;
import java.util.List;

// Review returned to clients; customerName is masked because reviews are public.
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
