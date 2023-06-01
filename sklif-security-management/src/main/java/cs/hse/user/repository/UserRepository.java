package cs.hse.user.repository;

import cs.hse.user.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {

    @Query("""
        SELECT u FROM AppUser u WHERE u.email = :username
    """)
    Optional<AppUser> findByUsername(String username);
}
