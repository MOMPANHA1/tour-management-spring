package co.panha.hibernate.tourmanagement.features.payment;

import co.panha.hibernate.tourmanagement.features.payment.dto.PaymentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "bookingCode", source = "booking.code")
    PaymentResponse toResponse(Payment payment);

    List<PaymentResponse> toResponses(List<Payment> payments);
}
