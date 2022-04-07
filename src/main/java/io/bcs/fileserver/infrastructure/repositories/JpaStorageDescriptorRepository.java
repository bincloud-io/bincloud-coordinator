package io.bcs.fileserver.infrastructure.repositories;

import io.bcs.fileserver.domain.model.DistributionPointNameProvider;
import io.bcs.fileserver.domain.model.storage.StorageDescriptor;
import io.bcs.fileserver.domain.model.storage.StorageDescriptorRepository;
import io.bcs.fileserver.domain.model.storage.StorageId;
import java.util.Optional;
import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * This class implements the file repository, using JPA framework.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class JpaStorageDescriptorRepository implements StorageDescriptorRepository {
  private final EntityManager entityManager;
  private final DistributionPointNameProvider distributionPointNameProvider;

  @Override
  public Optional<StorageDescriptor> findStorageDescriptor(String storageName) {
    StorageId storageId =
        new StorageId(storageName, distributionPointNameProvider.getDistributionPointName());
    return Optional.ofNullable(entityManager.find(StorageDescriptor.class, storageId));
  }
}
