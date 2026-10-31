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

    // country is set by the service; id and audit fields are filled by JPA.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Destination toEntity(CreateDestinationRequest request);

    // PATCH: null fields from the client do not overwrite existing values.
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateDestinationRequest request, @MappingTarget Destination destination);

    // tourCount comes from the service (database count), not from the entity.
    @Mapping(target = "tourCount", source = "tourCount")
    DestinationResponse toResponse(Destination destination, long tourCount);
}
