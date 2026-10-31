package co.panha.hibernate.tourmanagement.features.booking;

import co.panha.hibernate.tourmanagement.features.booking.dto.BookingDetailResponse;
import co.panha.hibernate.tourmanagement.features.booking.dto.BookingResponse;
import co.panha.hibernate.tourmanagement.features.booking.dto.PassengerRequest;
import co.panha.hibernate.tourmanagement.features.booking.dto.PassengerResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    // booking is set by Booking.addPassenger(); id is filled by JPA
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "booking", ignore = true)
    @Mapping(target = "isLead", expression = "java(Boolean.TRUE.equals(request.isLead()))")
    Passenger toPassenger(PassengerRequest request);

    PassengerResponse toPassengerResponse(Passenger passenger);

    List<PassengerResponse> toPassengerResponses(List<Passenger> passengers);

    // customerName comes from the service: null for the customer's own view, set only for ADMIN.
    @Mapping(target = "tourId", source = "booking.schedule.tour.id")
    @Mapping(target = "tourTitle", source = "booking.schedule.tour.title")
    @Mapping(target = "scheduleId", source = "booking.schedule.id")
    @Mapping(target = "departureDate", source = "booking.schedule.departureDate")
    @Mapping(target = "returnDate", source = "booking.schedule.returnDate")
    @Mapping(target = "meetingPoint", source = "booking.schedule.meetingPoint")
    @Mapping(target = "remainingAmount", expression = "java(booking.remainingAmount())")
    @Mapping(target = "customerName", source = "customerName")
    BookingResponse toResponse(Booking booking, String customerName);

    @Mapping(target = "tourId", source = "booking.schedule.tour.id")
    @Mapping(target = "tourTitle", source = "booking.schedule.tour.title")
    @Mapping(target = "scheduleId", source = "booking.schedule.id")
    @Mapping(target = "departureDate", source = "booking.schedule.departureDate")
    @Mapping(target = "returnDate", source = "booking.schedule.returnDate")
    @Mapping(target = "meetingPoint", source = "booking.schedule.meetingPoint")
    @Mapping(target = "guideName", source = "booking.schedule.guide.fullName")
    @Mapping(target = "remainingAmount", expression = "java(booking.remainingAmount())")
    @Mapping(target = "customerName", source = "customerName")
    @Mapping(target = "passengers", source = "booking.passengers")
    BookingDetailResponse toDetailResponse(Booking booking, String customerName);
}
