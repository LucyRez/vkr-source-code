package cs.hse.skliforganizationmanagement.aiService;

import cs.hse.skliforganizationmanagement.ai.client.examples.SimpleInferClient;
import cs.hse.skliforganizationmanagement.aiService.entity.DICOMFile;
import cs.hse.skliforganizationmanagement.aiService.repository.DICOMRepository;
import cs.hse.skliforganizationmanagement.registration.confirmation.ConfirmationToken;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AIService {
    private final DICOMRepository dicomRepository;
    private final SimpleInferClient simpleInferClient = new SimpleInferClient();

    public String getMaskForDICOM(Long id) {
        DICOMFile dicomFile = dicomRepository.findByDICOMId(id)
                .orElseThrow(() -> new IllegalStateException("DICOM File not found"));

        float volume =  simpleInferClient.getMaskData(dicomFile.getFilepath(), dicomFile.getFilepath() +"/masks/");


        return String.format("Total affected volume %.1f cm3 ", volume/1000);
    }

}
