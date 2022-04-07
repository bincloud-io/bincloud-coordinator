package io.bcs.fileserver.domain.model.storage;

import lombok.Getter;
import lombok.NonNull;

/**
 * This class enumerates all possible storage types.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public enum StorageType {
  LOCAL,
  REMOTE,
  UNKNOWN;

  @Getter
  @NonNull
  @SuppressWarnings("rawtypes")
  private FileStorageProvider registeredFileStorageProvider = descriptor -> new NullFileStorage();

  public <D extends StorageDescriptor> void registerFileStorageProvider(
      FileStorageProvider<D> storageProvider) {
    this.registeredFileStorageProvider = storageProvider;
  }

  private static class NullFileStorage implements FileStorage {
  }
}
