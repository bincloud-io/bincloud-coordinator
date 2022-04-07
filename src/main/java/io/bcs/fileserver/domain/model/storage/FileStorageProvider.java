package io.bcs.fileserver.domain.model.storage;

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