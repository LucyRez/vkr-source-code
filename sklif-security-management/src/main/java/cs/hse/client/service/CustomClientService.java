package cs.hse.client.service;

import cs.hse.client.entity.Client;
import cs.hse.client.repository.ClientRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Transactional
public class CustomClientService implements RegisteredClientRepository {

    private final ClientRepository clientRepository;

    @Override
    public void save(RegisteredClient registeredClient) {
        clientRepository.save(Client.from(registeredClient));
    }

    @Override
    public RegisteredClient findById(String id) {
        var client = clientRepository.findById(Long.valueOf(id))
                .orElseThrow();
        return Client.from(client);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        var client = clientRepository.findByClientId(clientId)
                .orElseThrow();
        return Client.from(client);
    }
}
