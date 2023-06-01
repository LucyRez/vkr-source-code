package cs.hse.scansprovider.fileManagement.models;

import lombok.*;
import java.util.List;

@NoArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class DeleteFilesRequest {
    private List<String> fileNames;
}
