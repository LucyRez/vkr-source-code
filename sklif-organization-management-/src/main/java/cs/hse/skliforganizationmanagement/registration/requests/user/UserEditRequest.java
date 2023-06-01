package cs.hse.skliforganizationmanagement.registration.requests.user;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
@ToString
public class UserEditRequest {
    private String firstName;
    private String lastName;
    private String patronymic;
    private String phoneNumber;
    private String organization;
}
