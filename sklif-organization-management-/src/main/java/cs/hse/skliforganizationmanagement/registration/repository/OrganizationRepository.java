package cs.hse.skliforganizationmanagement.registration.repository;

import cs.hse.skliforganizationmanagement.registration.entity.AppUser;
import cs.hse.skliforganizationmanagement.registration.entity.Organization;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    @Query("""
        SELECT o FROM Organization o WHERE o.id = :id
    """)
    Optional<Organization> findById(Long id);

    @Query("""
        SELECT o FROM Organization o WHERE o.email = :email
    """)
    Optional<Organization> findByEmail(String email);

    @Query("""
        SELECT o FROM Organization o WHERE o.name = :name
    """)
    Optional<Organization> findByName(String name);

    @Transactional
    @Modifying
    @Query("""
        UPDATE Organization o SET o.name = ?2, o.phoneNumber = ?3, o.email = ?4, o.administratorFullName = ?5, o.administratorFullName = ?6
        WHERE o.email = ?1
    """)
    Optional<Organization> updateOrganization(String orgEmail, String name, String phoneNumber, String email, String administratorFullName, String address);

}
