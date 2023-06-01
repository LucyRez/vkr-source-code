package cs.hse.aiclientservice.aiService.s3controller;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AmazonClient {

    public enum ResponseType {
        FOLDER_CREATION_FAILED,
        FOLDER_CREATION_SUCCESSFUL
    }

    private final AmazonS3 s3client;

    @Value("${amazonProperties.endpointUrl}")
    private String endpointUrl;

    @Value("${amazonProperties.bucketName}")
    private String bucketName;

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

    private String checkForDICOM(String key) {
        S3Object object = s3client.getObject(new GetObjectRequest(bucketName, key));
        InputStream objectData = object.getObjectContent();

        try {
            DicomInputStream is = new DicomInputStream(objectData);
            Attributes attrs = is.readDataset(-1, -1);
            is.close();
            String patientName = attrs.getString(0x00100010);
            return key;

        } catch (IOException e) {
            return null;
        }

    }

    public int[] getFileAsArray(String key) {
        S3Object object = s3client.getObject(new GetObjectRequest(bucketName, key));
        try {
            InputStream objectData = object.getObjectContent();
            Iterator<ImageReader> iter = ImageIO.getImageReadersByFormatName("DICOM");
            ImageReader reader = iter.next();
            DicomImageReadParam param = new DicomImageReadParam();
            DicomInputStream stream = new DicomInputStream(objectData);
            reader.setInput(stream);
            BufferedImage image = reader.read(0, param);

            int[] pixelArray = new int[image.getWidth() * image.getHeight()];
            image.getRaster().getPixels(0, 0, image.getWidth(), image.getHeight(), pixelArray);
            return pixelArray;
        } catch (IOException e) {
            return null;
        }
    }

    public List<String> getStudyDICOM(String key) {
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

        List<String> keys = new ArrayList<>();

        for (S3ObjectSummary objectSummary : summaries) {
            var obj = checkForDICOM(objectSummary.getKey());

            if (obj == null) {
                continue;
            }
            keys.add(obj);
        }
        return keys;
    }

    public boolean directoryExistsAndNotEmpty(String key){
        // Contains / at the end
        ListObjectsV2Result result = s3client.listObjectsV2(bucketName, key);
        List<Bucket> buckets = s3client.listBuckets();

        return !(result.getKeyCount() <= 0);
    }

    public void uploadFileTos3bucket(String fileName, File file) {
        s3client.putObject(new PutObjectRequest(bucketName, fileName, file)
                .withCannedAcl(CannedAccessControlList.PublicRead));
    }


}
