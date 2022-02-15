package io.bcs.fileserver.domain.model.storage.descriptor;

import java.util.Optional;

/**
 * This interface describes the {@link StorageDescriptor} entity repository.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface LocalStorageDescriptorRepository {
  
  public Optional<LocalStorageDescriptor> findByName(String storageName);
  
}
