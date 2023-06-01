package cs.hse.scansprovider.fileManagement.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@Getter
@Setter
public class FileResponse {
    protected String bucketName;
    protected String key;
    protected String eTag;
    protected long size;
    protected Date lastModified;
    protected String storageClass;
}
