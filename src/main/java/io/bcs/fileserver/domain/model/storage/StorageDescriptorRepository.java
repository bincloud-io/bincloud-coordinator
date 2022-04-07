package io.bcs.fileserver.domain.model.storage;

import java.util.Optional;

/**
 * This interface describes the {@link StorageDescriptor} entity repository.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface StorageDescriptorRepository {
  /**
   * Find storage descriptor by name.
   *
   * @param storageName The storage name
   * @return The storage descriptor
   */
  Optional<StorageDescriptor> findStorageDescriptor(String storageName);
}
