package cs.hse.skliforganizationmanagement.registration.organization;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
@ToString
public class OrganizationResponse {
    private String email;
    private String name;
    private String administratorFullName;
    private String phoneNumber;
    private String address;

}
