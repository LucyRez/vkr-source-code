package cs.hse.scansprovider.fileManagement.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class RenameFileRequest {
    private String oldPath;
    private String newPath;
}
