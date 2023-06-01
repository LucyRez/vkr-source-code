package cs.hse.aiclientservice;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "my")
public class ApplicationProperties {
    private String currentS3URL;
    private String currentIp;

    public void setCurrentS3URL(String currentS3URL) {
        this.currentS3URL = currentS3URL;
    }

    public void setCurrentIp(String currentIp) {
        this.currentIp = currentIp;
    }

    public String getCurrentIp() {
        return currentIp;
    }

    public String getCurrentS3URL() {
        return currentS3URL;
    }
}
