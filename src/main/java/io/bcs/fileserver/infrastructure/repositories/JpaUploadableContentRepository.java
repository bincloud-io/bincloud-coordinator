package io.bcs.fileserver.infrastructure.repositories;

import io.bcs.fileserver.domain.model.DistributionPointNameProvider;
import io.bcs.fileserver.domain.model.file.FileId;
import io.bcs.fileserver.domain.model.file.content.upload.ContentUploadSpace;
import io.bcs.fileserver.domain.model.file.content.upload.ContentUploadSpaceRepository;
import java.util.Optional;
import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * This class implements the downloadable content repository, using JPA framework.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class JpaUploadableContentRepository implements ContentUploadSpaceRepository {
  private final EntityManager entityManager;
  private final DistributionPointNameProvider distributionPointNameProvider;

  @Override
  public Optional<ContentUploadSpace> findBy(String storageFileName) {
    FileId fileId =
        new FileId(distributionPointNameProvider.getDistributionPointName(), storageFileName);
    return Optional.ofNullable(entityManager.find(ContentUploadSpace.class, fileId));
  }
}
