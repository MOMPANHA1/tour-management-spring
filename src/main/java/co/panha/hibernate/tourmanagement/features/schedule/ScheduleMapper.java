package co.panha.hibernate.tourmanagement.features.schedule;

import co.panha.hibernate.tourmanagement.features.schedule.dto.ScheduleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

// Seat counts and effective price are passed in because they need database counts the mapper cannot do.
@Mapper(componentModel = "spring")
public interface ScheduleMapper {

    @Mapping(target = "tourId", source = "schedule.tour.id")
    @Mapping(target = "tourTitle", source = "schedule.tour.title")
    @Mapping(target = "guideId", source = "schedule.guide.id")
    @Mapping(target = "guideName", source = "schedule.guide.fullName")
    @Mapping(target = "bookedSeats", source = "bookedSeats")
    @Mapping(target = "availableSeats", source = "availableSeats")
    @Mapping(target = "effectivePrice", source = "effectivePrice")
    ScheduleResponse toResponse(TourSchedule schedule,
                                int bookedSeats,
                                int availableSeats,
                                BigDecimal effectivePrice);
}
