package cs.hse.scansprovider.client;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import cs.hse.scansprovider.fileManagement.models.DICOMInfo;
import cs.hse.scansprovider.studyManagement.StudyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.parameters.P;
import org.springframework.security.web.header.Header;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.dcm4che3.io.DicomInputStream;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AmazonClient {

    public enum ResponseType {
        SUCCESSFUL_DELETE,
        PARTIAL_DELETE,
        S3_ERROR,
        S3_PARSE_CONNECT_ERROR,
        BUCKET_NOT_FOUND,
        FOLDER_CREATION_FAILED,
        FOLDER_CREATION_SUCCESSFUL,
        ORGANIZATION_DIRECTORY_NOT_FOUND,
        SUCCESSFUL_RENAME,
        SUCCESSFUL_COMMENT,
        FILE_NOT_FOUND

    }
    private final AmazonS3 s3client;

    @Value("${amazonProperties.endpointUrl}")
    private String endpointUrl;

    @Value("${amazonProperties.bucketName}")
    private String bucketName;


    // TODO: encrypt the uploaded file
    public String uploadFile(MultipartFile multipartFile, String organizationsName) {
        String fileUrl = "";

        File file = null;

        ListObjectsV2Result result = s3client.listObjectsV2(bucketName, organizationsName+"/");

        if (result.getKeyCount() <= 0) {
            return "";
        }

        try {
            file = convertMultiPartToFile(multipartFile);
            String fileName = generateFileName(multipartFile, organizationsName);

            fileUrl = endpointUrl + "/" + bucketName + "/" + fileName;

            // TODO: encrypt here
            uploadFileTos3bucket(fileName, file);
        } catch (IOException e) {
            fileUrl = "";
            log.error("Cant create file for image", e);
        } finally {
            if (file != null) {
                file.delete();
            }
        }

        return fileUrl;
    }

    public ResponseType deleteFiles(List<String> fileNames) {
        try {
            var keys = fileNames.stream().map(
                    DeleteObjectsRequest.KeyVersion::new
            ).collect(Collectors.toList());

            DeleteObjectsRequest multiObjectDeleteRequest = new DeleteObjectsRequest(bucketName)
                    .withKeys(keys)
                    .withQuiet(false);

            DeleteObjectsResult delObjRes = s3client.deleteObjects(multiObjectDeleteRequest);
            int successfulDeletes = delObjRes.getDeletedObjects().size();

            if (successfulDeletes == fileNames.stream().count()) {
                return ResponseType.SUCCESSFUL_DELETE;
            }

            return ResponseType.PARTIAL_DELETE;
        } catch (AmazonServiceException e) {
            return ResponseType.S3_ERROR;
        } catch (SdkClientException e) {
            return ResponseType.S3_PARSE_CONNECT_ERROR;
        }
    }

    public ResponseType deleteFolder(String folderName) {
        if (!s3client.doesBucketExistV2(bucketName)) {
            return ResponseType.BUCKET_NOT_FOUND;
        }

        ListObjectsV2Result result = s3client.listObjectsV2(bucketName, folderName+"/");

        if (result.getKeyCount() <= 0) {
            return ResponseType.ORGANIZATION_DIRECTORY_NOT_FOUND;
        }

        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                .withBucketName(bucketName)
                .withPrefix(folderName + "/");

        ObjectListing listing = s3client.listObjects(listObjectsRequest);

        for (S3ObjectSummary objectSummary : listing.getObjectSummaries()) {
            s3client.deleteObject(bucketName, objectSummary.getKey());
        }

        return ResponseType.SUCCESSFUL_DELETE;
    }

    public ResponseType createDirectory(String directoryName) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(0);

            InputStream emptyContent = new ByteArrayInputStream(new byte[0]);

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName,
                    directoryName + "/", emptyContent, metadata);
            s3client.putObject(putObjectRequest);
            return ResponseType.FOLDER_CREATION_SUCCESSFUL;
        } catch (Exception ex) {
            return ResponseType.FOLDER_CREATION_FAILED;
        }
    }

    public ResponseType renameFile(String oldPath, String newPath) {
        ListObjectsV2Result result = s3client.listObjectsV2(bucketName, oldPath+"/");

        if (result.getKeyCount() <= 0) {
            // It is a file
            CopyObjectRequest copyObjRequest = new CopyObjectRequest(bucketName,
                    oldPath, bucketName, newPath);
            s3client.copyObject(copyObjRequest);
            s3client.deleteObject(new DeleteObjectRequest(bucketName, oldPath));
            return ResponseType.SUCCESSFUL_RENAME;
        }

        // It is a folder

        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                .withBucketName(bucketName)
                .withPrefix(oldPath+"/");

        ObjectListing listing = s3client.listObjects(listObjectsRequest);

        for (S3ObjectSummary objectSummary : listing.getObjectSummaries()) {
            CopyObjectRequest copyObjRequest = new CopyObjectRequest(bucketName,
                    objectSummary.getKey(), bucketName, objectSummary.getKey().replace(oldPath + "/", newPath + "/"));

            s3client.copyObject(copyObjRequest);
            s3client.deleteObject(bucketName, objectSummary.getKey());
        }

        return ResponseType.SUCCESSFUL_RENAME;
    }

    public List<S3ObjectSummary> getAllFiles() {
        ObjectListing listing = s3client.listObjects(bucketName);
        return listing.getObjectSummaries();
    }

    public List<S3ObjectSummary> getAllFilesWith(String prefix) {
        ObjectListing listing = s3client.listObjects(bucketName, prefix);
        return listing.getObjectSummaries();
    }

    public DICOMInfo getSingleFileInfo(String key) {
        S3Object object = s3client.getObject(new GetObjectRequest(bucketName, key));
        InputStream objectData = object.getObjectContent();

        try {
            DicomInputStream is = new DicomInputStream(objectData);
            Attributes attrs = is.readDataset(-1, -1);
            is.close();

            String patientName = attrs.getString(0x00100010);
            String studyDescription = attrs.getString(0x00081030);
            String seriesDescription = attrs.getString(0x0008103E);
            String studyDate = attrs.getString(0x00080020);
            String seriesDate = attrs.getString(0x00080021);
            String acquisitionDateTime = attrs.getString(0x0008002A);
            String studyTime = attrs.getString(0x00080030);
            String seriesTime = attrs.getString(0x00080031);
            String modality = attrs.getString(0x00080060);
            String manufacturer = attrs.getString(0x00080070);
            String institutionName = attrs.getString(0x00080080);
            String institutionAddress = attrs.getString(0x00080081);
            String physicianName = attrs.getString(0x00080090);
            String physicianAddress = attrs.getString(0x00080092);
            String physicianPhoneNumbers= attrs.getString(0x00080094);
            String codeMeaning = attrs.getString(0x00080104);
            String stationName = attrs.getString(0x00081010);
            String departmentName = attrs.getString(0x00081040);
            String textComments = attrs.getString(0x40004000);
            String reviewDate = attrs.getString(0x300E004);
            String reviewTime = attrs.getString(0x300E0005);
            String reviewerName = attrs.getString(0x300E0008);
            String windowCenter = attrs.getString(0x00281050);
            String windowWidth = attrs.getString(0x00281051);
            String grayScale = attrs.getString(0x00281080);
            String studyId = attrs.getString(0x00200010);
            String dataType = attrs.getString(0x00189808);
            String performingPhysician =  attrs.getString(0x00081050);
            String operatorName =  attrs.getString(0x00081070);
            String patientId =  attrs.getString(0x00100020);
            String birthDate =  attrs.getString(0x00100030);
            String patientSex =  attrs.getString(0x00100040);
            String patientAge =  attrs.getString(0x00101010);
            String patientSize =  attrs.getString(0x00101020);
            String patientWeight =  attrs.getString(0x00101030);
            String patientAddress =  attrs.getString(0x00101040);
            String country =  attrs.getString(0x00102150);
            String region =  attrs.getString(0x00102152);
            String telephone =  attrs.getString(0x00102154);
            String occupation =  attrs.getString(0x00102180);
            String patientComments =  attrs.getString(0x00104000);
            String commentsGeneral = attrs.getString(0x00210011);

            return new DICOMInfo(patientName, studyDescription, seriesDescription, studyDate, seriesDate, acquisitionDateTime,
                    studyTime, seriesTime, modality, manufacturer, institutionName, institutionAddress, physicianName,
                    physicianAddress, physicianPhoneNumbers, codeMeaning, stationName, departmentName, textComments,
                    reviewDate, reviewTime, reviewerName, windowCenter, windowWidth, grayScale, studyId, dataType,
                    performingPhysician, operatorName, patientId, birthDate, patientSex, patientAge, patientSize, patientWeight, patientAddress,
                    country, region, telephone, occupation, patientComments, commentsGeneral);

        } catch (IOException e) {
            return null;
        }

    }

    public StudyResponse getStudyInfo(String key) {
        // Contains / at the end
        ListObjectsV2Result result = s3client.listObjectsV2(bucketName, key);

        if (result.getKeyCount() <= 0) {
            // It is a file
            return null;
        }

        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                .withBucketName(bucketName)
                .withPrefix(key);

        ObjectListing listing = s3client.listObjects(listObjectsRequest);

        var summaries = listing.getObjectSummaries();

        StudyResponse response = new StudyResponse();
        response.setKey(key);

        List<String> seriesDescriptions = new ArrayList<>();
        List<String> modalities = new ArrayList<>();
        List<String> patientComments = new ArrayList<>();
        List<String> textComments = new ArrayList<>();
        List<String> generalComments = new ArrayList<>();
        List<String> keys = new ArrayList<>();

        for (S3ObjectSummary objectSummary : summaries) {
            var obj = getSingleFileInfo(objectSummary.getKey());

            if (obj == null) {
                continue;
            }

            keys.add(objectSummary.getKey());

            var studyId = obj.getStudyId();
            if (studyId != null) {
                response.setStudyId(studyId);
            }

            var studyDescription = obj.getStudyDescription();
            if (studyDescription != null) {
                response.setStudyDescription(studyDescription);
            }

            var studyDate = obj.getStudyDate();
            if (studyDate != null) {
                response.setStudyDate(studyDate);
            }

            var studyTime = obj.getStudyTime();
            if (studyTime != null) {
                response.setStudyTime(studyTime);
            }

            var seriesDescription = obj.getSeriesDescription();
            if (seriesDescription != null) {
                if(!seriesDescriptions.contains(seriesDescription)) {
                    seriesDescriptions.add(seriesDescription);
                }
            }

            var modality = obj.getModality();
            if (modality != null) {
                if (!modalities.contains(modality)) {
                    modalities.add(modality);
                }
            }

            var dataType = obj.getDataType();
            if (dataType != null) {
                response.setDataType(dataType);
            }

            var patientId = obj.getPatientId();
            if (patientId != null) {
                response.setPatientId(patientId);
            }

            var patientName = obj.getPatientName();
            if (patientName != null) {
                response.setPatientName(patientName);
            }

            var birthdate = obj.getBirthDate();
            if (birthdate != null) {
                response.setBirthDate(birthdate);
            }

            var patientAge = obj.getPatientAge();
            if (patientAge != null) {
                response.setPatientAge(patientAge);
            }

            var patientWeight = obj.getPatientWeight();
            if (patientWeight != null) {
                response.setPatientWeight(patientWeight);
            }

            var patientAddress = obj.getPatientAddress();
            if (patientAddress != null) {
                response.setPatientAddress(patientAddress);
            }

            var country = obj.getCountry();
            if (country != null) {
                response.setCountry(country);
            }

            var region = obj.getRegion();
            if (region != null) {
                response.setRegion(region);
            }

            var telephone = obj.getTelephone();
            if (telephone != null) {
                response.setTelephone(telephone);
            }

            var occupation = obj.getOccupation();
            if (occupation != null) {
                response.setOccupation(occupation);
            }

            var patientComment = obj.getPatientComments();
            if (patientComment != null) {
                if (!patientComments.contains(patientComment)) {
                    patientComments.add(patientComment);
                }
            }

            var physicianName = obj.getPhysicianName();
            if (physicianName != null) {
                response.setPhysicianName(physicianName);
            }

            var physicianAddress = obj.getPhysicianAddress();
            if (physicianAddress != null) {
                response.setPhysicianAddress(physicianAddress);
            }

            var physicianPhone = obj.getPhysicianPhoneNumbers();
            if (physicianPhone != null) {
                response.setPhysicianPhoneNumbers(physicianPhone);
            }

            var departmentName = obj.getDepartmentName();
            if (departmentName != null) {
                response.setDepartmentName(departmentName);
            }

            var performingPhysician = obj.getPerformingPhysician();
            if (performingPhysician != null) {
                response.setPerformingPhysician(performingPhysician);
            }

            var operatorName = obj.getOperatorName();
            if (operatorName != null) {
                response.setOperatorName(operatorName);
            }

            var reviewDate = obj.getReviewDate();
            if (reviewDate != null) {
                response.setReviewDate(reviewDate);
            }

            var reviewTime = obj.getReviewTime();
            if (reviewTime != null) {
                response.setReviewTime(reviewTime);
            }

            var reviewerName = obj.getReviewerName();
            if (reviewerName != null) {
                response.setReviewerName(reviewerName);
            }

            var textComment = obj.getTextComments();
            if (textComment != null) {
                if (!textComments.contains(textComment)) {
                    textComments.add(textComment);
                }
            }

            var generalComment = obj.getCommentsGeneral();
            if (generalComment != null) {
                if (!generalComments.contains(generalComment)) {
                    generalComments.add(generalComment);
                }
            }


        }

        response.setKeys(keys);
        response.setImagesCount(keys.size());
        response.setSeriesDescription(seriesDescriptions);
        response.setModality(modalities);
        response.setPatientComments(patientComments);
        response.setTextComments(textComments);
        response.setCommentsGeneral(generalComments);

        return response;
    }

    public List<StudyResponse> getStudies(String directoryName) {
        // Contains / at the end
        ListObjectsV2Result result = s3client.listObjectsV2(bucketName, directoryName);

        if (result.getKeyCount() <= 0) {
            // It is a file
            return null;
        }

        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                .withBucketName(bucketName)
                .withPrefix(directoryName);

        ObjectListing listing = s3client.listObjects(listObjectsRequest);

        var summaries = listing.getObjectSummaries();

        List<StudyResponse> response = new ArrayList<>();
        HashSet<String> tmpStudyDirectories = new HashSet<>();
        List<String> allStudyDirectories = summaries.stream().map(
                (summary) -> {
                    var key = summary.getKey();
                    if (!key.endsWith("/")) {
                        if (key.length() > 0) {
                            int endIndex = key.lastIndexOf("/");
                            if (endIndex != -1) {
                                String tmp = key.substring(0, endIndex);
                                if (!tmpStudyDirectories.contains(tmp)) {
                                    var info = getStudyInfo(tmp);
                                    if (info != null) {
                                        response.add(info);
                                    }
                                    tmpStudyDirectories.add(tmp);
                                    return key;
                                }
                            }
                        }
                    }
                    return null;
                }
        ).toList();

        return response;
    }

    public ResponseType comment(String key, String comment) {
        S3Object object = s3client.getObject(new GetObjectRequest(bucketName, key));

        try {
            InputStream objectData = object.getObjectContent();
            DicomInputStream din = new DicomInputStream(objectData);
            Attributes attrs = din.readDataset(-1, -1);
            attrs.setString(0x00210011, VR.LO, comment);
            byte[] preamble = din.getPreamble();
            Attributes mif = din.getFileMetaInformation();
            din.close();

            var split = key.split("/");
            String fileName = split[split.length-1];
            File file = new File(fileName);
            DicomOutputStream dos = new DicomOutputStream(file);
            dos.setPreamble(preamble);
            dos.writeFileMetaInformation(mif);
            attrs.writeTo(dos);
            dos.close();
            uploadFileTos3bucket(key, file);
            file.delete();

            return ResponseType.SUCCESSFUL_COMMENT;
        } catch (IOException e) {
            return ResponseType.S3_PARSE_CONNECT_ERROR;
        }
    }

    public String getFileBinary(String key) {
        S3Object object = s3client.getObject(new GetObjectRequest(bucketName, key));
        try {
            var arr = IOUtils.toByteArray(object.getObjectContent());
            return Base64.getEncoder().encodeToString(arr);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return "";
    }

    public byte[] getFileByte(String key) {
        S3Object object = s3client.getObject(new GetObjectRequest(bucketName, key));

        try {
            var arr = IOUtils.toByteArray(object.getObjectContent());
            return arr;
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return new byte[0];
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);

        fos.write(file.getBytes());
        fos.close();

        return convFile;
    }

    private String generateFileName(MultipartFile multiPart, String suffix) {
        return suffix + "/" + multiPart.getOriginalFilename().replace(" ", "_");
    }

    private void uploadFileTos3bucket(String fileName, File file) {
        s3client.putObject(new PutObjectRequest(bucketName, fileName, file)
                .withCannedAcl(CannedAccessControlList.PublicRead));
    }

}
