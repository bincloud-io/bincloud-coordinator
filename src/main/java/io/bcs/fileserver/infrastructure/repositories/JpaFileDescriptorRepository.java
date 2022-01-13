package io.bcs.fileserver.infrastructure.repositories;

import io.bcs.fileserver.domain.model.file.FileDescriptor;
import io.bcs.fileserver.domain.model.file.FileDescriptorRepository;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.transaction.TransactionManager;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * This class implements the file descriptor repository, using JPA framework.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class JpaFileDescriptorRepository implements FileDescriptorRepository {
  private final EntityManager entityManager;
  private final TransactionManager transactionManager;

  @Override
  public Optional<FileDescriptor> findById(String storageFileName) {
    return Optional.ofNullable(entityManager.find(FileDescriptor.class, storageFileName));
  }

  @Override
  @SneakyThrows
  public void save(FileDescriptor fileDescriptor) {
    transactionManager.begin();
    entityManager.merge(fileDescriptor);
    transactionManager.commit();
  }
}
