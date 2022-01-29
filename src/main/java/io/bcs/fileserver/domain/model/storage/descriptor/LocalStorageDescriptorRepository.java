package io.bcs.fileserver.domain.model.storage.descriptor;

import java.util.Collection;
import java.util.Optional;

/**
 * This interface describes the {@link StorageDescriptor} entity repository.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface LocalStorageDescriptorRepository {
  
  public Collection<LocalStorageDescriptor> findByMediaType(String mediaType);
  
  public Optional<LocalStorageDescriptor> findByName(String storageName);
  
}
