package cs.hse.skliforganizationmanagement.registration.organization;

import cs.hse.skliforganizationmanagement.registration.entity.UserRole;
import cs.hse.skliforganizationmanagement.registration.repository.OrganizationRepository;
import cs.hse.skliforganizationmanagement.registration.repository.UserRepository;
import cs.hse.skliforganizationmanagement.registration.requests.organization.OrganizationEditRequest;
import cs.hse.skliforganizationmanagement.registration.requests.organization.OrganizationRegistrationRequest;
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
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    public ResponseEntity<OrganizationResponse> getOrganization(String email) {
        var org = organizationRepository.findByEmail(email);

        if (!org.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        var unwrap = org.get();

        OrganizationResponse organizationResponse = new OrganizationResponse(unwrap.getEmail(), unwrap.getName(), unwrap.getAdministratorFullName(),
                unwrap.getPhoneNumber(), unwrap.getAddress());

        return ResponseEntity.ok(organizationResponse);
    }

    public ResponseEntity<List<OrganizationResponse>> getOrganizations() {
        var org = organizationRepository.findAll()
                .stream().map(
                        (o) -> new OrganizationResponse(o.getEmail(), o.getName(), o.getAdministratorFullName(),
                                o.getPhoneNumber(), o.getAddress())
                ).collect(Collectors.toList());

        return ResponseEntity.ok(org);
    }

    @Transactional
    public ResponseEntity<String> editOrganization(String email, OrganizationEditRequest body) {
        var currentEmail =  SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByUsername(currentEmail);

        if (!user.isPresent()) {
            return new ResponseEntity<>( "Logined user does not exist ._.", HttpStatus.NOT_FOUND);
        }

        var userUnwrap = user.get();

        var org = organizationRepository.findByEmail(email);

        if (!org.isPresent()) {
            return new ResponseEntity<>( "Organization with the email does not exist", HttpStatus.NOT_FOUND);
        }
        var unwrap = org.get();

        try {
            if (!currentEmail.equals(email) || !(userUnwrap.getRole() == UserRole.ADMIN_GLOBAL)) {
                if (!(userUnwrap.getOrganization().equals(unwrap.getName()) && userUnwrap.getRole() == UserRole.ADMIN_LOCAL)) {
                    return new ResponseEntity<>( "Unauthorized access: you are not global admin or organization's admin", HttpStatus.UNAUTHORIZED);
                }
            }
        } catch (NullPointerException ex) {
            return new ResponseEntity<>( "Unauthorized access: you are not global admin or organization's admin", HttpStatus.UNAUTHORIZED);
        }

        // Change all fields in the entity that require it

        var name = body.getOrganizationName();
        if (organizationRepository.findByName(name).isPresent()) {
            return new ResponseEntity<>( "Organization with the same name already exists", HttpStatus.BAD_REQUEST);
        }

        if (!(name == null)) {
            unwrap.setName(name);
        }

        var phone = body.getPhoneNumber();
        if (!(phone == null)) {
            unwrap.setPhoneNumber(phone);
        }

        var mail = body.getEmail();
        if (organizationRepository.findByEmail(mail).isPresent()) {
            return new ResponseEntity<>( "Organization with the email already exists", HttpStatus.BAD_REQUEST);
        }

        if (!(mail == null)) {
            unwrap.setEmail(mail);
        }

        var fullName = "";

        var last = body.getAdministratorLastName();
        if (!(last == null)) {
            fullName += last;
        }

        var first = body.getAdministratorFirstName();
        if (!(first == null)) {
            fullName += " " + first;
        }

        var patronymic = body.getAdministratorPatronymic();
        if (!(patronymic == null)) {
            fullName += " " + patronymic;
        }

        if(!fullName.isEmpty()) {
            unwrap.setAdministratorFullName(fullName);
        }

        var address = body.getAddress();
        if (!(address == null)) {
            unwrap.setAddress(address);
        }

        // Save new entity with the same id
        organizationRepository.save(unwrap);

        return ResponseEntity.ok("organization updated");
    }
}
