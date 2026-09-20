package co.panha.hibernate.tourmanagement.features.destination;

import co.panha.hibernate.tourmanagement.features.destination.dto.CreateDestinationRequest;
import co.panha.hibernate.tourmanagement.features.destination.dto.DestinationResponse;
import co.panha.hibernate.tourmanagement.features.destination.dto.UpdateDestinationRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface DestinationMapper {

    // uuid និង country កំណត់ដោយ Service; id និង audit បំពេញដោយ JPA
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Destination toEntity(CreateDestinationRequest request);

    /** PATCH — field ដែល client មិនផ្ញើ (null) មិនជាន់លើតម្លៃចាស់។ */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateDestinationRequest request, @MappingTarget Destination destination);

    /** {@code tourCount} មកពី Service (ការរាប់ក្នុង database) មិនមែនពី entity ទេ។ */
    @Mapping(target = "tourCount", source = "tourCount")
    DestinationResponse toResponse(Destination destination, long tourCount);
}
