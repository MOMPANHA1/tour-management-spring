package co.panha.hibernate.tourmanagement.features.booking;

import co.panha.hibernate.tourmanagement.base.PageResponse;
import co.panha.hibernate.tourmanagement.features.booking.dto.BookingDetailResponse;
import co.panha.hibernate.tourmanagement.features.booking.dto.BookingResponse;
import co.panha.hibernate.tourmanagement.features.booking.dto.CancelBookingRequest;
import co.panha.hibernate.tourmanagement.features.booking.dto.CreateBookingRequest;
import co.panha.hibernate.tourmanagement.features.booking.dto.UpdatePassengersRequest;

import java.time.LocalDate;

/**
 * សេវាកម្មការកក់ — F7។
 *
 * <p>រាល់ method ដែលពាក់ព័ន្ធនឹងអតិថិជនទាញអត្តសញ្ញាណពី JWT ខ្លួនឯង
 * ({@code AuthUtils}) — មិនទទួល {@code customerId} ពី client ទេ។
 */
public interface BookingService {

    /** UC7.1 — អតិថិជនកក់ Tour។ */
    BookingDetailResponse bookTour(CreateBookingRequest request);

    /** UC7.2 — ការកក់របស់ខ្លួនឯង។ */
    PageResponse<BookingResponse> findMyBookings(BookingStatus status, Integer page, Integer size);

    /** UC7.3 — លម្អិតការកក់មួយ (ម្ចាស់ ឬ ADMIN)។ */
    BookingDetailResponse findByCode(String code);

    /** UC7.4 — អតិថិជនលុបចោលការកក់។ */
    BookingResponse cancel(String code, CancelBookingRequest request);

    /** UC7.5 — ADMIN បញ្ជាក់ការកក់។ */
    BookingResponse confirm(String code);

    /** UC7.6 — ADMIN មើលការកក់ទាំងអស់។ */
    PageResponse<BookingResponse> findAll(BookingStatus status, LocalDate fromDate, LocalDate toDate,
                                          Integer page, Integer size);

    /** UC7.7 — អតិថិជនកែចំនួន និងបញ្ជីអ្នកដំណើរ។ */
    BookingDetailResponse updatePassengers(String code, UpdatePassengersRequest request);

    /** Job — លុបចោលការកក់ {@code PENDING} ដែលមិនបានទូទាត់ក្នុង ២៤ ម៉ោង។ */
    void autoCancelExpiredPending();
}
