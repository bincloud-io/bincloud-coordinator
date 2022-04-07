package io.bcs.fileserver.domain.model.content;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * This class enumerates all possible storage types.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public enum StorageType {
  LOCAL(descriptor -> new NullFileStorage()),
  REMOTE(descriptor -> new NullFileStorage()),
  UNKNOWN(descriptor -> new NullFileStorage());

  @Getter
  @NonNull
  @SuppressWarnings("rawtypes")
  private FileStorageProvider registeredFileStorageProvider;

  public <D extends StorageDescriptor> void registerFileStorageProvider(
      FileStorageProvider<D> storageProvider) {
    this.registeredFileStorageProvider = storageProvider;
  }

  /**
   * This interface describes the component providing the concrete {@link FileStorage}
   * implementation for corresponding storage descriptor.
   *
   * @author Dmitry Mikhaylenko
   *
   * @param <S> The concrete storage descriptor type
   */
  public interface FileStorageProvider<S extends StorageDescriptor> {
    FileStorage getFileStorage(S storage);
  }

  private static class NullFileStorage implements FileStorage {
  }
}
