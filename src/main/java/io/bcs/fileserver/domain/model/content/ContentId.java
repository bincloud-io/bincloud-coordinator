package io.bcs.fileserver.domain.model.content;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * The file identifier value object.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ContentId implements Serializable {
  private static final long serialVersionUID = 2815308399379360212L;
  private static final String DEFAULT_STORAGE_FILE_NAME = "UNKNOWN";

  private StorageId storage = new StorageId();
  private String storageFileName = DEFAULT_STORAGE_FILE_NAME;

  /**
   * Create the content identifier.
   *
   * @param distributionPoint The distribution point
   * @param storageName       The storage name
   * @param storageFileName   The storage file name
   */
  public ContentId(String distributionPoint, String storageName, String storageFileName) {
    super();
    this.storage = new StorageId(storageName, distributionPoint);
    this.storageFileName = storageFileName;
  }
}
