package co.panha.hibernate.tourmanagement.features.tour;

import co.panha.hibernate.tourmanagement.features.tour.dto.CreateTourRequest;
import co.panha.hibernate.tourmanagement.features.tour.dto.TourCardResponse;
import co.panha.hibernate.tourmanagement.features.tour.dto.TourDetailResponse;
import co.panha.hibernate.tourmanagement.features.tour.dto.TourImageRequest;
import co.panha.hibernate.tourmanagement.features.tour.dto.TourImageResponse;
import co.panha.hibernate.tourmanagement.features.tour.dto.UpdateTourRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDate;

/**
 * បម្លែង {@link Tour} ↔ DTO។
 *
 * <p>ទំនាក់ទំនង ({@code category}, {@code destinations}, {@code images}) កំណត់ដោយ Service
 * ព្រោះវាត្រូវការទាញ entity ពី database ជាមុន — mapper មិនប៉ះ database ទេ។
 */
@Mapper(componentModel = "spring")
public interface TourMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "isPublished", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "destinations", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Tour toEntity(CreateTourRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "isPublished", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "destinations", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateTourRequest request, @MappingTarget Tour tour);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tour", ignore = true)
    TourImage toImageEntity(TourImageRequest request);

    TourImageResponse toImageResponse(TourImage image);

    /** {@code nextDepartureDate} មកពី Service (ScheduleRepository) — mapper មិនប៉ះ database។ */
    @Mapping(target = "categoryName", source = "tour.category.name")
    @Mapping(target = "nextDepartureDate", source = "nextDepartureDate")
    TourCardResponse toCardResponse(Tour tour, LocalDate nextDepartureDate);

    @Mapping(target = "categoryName", source = "tour.category.name")
    @Mapping(target = "categoryUuid", source = "tour.category.uuid")
    @Mapping(target = "nextDepartureDate", source = "nextDepartureDate")
    TourDetailResponse toDetailResponse(Tour tour, LocalDate nextDepartureDate);
}
