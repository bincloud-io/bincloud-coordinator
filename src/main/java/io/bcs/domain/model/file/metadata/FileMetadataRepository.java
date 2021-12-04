package io.bcs.domain.model.file.metadata;

import java.util.Optional;

public interface FileMetadataRepository {
    public Optional<FileMetadataView> findById(String storageFileName);
}
