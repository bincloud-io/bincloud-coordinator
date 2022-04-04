package io.bcs.fileserver.infrastructure.repositories;

import io.bcs.fileserver.domain.model.DistributionPointNameProvider;
import io.bcs.fileserver.domain.model.storage.descriptor.LocalStorageDescriptor;
import io.bcs.fileserver.domain.model.storage.descriptor.StorageDescriptor;
import io.bcs.fileserver.domain.model.storage.descriptor.StorageDescriptorId;
import io.bcs.fileserver.domain.model.storage.descriptor.StorageDescriptorRepository;
import java.util.Optional;
import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * This class implements the local storage descriptors repository, using JPA framework.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class JpaStorageDescriptorRepository implements StorageDescriptorRepository {
  private final EntityManager entityManager;
  private final DistributionPointNameProvider distributionPointNameProvider;

  @Override
  public Optional<StorageDescriptor> findByName(String storageName) {
    StorageDescriptorId storageDescriptorId = new StorageDescriptorId(
        distributionPointNameProvider.getDistributionPointName(), storageName);
    return Optional
        .ofNullable(entityManager.find(LocalStorageDescriptor.class, storageDescriptorId));
  }
}
