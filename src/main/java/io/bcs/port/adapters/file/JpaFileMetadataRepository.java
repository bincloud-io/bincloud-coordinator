package io.bcs.port.adapters.file;

import java.util.Optional;

import javax.persistence.EntityManager;

import io.bcs.domain.model.file.metadata.FileMetadataRepository;
import io.bcs.domain.model.file.metadata.FileMetadataView;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JpaFileMetadataRepository implements FileMetadataRepository {
    private final EntityManager entityManager;
    
    @Override
    public Optional<FileMetadataView> findById(String storageFileName) {
        return Optional.ofNullable(entityManager.find(FileMetadataView.class, storageFileName));
    }
}
