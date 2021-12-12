package io.bcs.fileserver.infrastructure.repositories;

import io.bcs.fileserver.domain.model.file.File;
import io.bcs.fileserver.domain.model.file.FileRepository;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.transaction.TransactionManager;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * This class implements the file repository, using JPA framework.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class JpaFileRepository implements FileRepository {
  private final EntityManager entityManager;
  private final TransactionManager transactionManager;

  @Override
  public Optional<File> findById(String storageFileName) {
    return Optional.ofNullable(entityManager.find(File.class, storageFileName));
  }

  @Override
  @SneakyThrows
  public void save(File file) {
    transactionManager.begin();
    entityManager.merge(file);
    transactionManager.commit();
  }
}
