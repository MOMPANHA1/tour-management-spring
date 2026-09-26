package co.panha.hibernate.tourmanagement.features.booking;

import co.panha.hibernate.tourmanagement.base.PageResponse;
import co.panha.hibernate.tourmanagement.features.booking.dto.BookingDetailResponse;
import co.panha.hibernate.tourmanagement.features.booking.dto.BookingResponse;
import co.panha.hibernate.tourmanagement.features.booking.dto.CancelBookingRequest;
import co.panha.hibernate.tourmanagement.features.booking.dto.CreateBookingRequest;
import co.panha.hibernate.tourmanagement.features.booking.dto.UpdatePassengersRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * F7 — ការកក់។
 *
 * <p>ធនធាននេះប្រើ {@code code} ({@code BK-20260912-0001}) ជាកូនសោលើ URL ជំនួស {@code id} —
 * លេខកូដជាអ្វីដែលអតិថិជនឃើញលើប័ណ្ណបញ្ជាក់ ហើយវាមិនបង្ហាញចំនួនការកក់សរុបក្នុងប្រព័ន្ធដូច
 * លេខតាមលំដាប់ទេ។
 *
 * <p>សិទ្ធិកំណត់ក្នុង {@code security/SecurityConfig}៖ {@code /confirm} និងបញ្ជីទាំងអស់
 * សម្រាប់ {@code ADMIN} · ឯផ្លូវដែលនៅសល់សម្រាប់ {@code CUSTOMER}។
 */
@Tag(name = "Booking")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDetailResponse bookTour(@Valid @RequestBody CreateBookingRequest request) {
        return bookingService.bookTour(request);
    }

    /**
     * ត្រូវប្រកាស<b>មុន</b> {@code @GetMapping("/{code}")} ដើម្បីកុំឲ្យ {@code me}
     * ក្លាយជាលេខកូដការកក់។
     */
    @GetMapping("/me")
    public PageResponse<BookingResponse> findMyBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return bookingService.findMyBookings(status, page, size);
    }

    @GetMapping("/{code}")
    public BookingDetailResponse findByCode(@PathVariable String code) {
        return bookingService.findByCode(code);
    }

    @PatchMapping("/{code}/cancel")
    public BookingResponse cancel(@PathVariable String code,
                                  @Valid @RequestBody CancelBookingRequest request) {
        return bookingService.cancel(code, request);
    }

    @PatchMapping("/{code}/passengers")
    public BookingDetailResponse updatePassengers(@PathVariable String code,
                                                  @Valid @RequestBody UpdatePassengersRequest request) {
        return bookingService.updatePassengers(code, request);
    }

    @PatchMapping("/{code}/confirm")
    public BookingResponse confirm(@PathVariable String code) {
        return bookingService.confirm(code);
    }

    @GetMapping
    public PageResponse<BookingResponse> findAll(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return bookingService.findAll(status, fromDate, toDate, page, size);
    }
}
