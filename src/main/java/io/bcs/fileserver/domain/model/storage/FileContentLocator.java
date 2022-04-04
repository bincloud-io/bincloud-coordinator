package io.bcs.fileserver.domain.model.storage;

import io.bcs.fileserver.domain.model.content.FileStorage.FileDescriptor;
import lombok.EqualsAndHashCode;

/**
 * This class implements the content locator of specified file.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@EqualsAndHashCode(callSuper = true)
public class FileContentLocator extends DefaultContentLocator {
  /**
   * Create file content locator for specified file.
   *
   * @param file The file for which the content locator is created
   */
  public FileContentLocator(FileDescriptor file) {
    super(file.getStorageFileName(), file.getStorageName().get());
  }
}
