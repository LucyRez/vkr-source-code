package cs.hse.skliforganizationmanagement.registration.confirmation;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ConfirmationService {

    private final ConfirmationTokenRepository repository;

    public void saveConfirmationToken(ConfirmationToken token) {
        repository.save(token);
    }

    public Optional<ConfirmationToken> getToken(String token){
        return repository.findByToken(token);
    }

    public int setConfirmedAt(String token) {
        return repository.updateConfirmedAt(
                token, LocalDateTime.now());
    }

}
