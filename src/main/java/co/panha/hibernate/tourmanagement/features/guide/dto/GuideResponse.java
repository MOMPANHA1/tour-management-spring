package co.panha.hibernate.tourmanagement.features.guide.dto;

import co.panha.hibernate.tourmanagement.base.Gender;
import co.panha.hibernate.tourmanagement.features.guide.GuideStatus;

import java.util.Set;

// Guide data returned to the client; assignedScheduleCount is always 0 until TourSchedule exists (F5).
public record GuideResponse(
        Long id,
        String code,
        String fullName,
        Gender gender,
        String phoneNumber,
        String email,
        Set<String> languages,
        Integer yearsExperience,
        String bio,
        String photoUrl,
        GuideStatus status,
        long assignedScheduleCount
) {
}
