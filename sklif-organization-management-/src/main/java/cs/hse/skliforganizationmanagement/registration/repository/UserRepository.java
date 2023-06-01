package cs.hse.skliforganizationmanagement.registration.repository;

import cs.hse.skliforganizationmanagement.registration.entity.AppUser;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {

    @Query("""
        SELECT u FROM AppUser u WHERE u.email = :username
    """)
    Optional<AppUser> findByUsername(String username);

    @Query("""
        SELECT u FROM AppUser u WHERE u.organization = :organization
    """)
    List<AppUser> findByOrganization(String organization);

    @Transactional
    @Modifying
    @Query("UPDATE AppUser a " +
            "SET a.enabled = TRUE WHERE a.email = ?1")
    int enableUser(String email);

    @Transactional
    @Modifying
    @Query("UPDATE AppUser a " +
            "SET a.password = ?2 WHERE a.email = ?1")
    int updatePassword(String email, String password);


}