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

    // keycloakId and status are set by the service; id and audit by JPA; password is not mapped (it goes to Keycloak).
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

    // PATCH: null fields do not overwrite existing values.
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

    // totalBookings and completedTours come from the service; memberSince is null-safe as unsaved entities lack createdAt.
    @Mapping(target = "totalBookings", source = "totalBookings")
    @Mapping(target = "completedTours", source = "completedTours")
    @Mapping(target = "memberSince",
            expression = "java(customer.getCreatedAt() == null ? null : customer.getCreatedAt().toLocalDate())")
    CustomerResponse toResponse(Customer customer, long totalBookings, long completedTours);
}
