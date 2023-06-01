package cs.hse.skliforganizationmanagement.registration.requests.user;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
@ToString
public class PasswordChangeRequest {
    private String email;
    private String password;
}

