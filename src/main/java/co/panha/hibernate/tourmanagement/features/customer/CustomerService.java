package co.panha.hibernate.tourmanagement.features.customer;

import co.panha.hibernate.tourmanagement.base.PageResponse;
import co.panha.hibernate.tourmanagement.features.customer.dto.CustomerResponse;
import co.panha.hibernate.tourmanagement.features.customer.dto.PatchCustomerRequest;
import co.panha.hibernate.tourmanagement.features.customer.dto.RegisterCustomerRequest;
import co.panha.hibernate.tourmanagement.features.customer.dto.UpdateCustomerStatusRequest;

// F6 customer service.
public interface CustomerService {

    // UC6.1 register a new customer (also creates the Keycloak user).
    CustomerResponse register(RegisterCustomerRequest request);

    // UC6.2 view own profile.
    CustomerResponse findMe();

    // UC6.3 update own profile (PATCH).
    CustomerResponse updateMe(PatchCustomerRequest request);

    // UC6.4 list customers, optionally by keyword.
    PageResponse<CustomerResponse> findAll(String keyword, Integer page, Integer size);

    // UC6.5 get one customer by id.
    CustomerResponse findById(Long id);

    // UC6.6 change account status (suspend / unsuspend).
    CustomerResponse changeStatus(Long id, UpdateCustomerStatusRequest request);
}
