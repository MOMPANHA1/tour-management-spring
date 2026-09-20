package co.panha.hibernate.tourmanagement.features.schedule;

import co.panha.hibernate.tourmanagement.features.schedule.dto.AssignGuideRequest;
import co.panha.hibernate.tourmanagement.features.schedule.dto.CancelScheduleRequest;
import co.panha.hibernate.tourmanagement.features.schedule.dto.CreateScheduleRequest;
import co.panha.hibernate.tourmanagement.features.schedule.dto.ScheduleResponse;
import co.panha.hibernate.tourmanagement.features.schedule.dto.UpdateScheduleRequest;

import java.time.LocalDate;
import java.util.List;

/**
 * សេវាកម្មកាលវិភាគចេញដំណើរ — F5។
 */
public interface ScheduleService {

    /** UC5.1 — បង្កើតកាលវិភាគថ្មី។ */
    ScheduleResponse createNew(CreateScheduleRequest request);

    /** UC5.2 — កាលវិភាគបើកទទួលការកក់របស់ Tour មួយ។ */
    List<ScheduleResponse> findByTour(String tourUuid, LocalDate fromDate);

    /** UC5.3 — កាលវិភាគមួយ + កៅអីនៅសល់។ */
    ScheduleResponse findByUuid(String uuid);

    /** UC5.6 — កែកាលវិភាគ (PATCH)។ */
    ScheduleResponse updateByUuid(String uuid, UpdateScheduleRequest request);

    /** UC5.4 — ចាត់តាំងមគ្គុទ្ទេសក៍។ */
    ScheduleResponse assignGuide(String uuid, AssignGuideRequest request);

    /** UC5.5 — បោះបង់កាលវិភាគ។ */
    ScheduleResponse cancel(String uuid, CancelScheduleRequest request);

    /**
     * Job ប្រចាំថ្ងៃ — រំកិលស្ថានភាពតាមកាលបរិច្ឆេទ។
     *
     * <p>ក. ថ្ងៃចេញកន្លងផុត → {@code DEPARTED} · ខ. ថ្ងៃត្រឡប់កន្លងផុត → {@code COMPLETED}
     */
    void refreshScheduleStatuses();
}
