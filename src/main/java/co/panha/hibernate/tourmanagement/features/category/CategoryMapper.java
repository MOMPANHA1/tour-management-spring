package co.panha.hibernate.tourmanagement.features.category;

import co.panha.hibernate.tourmanagement.features.category.dto.CategoryResponse;
import co.panha.hibernate.tourmanagement.features.category.dto.CreateCategoryRequest;
import co.panha.hibernate.tourmanagement.features.category.dto.UpdateCategoryRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * បម្លែង {@link Category} ↔ DTO។ MapStruct បង្កើត implementation ពេល compile។
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    // slug កំណត់ដោយ Service; id និង audit បំពេញដោយ JPA — mapper មិនប៉ះ
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toEntity(CreateCategoryRequest request);

    /**
     * កែ entity ដែលមានស្រាប់តាមលក្ខណៈ PATCH។
     *
     * <p>{@code NullValuePropertyMappingStrategy.IGNORE} ធ្វើឲ្យ field ដែល client មិនផ្ញើមក
     * (null) <b>មិនត្រូវជាន់</b>លើតម្លៃចាស់។ បើគ្មានវា PATCH នឹងលុបទិន្នន័យដោយចៃដន្យ។
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UpdateCategoryRequest request, @MappingTarget Category category);

    /**
     * {@code tourCount} មកពី Service (ការរាប់ក្នុង database) មិនមែនពី entity ទេ —
     * mapper មិនប៉ះ database។
     */
    @Mapping(target = "tourCount", source = "tourCount")
    CategoryResponse toResponse(Category category, long tourCount);
}
