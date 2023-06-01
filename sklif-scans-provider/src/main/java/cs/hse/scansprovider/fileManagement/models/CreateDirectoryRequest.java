package cs.hse.scansprovider.fileManagement.models;

import lombok.*;

@NoArgsConstructor
@Getter
@EqualsAndHashCode
@ToString

public class CreateDirectoryRequest {
    private String organizationName;
}
