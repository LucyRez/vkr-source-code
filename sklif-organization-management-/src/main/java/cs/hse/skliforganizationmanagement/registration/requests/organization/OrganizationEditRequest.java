package cs.hse.skliforganizationmanagement.registration.requests.organization;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class OrganizationEditRequest {
    private String organizationName;
    private String phoneNumber;
    private String email;
    private String administratorFirstName;
    private String administratorLastName;
    private String administratorPatronymic;
    private String address;
}
