package cs.hse.skliforganizationmanagement.registration.user;

import cs.hse.skliforganizationmanagement.registration.RegistrationService;
import cs.hse.skliforganizationmanagement.registration.entity.AppUser;
import cs.hse.skliforganizationmanagement.registration.entity.UserRole;
import cs.hse.skliforganizationmanagement.registration.repository.OrganizationRepository;
import cs.hse.skliforganizationmanagement.registration.repository.UserRepository;
import cs.hse.skliforganizationmanagement.registration.requests.organization.OrganizationEditRequest;
import cs.hse.skliforganizationmanagement.registration.requests.user.EmailChangeRequest;
import cs.hse.skliforganizationmanagement.registration.requests.user.PasswordChangeRequest;
import cs.hse.skliforganizationmanagement.registration.requests.user.RoleUpdateRequest;
import cs.hse.skliforganizationmanagement.registration.requests.user.UserEditRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder encoder;

    private final RegistrationService registrationService;

    @Transactional
    public ResponseEntity<String> changePassword(PasswordChangeRequest body) {
        var user = userRepository.findByUsername(body.getEmail());

        if (!user.isPresent()) {
            return new ResponseEntity<>( "User with the email does not exist", HttpStatus.NOT_FOUND);
        }

        var unwrap = user.get();

        var currentEmail =  SecurityContextHolder.getContext().getAuthentication().getName();
        if (!currentEmail.equals(body.getEmail())) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "unauthorized");
        }

        var encoded = encoder.encode(body.getPassword());
        unwrap.setPassword(encoded);
        userRepository.updatePassword(encoded, currentEmail);
        return ResponseEntity.ok("password changed");
    }

    public ResponseEntity<UserResponse> getUser(String email) {
        var user = userRepository.findByUsername(email);

        if (!user.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        var unwrap = user.get();

        UserResponse userResponse = new UserResponse(unwrap.getEmail(), unwrap.getFirstName(), unwrap.getLastName(),
                unwrap.getPatronymic(), unwrap.getPhoneNumber(), unwrap.getRole().name(), unwrap.getOrganization());

        return ResponseEntity.ok(userResponse);
    }

    public ResponseEntity<List<UserResponse>> getUsers(String organization) {
        var org = organizationRepository.findByName(organization);

        if (!org.isPresent()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<AppUser> users = userRepository.findByOrganization(organization);

        var list =  users.stream().map(
                user -> new UserResponse(user.getEmail(), user.getFirstName(), user.getLastName(),
                        user.getPatronymic(), user.getPhoneNumber(), user.getRole().name(), user.getOrganization())
        ).collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    @Transactional
    public ResponseEntity<String> editUser(String email, UserEditRequest body) {
        var currentEmail =  SecurityContextHolder.getContext().getAuthentication().getName();
        var currentUser = userRepository.findByUsername(currentEmail);
        if (!currentUser.isPresent()) {
            return new ResponseEntity<>( "Logined user does not exist ._.", HttpStatus.NOT_FOUND);
        }

        var unwrappedCurrent = currentUser.get();

        var requestedUser = userRepository.findByUsername(email);
        if (!requestedUser.isPresent()) {
            return new ResponseEntity<>( "Requested user does not exist ._.", HttpStatus.NOT_FOUND);
        }

        var unwrappedRequested = requestedUser.get();

        try {
            if (!currentEmail.equals(email)) {
                if (!((unwrappedCurrent.getRole() == UserRole.ADMIN_GLOBAL) ||
                        (unwrappedCurrent.getRole() == UserRole.ADMIN_LOCAL &&
                                unwrappedRequested.getOrganization().equals(unwrappedCurrent.getOrganization())))) {
                    return new ResponseEntity<>( "Unauthorized access: you are not global admin or organization's admin", HttpStatus.UNAUTHORIZED);
                }
            }
        } catch (NullPointerException ex) {
            return new ResponseEntity<>( "Unauthorized access: you are not global admin or organization's admin", HttpStatus.UNAUTHORIZED);
        }

        // Change all fields in the entity that require it

        var first = body.getFirstName();
        if (first != null) {
            unwrappedRequested.setFirstName(first);
        }

        var last = body.getLastName();
        if (last != null) {
            unwrappedRequested.setLastName(last);
        }

        var patronymic = body.getPatronymic();
        if (patronymic != null) {
            unwrappedRequested.setPatronymic(patronymic);
        }

        var phone = body.getPhoneNumber();
        if (!(phone == null)) {
            unwrappedRequested.setPhoneNumber(phone);
        }

        var organization = body.getOrganization();

        if (organization != null) {
            var org = organizationRepository.findByName(organization);
            if (!org.isPresent()) {
                return new ResponseEntity<>( "Organization with the email does not exist", HttpStatus.NOT_FOUND);
            }

            unwrappedRequested.setOrganization(organization);
        }

        // Save new entity with the same id
        userRepository.save(unwrappedRequested);

        return ResponseEntity.ok("user updated");
    }

    @Transactional
    public ResponseEntity<String> changeEmail(EmailChangeRequest body) {
        var currentEmail =  SecurityContextHolder.getContext().getAuthentication().getName();
        var currentUser = userRepository.findByUsername(currentEmail);
        if (!currentUser.isPresent()) {
            return new ResponseEntity<>( "Logined user does not exist ._.", HttpStatus.NOT_FOUND);
        }

        var unwrappedCurrent = currentUser.get();

        var requestedUser = userRepository.findByUsername(body.getPreviousEmail());
        if (!requestedUser.isPresent()) {
            return new ResponseEntity<>( "Requested user does not exist ._.", HttpStatus.NOT_FOUND);
        }

        var unwrappedRequested = requestedUser.get();

        try {
            if (!currentEmail.equals(body.getPreviousEmail())) {
                if (!((unwrappedCurrent.getRole() == UserRole.ADMIN_GLOBAL) ||
                        (unwrappedCurrent.getRole() == UserRole.ADMIN_LOCAL &&
                                unwrappedRequested.getOrganization().equals(unwrappedCurrent.getOrganization())))) {
                    return new ResponseEntity<>( "Unauthorized access: you are not global admin or organization's admin", HttpStatus.UNAUTHORIZED);
                }
            }
        } catch (NullPointerException ex) {
            return new ResponseEntity<>( "Unauthorized access: you are not global admin or organization's admin", HttpStatus.UNAUTHORIZED);
        }

        // Change all fields in the entity that require it

        var mail = body.getNewEmail();
        if (userRepository.findByUsername(mail).isPresent()) {
            return new ResponseEntity<>( "User with the same email already exists", HttpStatus.BAD_REQUEST);
        }

        unwrappedRequested.setEmail(mail);
        unwrappedRequested.setEnabled(false);

        // Save new entity with the same id
        userRepository.save(unwrappedRequested);
        registrationService.sendTokenThroughEmail(unwrappedRequested, true);

        return ResponseEntity.ok("User email updated. Email has been sent");
    }

    @Transactional
    public ResponseEntity<String> updateUserRole(String email, RoleUpdateRequest body) {
        var currentEmail =  SecurityContextHolder.getContext().getAuthentication().getName();
        var currentUser = userRepository.findByUsername(currentEmail);
        if (!currentUser.isPresent()) {
            return new ResponseEntity<>( "Logined user does not exist ._.", HttpStatus.NOT_FOUND);
        }

        var unwrappedCurrent = currentUser.get();

        var requestedUser = userRepository.findByUsername(email);
        if (!requestedUser.isPresent()) {
            return new ResponseEntity<>( "Requested user does not exist ._.", HttpStatus.NOT_FOUND);
        }

        var unwrappedRequested = requestedUser.get();

        try {
            if (currentEmail.equals(email)) {
                return new ResponseEntity<>( "Unauthorized access: you cannot change role for yourself", HttpStatus.UNAUTHORIZED);
            }

            if (!((unwrappedCurrent.getRole() == UserRole.ADMIN_GLOBAL) ||
                    (unwrappedCurrent.getRole() == UserRole.ADMIN_LOCAL &&
                            unwrappedRequested.getOrganization().equals(unwrappedCurrent.getOrganization())))) {
                return new ResponseEntity<>( "Unauthorized access: you are not global admin or organization's admin", HttpStatus.UNAUTHORIZED);
            }

        } catch (NullPointerException ex) {
            return new ResponseEntity<>( "Unauthorized access: you are not global admin or organization's admin", HttpStatus.UNAUTHORIZED);
        }

        // Change all fields in the entity that require it

        var role = body.getNewRole();
        if (role.equals(UserRole.ADMIN_GLOBAL.name()) && unwrappedCurrent.getRole() != UserRole.ADMIN_GLOBAL) {
            return new ResponseEntity<>( "Unauthorized access: you need to be global admin to give someone global admin role", HttpStatus.UNAUTHORIZED);
        }

        unwrappedRequested.setRole(UserRole.valueOf(role));

        // Save new entity with the same id
        userRepository.save(unwrappedRequested);

        return ResponseEntity.ok("role updated");
    }

}
