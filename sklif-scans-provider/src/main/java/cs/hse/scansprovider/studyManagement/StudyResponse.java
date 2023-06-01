package cs.hse.scansprovider.studyManagement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StudyResponse {
    protected String key;
    protected Integer imagesCount;
    protected List<String> keys;
    protected String studyId;
    protected String studyDescription;
    protected String studyDate;
    protected String studyTime;
    protected List<String> seriesDescription;
    protected List<String> modality;
    protected String dataType;
    protected String patientId;
    protected String patientName;
    protected String birthDate;
    protected String patientAge;
    protected String patientWeight;
    protected String patientAddress;
    protected String country;
    protected String region;
    protected String telephone;
    protected String occupation;
    protected List<String> patientComments;
    protected String physicianName;
    protected String physicianAddress;
    protected String physicianPhoneNumbers;
    protected String departmentName;
    protected String performingPhysician;
    protected String operatorName;
    protected String reviewDate;
    protected String reviewTime;
    protected String reviewerName;
    protected List<String> textComments;
    protected List<String> commentsGeneral; // Artificially added
 }
