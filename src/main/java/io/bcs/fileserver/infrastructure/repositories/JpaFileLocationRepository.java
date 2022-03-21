package io.bcs.fileserver.infrastructure.repositories;

import io.bcs.fileserver.domain.model.file.FileLocation;
import io.bcs.fileserver.domain.model.file.FileLocationRepository;
import java.util.Collection;
import javax.persistence.EntityManager;
import javax.transaction.TransactionManager;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * This class implements the file location repository, using JPA framework.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class JpaFileLocationRepository implements FileLocationRepository {
  private final EntityManager entityManager;
  private final TransactionManager transactionManager;

  @Override
  @SneakyThrows
  public void save(Collection<FileLocation> fileLocations) {
    transactionManager.begin();
    fileLocations.forEach(entityManager::merge);
    transactionManager.commit();
  }
}
