package cs.hse.scansprovider.fileManagement.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class CommentRequest {
    private String key;
    private String comment;
}
