package io.bcs.fileserver.infrastructure.repositories;

import io.bcs.fileserver.domain.model.file.metadata.FileMetadataRepository;
import io.bcs.fileserver.domain.model.file.metadata.FileMetadataView;
import java.util.Optional;
import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * This class implements the file metadata repository, using JPA framework.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class JpaFileMetadataRepository implements FileMetadataRepository {
  private final EntityManager entityManager;

  @Override
  public Optional<FileMetadataView> findById(String storageFileName) {
    return Optional.ofNullable(entityManager.find(FileMetadataView.class, storageFileName));
  }
}
