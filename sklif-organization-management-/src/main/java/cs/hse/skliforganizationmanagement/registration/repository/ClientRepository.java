package cs.hse.skliforganizationmanagement.registration.repository;

import cs.hse.skliforganizationmanagement.registration.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    @Query("""
        SELECT c FROM Client c WHERE c.clientId = :clientId
    """)
    Optional<Client> findByClientId(String clientId);
}
