package io.bcs.fileserver.domain.model.file;

import io.bcs.fileserver.domain.model.storage.ContentLocator;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * This class implements the content locator of specified file.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@Getter
@EqualsAndHashCode
public class FileContentLocator implements ContentLocator {
  private String storageFileName;
  private String storageName;

  /**
   * Create file content locator for specified file.
   *
   * @param file The file for which the content locator is created
   */
  public FileContentLocator(File file) {
    super();
    this.storageFileName = file.getStorageFileName();
    this.storageName = file.getStorageName().get();
  }
}
