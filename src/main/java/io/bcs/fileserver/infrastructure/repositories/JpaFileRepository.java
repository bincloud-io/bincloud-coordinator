package io.bcs.fileserver.infrastructure.repositories;

import io.bcs.fileserver.domain.model.file.File;
import io.bcs.fileserver.domain.model.file.FileRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
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
  public Collection<File> findNotRemovedDisposedFiles() {
    TypedQuery<File> query =
        entityManager.createNamedQuery("File.findNotRemovedDisposedFiles", File.class);
    LocalDateTime disposedMinDate = LocalDateTime.now().minusDays(1);
    query.setParameter("disposedAfter", disposedMinDate);
    return Collections.unmodifiableList(query.getResultList());
  }

  @Override
  @SneakyThrows
  public void save(File file) {
    transactionManager.begin();
    entityManager.merge(file);
    transactionManager.commit();
  }
}
