package cs.hse.user.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
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
            strategy = GenerationType.SEQUENCE,
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
    private Boolean locked;
    private Boolean enabled;
}
