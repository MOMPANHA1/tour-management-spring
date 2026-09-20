package co.panha.hibernate.tourmanagement.features.guide.dto;

import co.panha.hibernate.tourmanagement.features.guide.GuideStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * ទិន្នន័យប្តូរស្ថានភាពមគ្គុទ្ទេសក៍ — UC3.5។
 */
public record UpdateGuideStatusRequest(

        @NotNull(message = "Status is required")
        GuideStatus status,

        @Size(max = 255, message = "Reason cannot exceed 255 characters")
        String reason
) {
}
