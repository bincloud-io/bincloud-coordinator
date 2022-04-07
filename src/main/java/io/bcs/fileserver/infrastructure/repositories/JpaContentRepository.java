package io.bcs.fileserver.infrastructure.repositories;

import io.bcs.fileserver.domain.model.DistributionPointNameProvider;
import io.bcs.fileserver.domain.model.content.Content;
import io.bcs.fileserver.domain.model.content.ContentRepository;
import io.bcs.fileserver.domain.model.file.FileId;
import java.util.Optional;
import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * This class implements the content repository, using JPA framework.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class JpaContentRepository implements ContentRepository {
  private final EntityManager entityManager;
  private final DistributionPointNameProvider distributionPointNameProvider;

  @Override
  public Optional<Content> findBy(String storageFileName) {
    FileId fileId =
        new FileId(distributionPointNameProvider.getDistributionPointName(), storageFileName);
    return Optional.ofNullable(entityManager.find(Content.class, fileId));
  }
}
