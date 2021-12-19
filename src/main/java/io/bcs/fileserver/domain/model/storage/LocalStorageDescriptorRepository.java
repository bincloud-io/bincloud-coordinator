package io.bcs.fileserver.domain.model.storage;

import java.util.Optional;

/**
 * This interface describes the {@link StorageDescriptor} entity repository.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface LocalStorageDescriptorRepository {
  public Optional<LocalStorageDescriptor> findByMediaType(String mediaType);
}
