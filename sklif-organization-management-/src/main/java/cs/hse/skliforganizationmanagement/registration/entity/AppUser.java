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
public class AppUser {
    @SequenceGenerator(
            name = "user_sequence",
            sequenceName = "user_sequence",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(
            strategy = GenerationType.AUTO,
            generator = "user_sequence"
    )
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String patronymic;
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    private String organization;

    private String password;
    private String tempPassword;

    private Boolean locked;
    private Boolean enabled;

    public AppUser(String email, String firstName, String lastName, String patronymic, String phoneNumber, UserRole role, String organization, String password, Boolean locked, Boolean enabled) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.patronymic = patronymic;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.organization = organization;
        this.password = password;
        this.locked = locked;
        this.enabled = enabled;
    }
}
