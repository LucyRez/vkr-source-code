package cs.hse.scansprovider.fileManagement.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class UploadFilesResponse {
    private List<String> fileNames;
}
