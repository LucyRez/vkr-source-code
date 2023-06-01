package cs.hse.client.repository;

import cs.hse.client.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


public interface ClientRepository extends JpaRepository<Client, Long> {

    @Query("""
        SELECT c FROM Client c WHERE c.clientId = :clientId
    """)
    Optional<Client> findByClientId(String clientId);
}
