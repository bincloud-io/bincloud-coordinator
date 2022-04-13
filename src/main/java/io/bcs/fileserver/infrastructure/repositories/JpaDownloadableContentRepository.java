package io.bcs.fileserver.infrastructure.repositories;

import io.bcs.fileserver.domain.model.DistributionPointNameProvider;
import io.bcs.fileserver.domain.model.file.FileId;
import io.bcs.fileserver.domain.model.file.content.download.DownloadableContent;
import io.bcs.fileserver.domain.model.file.content.download.DownloadableContentRepository;
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
public class JpaDownloadableContentRepository implements DownloadableContentRepository {
  private final EntityManager entityManager;
  private final DistributionPointNameProvider distributionPointNameProvider;

  @Override
  public Optional<DownloadableContent> findBy(String storageFileName) {
    FileId fileId =
        new FileId(distributionPointNameProvider.getDistributionPointName(), storageFileName);
    return Optional.ofNullable(entityManager.find(DownloadableContent.class, fileId));
  }
}
