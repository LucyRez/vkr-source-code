package cs.hse.skliforganizationmanagement.registration.user;

import cs.hse.skliforganizationmanagement.registration.requests.user.EmailChangeRequest;
import cs.hse.skliforganizationmanagement.registration.requests.user.PasswordChangeRequest;
import cs.hse.skliforganizationmanagement.registration.requests.user.RoleUpdateRequest;
import cs.hse.skliforganizationmanagement.registration.requests.user.UserEditRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api/user")
public class UserController {

    private final UserService userService;

    @PostMapping("password_change")
    public @ResponseBody ResponseEntity<String> changePassword(@RequestBody PasswordChangeRequest body) {
        return userService.changePassword(body);
    }

    @GetMapping
    public @ResponseBody ResponseEntity<UserResponse> getUserByEmail(@RequestParam String email) {
        return userService.getUser(email);
    }

    @GetMapping("/organization")
    public @ResponseBody ResponseEntity<List<UserResponse>> getUsersByOrganization(@RequestParam String organization) {
        return userService.getUsers(organization);
    }

    @PatchMapping("/edit_info")
    public @ResponseBody ResponseEntity<String> editUserInfo(@RequestParam String email, @RequestBody UserEditRequest body) {
        return userService.editUser(email, body);
    }

    @PatchMapping("/edit_email")
    public @ResponseBody ResponseEntity<String> editUserEmail(@RequestBody EmailChangeRequest body) {
        return userService.changeEmail(body);
    }

    @PatchMapping("/edit_role")
    public @ResponseBody ResponseEntity<String> editUserRole(@RequestParam String email, @RequestBody RoleUpdateRequest body) {
        return userService.updateUserRole(email, body);
    }

}
