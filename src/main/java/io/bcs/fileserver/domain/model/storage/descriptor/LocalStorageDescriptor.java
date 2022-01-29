package io.bcs.fileserver.domain.model.storage.descriptor;

import io.bcs.fileserver.domain.model.storage.StorageType;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * This class inherits the {@link StorageDescriptor} and represents the file storage type, which
 * stores files on the local filesystem.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
public class LocalStorageDescriptor extends StorageDescriptor {
  static final String DEFAULT_BASE_DIRECTORY = "";

  @Default
  private String baseDirectory = DEFAULT_BASE_DIRECTORY;

  @Default
  private Long storageSize = 0L;
  
  @Override
  public StorageType getType() {
    return StorageType.LOCAL;
  }
}
