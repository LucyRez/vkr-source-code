package cs.hse.skliforganizationmanagement.registration;

import cs.hse.skliforganizationmanagement.registration.confirmation.ConfirmationService;
import cs.hse.skliforganizationmanagement.registration.confirmation.ConfirmationToken;
import cs.hse.skliforganizationmanagement.registration.confirmation.email.EmailService;
import cs.hse.skliforganizationmanagement.registration.entity.AppUser;
import cs.hse.skliforganizationmanagement.registration.entity.Organization;
import cs.hse.skliforganizationmanagement.registration.entity.UserRole;
import cs.hse.skliforganizationmanagement.registration.repository.OrganizationRepository;
import cs.hse.skliforganizationmanagement.registration.repository.UserRepository;
import cs.hse.skliforganizationmanagement.registration.requests.organization.OrganizationRegistrationRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder encoder;
    private final ConfirmationService confirmationService;
    private final EmailService emailSender;

    public String signUp(AppUser appUser) {

        // TODO: запретить регистрацию пользователей с ролью ADMIN_GLOBAL
        boolean userExists = userRepository.findByUsername(appUser.getEmail()).isPresent();


        if (userExists) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "User with the email already exists");
        }

        boolean organizationExists = organizationRepository.findByName(appUser.getOrganization()).isPresent();

        if (!organizationExists) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Organization not found");
        }

        appUser.setTempPassword(appUser.getPassword());
        String encodedPassword = encoder.encode(appUser.getPassword());
        appUser.setPassword(encodedPassword);

        userRepository.save(appUser);
        sendTokenThroughEmail(appUser, false);

        return HttpStatus.OK.getReasonPhrase();
    }

    public void sendTokenThroughEmail(AppUser appUser, Boolean updated) {
        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(20),
                appUser
        );

        confirmationService.saveConfirmationToken(confirmationToken);

        String link = "http://localhost:9090/api/register/confirm?token=" + token;

        if (updated) {
            link = "http://localhost:9090/api/register/confirm_updated?token=" + token;
        }

        emailSender.send(appUser.getEmail(), emailSender.buildConfirmationEmail(appUser.getFirstName(), link));
    }

    public String registerOrganization(OrganizationRegistrationRequest organization) {
        boolean organizationExist = organizationRepository.findByEmail(organization.getEmail()).isPresent();

        if (organizationExist) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Organization with the email already exists");
        }

        Organization org = new Organization(organization.getOrganizationName(), organization.getPhoneNumber(),
                organization.getEmail(), organization.getAdministratorLastName() + " " +
                organization.getAdministratorFirstName() + " " + organization.getAdministratorPatronymic(), organization.getAddress());

        organizationRepository.save(org);

        AppUser admin = new AppUser(organization.getEmail(), organization.getAdministratorFirstName(),
                organization.getAdministratorLastName(), organization.getAdministratorPatronymic(),
                organization.getPhoneNumber(), UserRole.ADMIN_LOCAL, organization.getOrganizationName(),
                organization.getPassword(), false, false);

        return signUp(admin);
    }

    @Transactional
    public String confirmToken(String token) {
        ConfirmationToken confirmationToken = confirmationService.getToken(token)
                .orElseThrow(() -> new IllegalStateException("Token not found"));

        if (confirmationToken.getConfirmedAt() != null) {
            throw new IllegalStateException("Token already confirmed");
        }

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();

        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Token expired");
        }

        confirmationService.setConfirmedAt(token);
        userRepository.enableUser(confirmationToken.getUser().getEmail());

        var user = confirmationToken.getUser();
        emailSender.send(user.getEmail(), emailSender.buildCredentialsEmail(user.getEmail(), user.getTempPassword()));

        return "confirmed";
    }

    @Transactional
    public String confirmUpdatedEmail(String token) {
        ConfirmationToken confirmationToken = confirmationService.getToken(token)
                .orElseThrow(() -> new IllegalStateException("Token not found"));

        if (confirmationToken.getConfirmedAt() != null) {
            throw new IllegalStateException("Token already confirmed");
        }

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();

        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Token expired");
        }

        confirmationService.setConfirmedAt(token);
        userRepository.enableUser(confirmationToken.getUser().getEmail());

        return "confirmed updated email";
    }

}
