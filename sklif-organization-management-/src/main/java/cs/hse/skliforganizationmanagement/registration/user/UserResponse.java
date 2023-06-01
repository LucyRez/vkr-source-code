package cs.hse.skliforganizationmanagement.registration.user;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
@ToString
public class UserResponse {
    private String email;
    private String firstName;
    private String lastName;
    private String patronymic;
    private String phoneNumber;
    private String role;
    private String organization;
}
