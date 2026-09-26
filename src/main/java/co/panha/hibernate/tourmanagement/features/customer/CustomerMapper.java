package co.panha.hibernate.tourmanagement.features.customer;

import co.panha.hibernate.tourmanagement.features.customer.dto.CustomerResponse;
import co.panha.hibernate.tourmanagement.features.customer.dto.PatchCustomerRequest;
import co.panha.hibernate.tourmanagement.features.customer.dto.RegisterCustomerRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    // keycloakId និង status កំណត់ដោយ Service; id និង audit បំពេញដោយ JPA។
    // password មិន map ទេ — វាទៅ Keycloak មិនមែនមកតារាងនេះ។
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "keycloakId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "passportNo", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Customer toEntity(RegisterCustomerRequest request);

    /** PATCH — field ដែល client មិនផ្ញើ (null) មិនជាន់លើតម្លៃចាស់។ */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "keycloakId", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(PatchCustomerRequest request, @MappingTarget Customer customer);

    /**
     * {@code totalBookings} និង {@code completedTours} មកពី Service (ការរាប់ក្នុង database)។
     *
     * <p>{@code memberSince} ទាញពី {@code createdAt} — ការពារ {@code null} ព្រោះ entity
     * ដែលមិនទាន់ save មិនទាន់មាន audit timestamp ទេ។
     */
    @Mapping(target = "totalBookings", source = "totalBookings")
    @Mapping(target = "completedTours", source = "completedTours")
    @Mapping(target = "memberSince",
            expression = "java(customer.getCreatedAt() == null ? null : customer.getCreatedAt().toLocalDate())")
    CustomerResponse toResponse(Customer customer, long totalBookings, long completedTours);
}
