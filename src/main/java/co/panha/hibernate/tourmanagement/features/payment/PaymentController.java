package co.panha.hibernate.tourmanagement.features.payment;

import co.panha.hibernate.tourmanagement.features.payment.dto.PaymentResponse;
import co.panha.hibernate.tourmanagement.features.payment.dto.RejectPaymentRequest;
import co.panha.hibernate.tourmanagement.features.payment.dto.RevenueReportResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * មុខងារទូទាត់សម្រាប់ ADMIN — ការពិនិត្យ និងរបាយការណ៍។
 */
@Tag(name = "Payment")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * ត្រូវប្រកាស<b>មុន</b> {@code /{ref}/...} ដើម្បីកុំឲ្យ {@code report}
     * ក្លាយជាលេខយោងការទូទាត់។
     */
    @GetMapping("/report")
    public RevenueReportResponse getRevenueReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return paymentService.getRevenueReport(fromDate, toDate);
    }

    @PatchMapping("/{referenceNo}/verify")
    public PaymentResponse verify(@PathVariable String referenceNo) {
        return paymentService.verify(referenceNo);
    }

    @PatchMapping("/{referenceNo}/reject")
    public PaymentResponse reject(@PathVariable String referenceNo,
                                  @Valid @RequestBody RejectPaymentRequest request) {
        return paymentService.reject(referenceNo, request);
    }
}
