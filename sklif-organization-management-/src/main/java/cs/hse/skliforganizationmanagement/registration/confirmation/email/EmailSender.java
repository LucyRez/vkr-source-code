package cs.hse.skliforganizationmanagement.registration.confirmation.email;

public interface EmailSender {

    void send(String to, String email);
}
