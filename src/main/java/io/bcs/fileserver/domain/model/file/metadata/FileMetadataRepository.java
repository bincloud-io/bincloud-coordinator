package io.bcs.fileserver.domain.model.file.metadata;

import java.util.Optional;

/**
 * This interface describes the file metadata repository.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface FileMetadataRepository {
  Optional<FileMetadataView> findById(String storageFileName);
}
