package co.panha.hibernate.tourmanagement.features.guide;

import co.panha.hibernate.tourmanagement.features.guide.dto.CreateGuideRequest;
import co.panha.hibernate.tourmanagement.features.guide.dto.GuideResponse;
import co.panha.hibernate.tourmanagement.features.guide.dto.UpdateGuideRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface GuideMapper {

    // code, status និង uuid កំណត់ដោយ Service; id និង audit បំពេញដោយ JPA
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Guide toEntity(CreateGuideRequest request);

    /** PATCH — field ដែល client មិនផ្ញើ (null) មិនជាន់លើតម្លៃចាស់។ */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateGuideRequest request, @MappingTarget Guide guide);

    /** {@code assignedScheduleCount} មកពី Service (ការរាប់ក្នុង database)។ */
    @Mapping(target = "assignedScheduleCount", source = "assignedScheduleCount")
    GuideResponse toResponse(Guide guide, long assignedScheduleCount);
}
