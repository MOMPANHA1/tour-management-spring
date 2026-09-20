package co.panha.hibernate.tourmanagement.features.schedule;

import co.panha.hibernate.tourmanagement.features.schedule.dto.ScheduleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

/**
 * បម្លែង {@link TourSchedule} → DTO។
 *
 * <p>{@code bookedSeats}, {@code availableSeats} និង {@code effectivePrice} បញ្ជូនចូលជា
 * អាគុយម៉ង់ ព្រោះវាត្រូវការការរាប់ក្នុង database ដែល mapper មិនធ្វើ។
 */
@Mapper(componentModel = "spring")
public interface ScheduleMapper {

    @Mapping(target = "tourUuid", source = "schedule.tour.uuid")
    @Mapping(target = "tourTitle", source = "schedule.tour.title")
    @Mapping(target = "guideUuid", source = "schedule.guide.uuid")
    @Mapping(target = "guideName", source = "schedule.guide.fullName")
    @Mapping(target = "bookedSeats", source = "bookedSeats")
    @Mapping(target = "availableSeats", source = "availableSeats")
    @Mapping(target = "effectivePrice", source = "effectivePrice")
    ScheduleResponse toResponse(TourSchedule schedule,
                                int bookedSeats,
                                int availableSeats,
                                BigDecimal effectivePrice);
}
