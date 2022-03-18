package io.bcs.fileserver.domain.model.storage.descriptor;

import java.util.Optional;

/**
 * This interface describes the {@link StorageDescriptor} entity repository.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface StorageDescriptorRepository {
  
  public Optional<StorageDescriptor> findByName(String storageName);
  
}
