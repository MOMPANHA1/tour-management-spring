package co.panha.hibernate.tourmanagement.features.category;

import co.panha.hibernate.tourmanagement.features.category.dto.CategoryResponse;
import co.panha.hibernate.tourmanagement.features.category.dto.CreateCategoryRequest;
import co.panha.hibernate.tourmanagement.features.category.dto.UpdateCategoryRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

// Maps Category <-> DTOs; MapStruct generates the implementation at compile time.
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    // slug is set by the service; id and audit fields are filled by JPA.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toEntity(CreateCategoryRequest request);

    // PATCH update: IGNORE keeps old values for fields the client omits (null).
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateCategoryRequest request, @MappingTarget Category category);

    // tourCount comes from the service (database count), not from the entity.
    @Mapping(target = "tourCount", source = "tourCount")
    CategoryResponse toResponse(Category category, long tourCount);
}
