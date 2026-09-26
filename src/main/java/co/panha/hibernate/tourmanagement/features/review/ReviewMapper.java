package co.panha.hibernate.tourmanagement.features.review;

import co.panha.hibernate.tourmanagement.features.review.dto.CreateReviewRequest;
import co.panha.hibernate.tourmanagement.features.review.dto.ReviewResponse;
import co.panha.hibernate.tourmanagement.features.review.dto.UpdateReviewRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    // booking, tour, customer និង isVisible កំណត់ដោយ Service; id និង audit បំពេញដោយ JPA
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "booking", ignore = true)
    @Mapping(target = "tour", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "isVisible", ignore = true)
    @Mapping(target = "hiddenReason", ignore = true)
    @Mapping(target = "adminReply", ignore = true)
    @Mapping(target = "repliedAt", ignore = true)
    @Mapping(target = "repliedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Review toEntity(CreateReviewRequest request);

    /** PATCH — field ដែល client មិនផ្ញើ (null) មិនជាន់លើតម្លៃចាស់។ */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "booking", ignore = true)
    @Mapping(target = "tour", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "isVisible", ignore = true)
    @Mapping(target = "hiddenReason", ignore = true)
    @Mapping(target = "adminReply", ignore = true)
    @Mapping(target = "repliedAt", ignore = true)
    @Mapping(target = "repliedBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateReviewRequest request, @MappingTarget Review review);

    /** {@code customerName} បិទបាំងរួចមកពី Service។ */
    @Mapping(target = "bookingCode", source = "review.booking.code")
    @Mapping(target = "tourId", source = "review.tour.id")
    @Mapping(target = "tourTitle", source = "review.tour.title")
    @Mapping(target = "customerAvatarUrl", source = "review.customer.avatarUrl")
    @Mapping(target = "customerName", source = "customerName")
    ReviewResponse toResponse(Review review, String customerName);
}
