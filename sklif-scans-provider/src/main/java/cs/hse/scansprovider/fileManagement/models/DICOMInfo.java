package cs.hse.scansprovider.fileManagement.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class DICOMInfo {
    String patientName;
    String studyDescription;
    String seriesDescription;
    String studyDate;
    String seriesDate;
    String acquisitionDateTime;
    String studyTime;
    String seriesTime;
    String modality;
    String manufacturer;
    String institutionName;
    String institutionAddress;
    String physicianName;
    String physicianAddress;
    String physicianPhoneNumbers;
    String codeMeaning;
    String stationName;
    String departmentName;
    String textComments;
    String reviewDate;
    String reviewTime;
    String reviewerName;
    String windowCenter;
    String windowWidth;
    String grayScale;
    String studyId;
    String dataType;
    String performingPhysician;
    String operatorName;
    String patientId;
    String birthDate;
    String patientSex;
    String patientAge;
    String patientSize;
    String patientWeight;
    String patientAddress;
    String country;
    String region;
    String telephone;
    String occupation;
    String patientComments;
    String commentsGeneral; // Artificially added

}
