package io.bcs.fileserver.domain.model.file;

import io.bce.promises.Promise;
import io.bcs.fileserver.domain.model.file.FileDescriptor.CreateFile;

/**
 * This interface describes file management operations.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface FileManagement {

  /**
   * Create a file.
   *
   * @param command The file creation command
   * @return The file creation result promise
   */
  Promise<String> createFile(CreateFile command);

  /**
   * Dispose file.
   *
   * @param storageFileName The storage file name
   * @return The file dispose complete promise
   */
  Promise<Void> disposeFile(String storageFileName);
}