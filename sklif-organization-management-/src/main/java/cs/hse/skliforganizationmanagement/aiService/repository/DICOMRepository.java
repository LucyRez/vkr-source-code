package cs.hse.skliforganizationmanagement.aiService.repository;

import cs.hse.skliforganizationmanagement.aiService.entity.DICOMFile;
import cs.hse.skliforganizationmanagement.registration.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DICOMRepository extends JpaRepository<DICOMFile, Long> {
    @Query("""
        SELECT d FROM DICOMFile d WHERE d.id = :dicomId
    """)
    Optional<DICOMFile> findByDICOMId(Long dicomId);
}
