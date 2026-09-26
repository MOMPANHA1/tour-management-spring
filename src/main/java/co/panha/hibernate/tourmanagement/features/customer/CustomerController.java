package co.panha.hibernate.tourmanagement.features.customer;

import co.panha.hibernate.tourmanagement.base.PageResponse;
import co.panha.hibernate.tourmanagement.features.customer.dto.CustomerResponse;
import co.panha.hibernate.tourmanagement.features.customer.dto.PatchCustomerRequest;
import co.panha.hibernate.tourmanagement.features.customer.dto.RegisterCustomerRequest;
import co.panha.hibernate.tourmanagement.features.customer.dto.UpdateCustomerStatusRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * F6 — អតិថិជន។
 *
 * <p>សិទ្ធិកំណត់ក្នុង {@code security/SecurityConfig}៖ {@code /register} សាធារណៈ ·
 * {@code /me} សម្រាប់ {@code CUSTOMER} · ឯផ្លូវដែលនៅសល់សម្រាប់ {@code ADMIN}។
 *
 * <p>{@code /me} មិនទទួល id ពី client ទេ — Service ទាញអត្តសញ្ញាណពី JWT ដោយផ្ទាល់
 * ({@code AuthUtils.currentKeycloakId()})។ បើទទួលពី client នោះអ្នកណាក៏អាចមើលប្រវត្តិរូប
 * អ្នកដទៃបានដែរ។
 */
@Tag(name = "Customer")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse register(@Valid @RequestBody RegisterCustomerRequest request) {
        return customerService.register(request);
    }

    @GetMapping("/me")
    public CustomerResponse findMe() {
        return customerService.findMe();
    }

    @PatchMapping("/me")
    public CustomerResponse updateMe(@Valid @RequestBody PatchCustomerRequest request) {
        return customerService.updateMe(request);
    }

    @GetMapping
    public PageResponse<CustomerResponse> findAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return customerService.findAll(keyword, page, size);
    }

    @GetMapping("/{id}")
    public CustomerResponse findById(@PathVariable Long id) {
        return customerService.findById(id);
    }

    @PatchMapping("/{id}/status")
    public CustomerResponse changeStatus(@PathVariable Long id,
                                         @Valid @RequestBody UpdateCustomerStatusRequest request) {
        return customerService.changeStatus(id, request);
    }
}
