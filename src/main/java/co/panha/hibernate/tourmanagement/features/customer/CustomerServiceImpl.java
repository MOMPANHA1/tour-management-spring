package co.panha.hibernate.tourmanagement.features.customer;

import co.panha.hibernate.tourmanagement.base.PageMapper;
import co.panha.hibernate.tourmanagement.base.PageResponse;
import co.panha.hibernate.tourmanagement.features.customer.dto.CustomerResponse;
import co.panha.hibernate.tourmanagement.features.customer.dto.PatchCustomerRequest;
import co.panha.hibernate.tourmanagement.features.customer.dto.RegisterCustomerRequest;
import co.panha.hibernate.tourmanagement.features.customer.dto.UpdateCustomerStatusRequest;
import co.panha.hibernate.tourmanagement.security.AuthUtils;
import co.panha.hibernate.tourmanagement.security.KeycloakUserService;
import co.panha.hibernate.tourmanagement.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    /** អាយុអប្បបរមាដើម្បីបើកគណនីដោយខ្លួនឯង។ */
    private static final int MIN_AGE = 16;

    /** ស្ថានភាពដែលបិទ user ក្នុង Keycloak។ */
    private static final Set<CustomerStatus> BLOCKING_STATUSES =
            Set.of(CustomerStatus.SUSPENDED, CustomerStatus.BLOCKED);

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final KeycloakUserService keycloakUserService;

    /**
     * UC6.1 — ចុះឈ្មោះ។
     *
     * <p><b>លំដាប់សំខាន់</b>៖ ពិនិត្យស្ទួនក្នុង database <b>មុន</b> ហៅ Keycloak — ដើម្បីកុំឲ្យ
     * បង្កើត user រួចទើបដឹងថាលេខទូរស័ព្ទស្ទួន។
     *
     * <p><b>គ្មាន transaction ឆ្លងប្រព័ន្ធ</b>៖ Keycloak និង Postgres ជាប្រព័ន្ធពីរដាច់ពីគ្នា។
     * បើការ save បរាជ័យក្រោយ Keycloak បង្កើត user រួច នោះនឹងសល់ user កំព្រា — ដូច្នេះត្រូវ
     * លុបវាវិញ (compensation) រួចបោះកំហុសដើមបន្ត។
     */
    @Override
    @Transactional
    public CustomerResponse register(RegisterCustomerRequest request) {

        // ១. Validate
        if (!request.password().equals(request.confirmedPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        requireMinimumAge(request.dateOfBirth());

        // ៣. Check Rules — ស្ទួន ៣ ចំណុច
        if (customerRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Username '" + request.username() + "' is already taken");
        }

        if (customerRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        if (customerRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number is already registered");
        }

        // ៥. Build — Keycloak មុន ព្រោះយើងត្រូវការ keycloakId ដើម្បីបំពេញ entity
        String keycloakId = keycloakUserService.createUser(
                request.username(), request.email(), request.fullName(), request.password());

        try {
            Customer customer = customerMapper.toEntity(request);
            customer.setKeycloakId(keycloakId);
            customer.setStatus(CustomerStatus.ACTIVE);
            customer.setIsDeleted(false);

            // saveAndFlush មិនមែន save — ដើម្បីឲ្យការបំពានលើ unique constraint លេចឡើង
            // នៅត្រង់នេះ មិនមែននៅពេល commit (ដែលនៅក្រៅ try ហើយ compensation មិនរត់)។
            Customer saved = customerRepository.saveAndFlush(customer);

            // ៧. Side Effects
            // TODO ដំណាក់កាល ៥៖ notificationService.sendWelcomeEmail(saved)

            return toResponseWithStats(saved);

        } catch (RuntimeException ex) {
            keycloakUserService.deleteUserQuietly(keycloakId);
            throw ex;
        }
    }

    @Override
    public CustomerResponse findMe() {
        return toResponseWithStats(loadMe());
    }

    @Override
    @Transactional
    public CustomerResponse updateMe(PatchCustomerRequest request) {

        Customer customer = loadMe();

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Your account is " + customer.getStatus());
        }

        if (request.phoneNumber() != null
                && !request.phoneNumber().equals(customer.getPhoneNumber())
                && customerRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number is already registered");
        }

        requireMinimumAge(request.dateOfBirth());

        customerMapper.updateEntity(request, customer);

        return toResponseWithStats(customerRepository.save(customer));
    }

    @Override
    public PageResponse<CustomerResponse> findAll(String keyword, Integer page, Integer size) {

        Pageable pageable = PageMapper.buildPageable(page, size, "fullName", Sort.Direction.ASC);

        Page<Customer> result = (keyword == null || keyword.isBlank())
                ? customerRepository.findAllByIsDeletedFalse(pageable)
                : customerRepository.searchByKeyword(keyword.trim(), pageable);

        return PageMapper.toPageResponse(result, this::toResponseWithStats);
    }

    @Override
    public CustomerResponse findById(Long id) {
        return toResponseWithStats(loadById(id));
    }

    /**
     * UC6.6 — ផ្អាក ឬដោះការផ្អាកគណនី។
     *
     * <p>ប្តូរទាំង database <b>និង</b> Keycloak — បើប្តូរតែ database អតិថិជននៅតែ login បាន
     * ហើយទទួល token ដែលមាន role {@code CUSTOMER} ធម្មតា ដូច្នេះការផ្អាកគ្មានប្រសិទ្ធភាពទេ។
     */
    @Override
    @Transactional
    public CustomerResponse changeStatus(Long id, UpdateCustomerStatusRequest request) {

        Customer customer = loadById(id);

        if (customer.getStatus() == request.status()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Customer is already in status " + request.status());
        }

        if (BLOCKING_STATUSES.contains(request.status())) {
            requireReasonWhenCustomerHasActiveBookings(customer, request.reason());
        }

        customer.setStatus(request.status());
        Customer saved = customerRepository.save(customer);

        keycloakUserService.setEnabled(saved.getKeycloakId(),
                saved.getStatus() == CustomerStatus.ACTIVE);

        // TODO ដំណាក់កាល ៥៖ notificationService.notifyAccountStatusChanged(saved, request.reason())
        log.info("Customer id={} status changed to {} (reason: {})",
                saved.getId(), saved.getStatus(), request.reason());

        return toResponseWithStats(saved);
    }

    // ---------- ជំនួយខាងក្នុង ----------

    /** អតិថិជនដែលកំពុង login — ស្ពានពី JWT មកតារាង {@code customers}។ */
    private Customer loadMe() {
        String keycloakId = AuthUtils.currentKeycloakId();

        return customerRepository.findByKeycloakIdAndIsDeletedFalse(keycloakId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No customer profile is linked to your account"));
    }

    private Customer loadById(Long id) {
        return customerRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Customer not found with id = " + id));
    }

    /** {@code null} អនុញ្ញាត — {@code dateOfBirth} ជាជម្រើស។ */
    private void requireMinimumAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return;
        }

        if (DateUtils.yearsBetween(dateOfBirth, LocalDate.now()) < MIN_AGE) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "Customer must be at least " + MIN_AGE + " years old");
        }
    }

    /**
     * ការផ្អាកអតិថិជនដែលមានការកក់សកម្មមិនត្រូវបានហាមទេ — តែត្រូវមានហេតុផលកត់ត្រាទុក។
     *
     * <p>បច្ចុប្បន្នមិនអាចរាប់ការកក់បានទេ ព្រោះ {@code Booking} មិនទាន់មាន (F7) — ដូច្នេះ
     * ច្បាប់នេះនៅមិនទាន់មានប្រសិទ្ធភាព។
     */
    private void requireReasonWhenCustomerHasActiveBookings(Customer customer, String reason) {
        // TODO ដំណាក់កាល ៤ (F7 Booking)៖
        //   long active = bookingRepository.countActiveByCustomer(customer.getId());
        //   if (active > 0 && (reason == null || reason.isBlank())) {
        //       throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
        //               "A reason is required — this customer has " + active + " active booking(s)");
        //   }
    }

    /** បំពេញស្ថិតិដែលមាននៅ database មិនមែនក្នុង entity — ដូចលំនាំ {@code GuideServiceImpl}។ */
    private CustomerResponse toResponseWithStats(Customer customer) {

        // TODO ដំណាក់កាល ៤ (F7 Booking)៖ បូកការកក់ពិត
        //   long totalBookings  = bookingRepository.countByCustomerId(customer.getId());
        //   long completedTours = bookingRepository.countByCustomerIdAndStatus(customer.getId(), COMPLETED);
        long totalBookings = 0;
        long completedTours = 0;

        return customerMapper.toResponse(customer, totalBookings, completedTours);
    }
}
