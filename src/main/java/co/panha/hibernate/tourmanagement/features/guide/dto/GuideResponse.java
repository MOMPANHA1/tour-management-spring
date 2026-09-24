package co.panha.hibernate.tourmanagement.features.guide.dto;

import co.panha.hibernate.tourmanagement.base.Gender;
import co.panha.hibernate.tourmanagement.features.guide.GuideStatus;

import java.util.Set;

/**
 * ទិន្នន័យមគ្គុទ្ទេសក៍ដែលត្រឡប់ទៅ client។
 *
 * @param assignedScheduleCount ចំនួនកាលវិភាគដែលបានចាត់តាំង។ <b>បច្ចុប្បន្នតែងតែ 0</b>
 *                              ព្រោះ entity {@code TourSchedule} មិនទាន់មាន (F5)។
 */
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
