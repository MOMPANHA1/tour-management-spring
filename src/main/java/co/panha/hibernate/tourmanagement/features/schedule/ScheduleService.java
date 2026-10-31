package co.panha.hibernate.tourmanagement.features.schedule;

import co.panha.hibernate.tourmanagement.features.schedule.dto.AssignGuideRequest;
import co.panha.hibernate.tourmanagement.features.schedule.dto.CancelScheduleRequest;
import co.panha.hibernate.tourmanagement.features.schedule.dto.CreateScheduleRequest;
import co.panha.hibernate.tourmanagement.features.schedule.dto.ScheduleResponse;
import co.panha.hibernate.tourmanagement.features.schedule.dto.UpdateScheduleRequest;

import java.time.LocalDate;
import java.util.List;

// Departure schedule service (F5).
public interface ScheduleService {

    // UC5.1: create a schedule.
    ScheduleResponse createNew(CreateScheduleRequest request);

    // UC5.2: open schedules of a tour.
    List<ScheduleResponse> findByTour(Long tourId, LocalDate fromDate);

    // UC5.3: one schedule with remaining seats.
    ScheduleResponse findById(Long id);

    // UC5.6: update a schedule (PATCH).
    ScheduleResponse updateById(Long id, UpdateScheduleRequest request);

    // UC5.4: assign a guide.
    ScheduleResponse assignGuide(Long id, AssignGuideRequest request);

    // UC5.5: cancel a schedule.
    ScheduleResponse cancel(Long id, CancelScheduleRequest request);

    // Daily job: past departure -> DEPARTED, past return -> COMPLETED.
    void refreshScheduleStatuses();
}
