package co.panha.hibernate.tourmanagement.features.customer;

import co.panha.hibernate.tourmanagement.base.PageResponse;
import co.panha.hibernate.tourmanagement.features.customer.dto.CustomerResponse;
import co.panha.hibernate.tourmanagement.features.customer.dto.PatchCustomerRequest;
import co.panha.hibernate.tourmanagement.features.customer.dto.RegisterCustomerRequest;
import co.panha.hibernate.tourmanagement.features.customer.dto.UpdateCustomerStatusRequest;

/**
 * សេវាកម្មអតិថិជន — F6។
 */
public interface CustomerService {

    /** UC6.1 — ចុះឈ្មោះអតិថិជនថ្មី (បង្កើត user ក្នុង Keycloak ផង)។ */
    CustomerResponse register(RegisterCustomerRequest request);

    /** UC6.2 — មើលប្រវត្តិរូបខ្លួនឯង។ */
    CustomerResponse findMe();

    /** UC6.3 — កែប្រវត្តិរូបខ្លួនឯង (PATCH)។ */
    CustomerResponse updateMe(PatchCustomerRequest request);

    /** UC6.4 — បញ្ជីអតិថិជន ស្វែងរកតាមពាក្យគន្លឹះបាន។ */
    PageResponse<CustomerResponse> findAll(String keyword, Integer page, Integer size);

    /** UC6.5 — អតិថិជនម្នាក់តាម id។ */
    CustomerResponse findById(Long id);

    /** UC6.6 — ប្តូរស្ថានភាពគណនី (ផ្អាក / ដោះការផ្អាក)។ */
    CustomerResponse changeStatus(Long id, UpdateCustomerStatusRequest request);
}
