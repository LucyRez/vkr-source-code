package cs.hse.skliforganizationmanagement.registration.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@EqualsAndHashCode
@NoArgsConstructor
public class Organization {

    @SequenceGenerator(
            name = "organization_sequence",
            sequenceName = "organization_sequence",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(
            strategy = GenerationType.AUTO,
            generator = "organization_sequence"
    )
    private Long id;
    private String name;
    private String phoneNumber;
    private String email;
    private String administratorFullName;

    private String address;

    public Organization(String name, String phoneNumber, String email, String administratorFullName, String address) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.administratorFullName = administratorFullName;
        this.address = address;
    }
}
