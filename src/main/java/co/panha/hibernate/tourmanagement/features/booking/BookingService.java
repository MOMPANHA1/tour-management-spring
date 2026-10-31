package co.panha.hibernate.tourmanagement.features.booking;

import co.panha.hibernate.tourmanagement.base.PageResponse;
import co.panha.hibernate.tourmanagement.features.booking.dto.BookingDetailResponse;
import co.panha.hibernate.tourmanagement.features.booking.dto.BookingResponse;
import co.panha.hibernate.tourmanagement.features.booking.dto.CancelBookingRequest;
import co.panha.hibernate.tourmanagement.features.booking.dto.CreateBookingRequest;
import co.panha.hibernate.tourmanagement.features.booking.dto.UpdatePassengersRequest;

import java.time.LocalDate;

// F7 booking service; customer identity comes from the JWT (AuthUtils), never from the client.
public interface BookingService {

    // UC7.1 customer books a tour.
    BookingDetailResponse bookTour(CreateBookingRequest request);

    // UC7.2 the customer's own bookings.
    PageResponse<BookingResponse> findMyBookings(BookingStatus status, Integer page, Integer size);

    // UC7.3 booking detail (owner or ADMIN).
    BookingDetailResponse findByCode(String code);

    // UC7.4 customer cancels a booking.
    BookingResponse cancel(String code, CancelBookingRequest request);

    // UC7.5 ADMIN confirms a booking.
    BookingResponse confirm(String code);

    // UC7.6 ADMIN lists all bookings.
    PageResponse<BookingResponse> findAll(BookingStatus status, LocalDate fromDate, LocalDate toDate,
                                          Integer page, Integer size);

    // UC7.7 customer edits party size and passenger list.
    BookingDetailResponse updatePassengers(String code, UpdatePassengersRequest request);

    // Job: cancels PENDING bookings left unpaid for 24 hours.
    void autoCancelExpiredPending();
}
