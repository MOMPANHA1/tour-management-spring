package co.panha.hibernate.tourmanagement.features.customer;

import co.panha.hibernate.tourmanagement.base.PageMapper;
import co.panha.hibernate.tourmanagement.features.booking.BookingRepository;
import co.panha.hibernate.tourmanagement.features.booking.BookingStatus;
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

    // Minimum age to self-register.
    private static final int MIN_AGE = 16;

    // Statuses that disable the Keycloak user.
    private static final Set<CustomerStatus> BLOCKING_STATUSES =
            Set.of(CustomerStatus.SUSPENDED, CustomerStatus.BLOCKED);

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final BookingRepository bookingRepository;
    private final KeycloakUserService keycloakUserService;

    // UC6.1: duplicates are checked in the DB before calling Keycloak; if save fails, the Keycloak user is deleted (compensation).
    @Override
    @Transactional
    public CustomerResponse register(RegisterCustomerRequest request) {

        // 1. Validate
        if (!request.password().equals(request.confirmedPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        requireMinimumAge(request.dateOfBirth());

        // 3. Check Rules: three duplicate checks
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

        // 5. Build: Keycloak first, since keycloakId is needed to fill the entity
        String keycloakId = keycloakUserService.createUser(
                request.username(), request.email(), request.fullName(), request.password());

        try {
            Customer customer = customerMapper.toEntity(request);
            customer.setKeycloakId(keycloakId);
            customer.setStatus(CustomerStatus.ACTIVE);
            customer.setIsDeleted(false);

            // saveAndFlush so unique violations surface here (inside try) rather than at commit.
            Customer saved = customerRepository.saveAndFlush(customer);

            // 7. Side Effects
            // TODO: notificationService.sendWelcomeEmail(saved)

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

    // UC6.6: updates both the DB and Keycloak, otherwise a suspended customer could still log in.
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

        // TODO: notificationService.notifyAccountStatusChanged(saved, request.reason())
        log.info("Customer id={} status changed to {} (reason: {})",
                saved.getId(), saved.getStatus(), request.reason());

        return toResponseWithStats(saved);
    }

    // ---------- Internal helpers ----------

    // Current logged-in customer, resolved from the JWT.
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

    // A null dateOfBirth is allowed; it is optional.
    private void requireMinimumAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return;
        }

        if (DateUtils.yearsBetween(dateOfBirth, LocalDate.now()) < MIN_AGE) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "Customer must be at least " + MIN_AGE + " years old");
        }
    }

    // Suspending a customer with active bookings is allowed but requires a recorded reason.
    private void requireReasonWhenCustomerHasActiveBookings(Customer customer, String reason) {

        long active = bookingRepository.countActiveByCustomer(customer.getId());

        if (active > 0 && (reason == null || reason.isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A reason is required — this customer has " + active + " active booking(s)");
        }
    }

    // Fills DB-derived stats that are not stored on the entity.
    private CustomerResponse toResponseWithStats(Customer customer) {

        long totalBookings = bookingRepository.countByCustomerIdAndIsDeletedFalse(customer.getId());
        long completedTours = bookingRepository.countByCustomerIdAndStatusAndIsDeletedFalse(
                customer.getId(), BookingStatus.COMPLETED);

        return customerMapper.toResponse(customer, totalBookings, completedTours);
    }
}
