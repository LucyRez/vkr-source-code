package cs.hse.skliforganizationmanagement.registration.organization;

import cs.hse.skliforganizationmanagement.registration.requests.organization.OrganizationEditRequest;
import cs.hse.skliforganizationmanagement.registration.requests.user.PasswordChangeRequest;
import cs.hse.skliforganizationmanagement.registration.user.UserResponse;
import cs.hse.skliforganizationmanagement.registration.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api/organizations")
public class OrganizationController {
    private final OrganizationService organizationService;

    @GetMapping("/organization")
    public @ResponseBody ResponseEntity<OrganizationResponse> getOrganizationByEmail(@RequestParam String email) {
        return organizationService.getOrganization(email);
    }

    @GetMapping
    public @ResponseBody ResponseEntity<List<OrganizationResponse>> getOrganizations() {
        return organizationService.getOrganizations();
    }

    @PatchMapping("/edit")
    public @ResponseBody ResponseEntity<String> editOrganization(@RequestParam String email, @RequestBody OrganizationEditRequest body) {
        return organizationService.editOrganization(email, body);
    }
}
