package cs.hse.skliforganizationmanagement.aiService.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@EqualsAndHashCode
@NoArgsConstructor
public class DICOMFile {
    @SequenceGenerator(
            name = "dicom_sequence",
            sequenceName = "dicom_sequence",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(
            strategy = GenerationType.AUTO,
            generator = "dicom_sequence"
    )
    private Long id;
    private String patientName;
    private String filepath;

}
