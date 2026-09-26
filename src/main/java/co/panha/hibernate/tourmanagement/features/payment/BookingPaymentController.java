package co.panha.hibernate.tourmanagement.features.payment;

import co.panha.hibernate.tourmanagement.features.payment.dto.CreatePaymentRequest;
import co.panha.hibernate.tourmanagement.features.payment.dto.CreateRefundRequest;
import co.panha.hibernate.tourmanagement.features.payment.dto.PaymentResponse;
import co.panha.hibernate.tourmanagement.features.payment.dto.PaymentSummaryResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * ការទូទាត់ជាធនធានរងរបស់ការកក់ — ដាក់ដាច់ពី {@code PaymentController}
 * ព្រោះ base path ខុសគ្នា ដូចលំនាំ {@code TourScheduleController} ដែរ។
 */
@Tag(name = "Payment")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bookings/{code}")
public class BookingPaymentController {

    private final PaymentService paymentService;

    @PostMapping("/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse pay(@PathVariable String code,
                               @Valid @RequestBody CreatePaymentRequest request) {
        return paymentService.pay(code, request);
    }

    @GetMapping("/payments")
    public PaymentSummaryResponse getSummary(@PathVariable String code) {
        return paymentService.getSummary(code);
    }

    @PostMapping("/refunds")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createRefund(@PathVariable String code,
                                        @Valid @RequestBody CreateRefundRequest request) {
        return paymentService.createRefund(code, request);
    }
}
