package cs.hse.skliforganizationmanagement.registration;

import cs.hse.skliforganizationmanagement.registration.entity.AppUser;
import cs.hse.skliforganizationmanagement.registration.entity.UserRole;
import cs.hse.skliforganizationmanagement.registration.requests.organization.OrganizationRegistrationRequest;
import cs.hse.skliforganizationmanagement.registration.requests.user.UserRegistrationRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("api/register")
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("user")
    public @ResponseBody String registerUser(@RequestBody UserRegistrationRequest body) {
        return registrationService.signUp(new AppUser(
                body.getEmail(),
                body.getFirstName(),
                body.getLastName(),
                body.getPatronymic(),
                body.getPhoneNumber(),
                UserRole.valueOf(body.getRole()),
                body.getOrganization(),
                body.getPassword(),
                false,
                false
        ));
    }

    @PostMapping("organization")
    public @ResponseBody String registerOrganization(@RequestBody OrganizationRegistrationRequest body) {
        return registrationService.registerOrganization(body);
    }

    @GetMapping("confirm")
    public String confirmEmail(@RequestParam("token") String token) {
        return registrationService.confirmToken(token);
    }

    @GetMapping("confirm_updated")
    public String confirmUpdatedEmail(@RequestParam("token") String token) {
        return registrationService.confirmUpdatedEmail(token);
    }

}
