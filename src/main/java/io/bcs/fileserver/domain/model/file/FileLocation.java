package io.bcs.fileserver.domain.model.file;

import io.bcs.fileserver.domain.model.file.File.CreatedFileState;
import java.time.LocalDateTime;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * This class represents the file location.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@Getter
@ToString
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class FileLocation {
  static final String DEFAULT_STORAGE_FILE_NAME = "UNKNOWN";
  static final String DEFAULT_STORAGE_NAME = "UNKNOWN";

  @Include
  @Default
  private String storageFileName = DEFAULT_STORAGE_FILE_NAME;

  @Include
  @Default
  private String storageName = DEFAULT_STORAGE_NAME;

  @Default
  private LocalDateTime lastModification = LocalDateTime.now();

  /**
   * Create file location entity from created file state.
   *
   * @param createdFileState The created file state
   */
  public FileLocation(CreatedFileState createdFileState) {
    super();
    this.storageFileName = createdFileState.getStorageFileName();
    this.storageName = createdFileState.getStorageName();
    this.lastModification = createdFileState.getCreatedAt();
  }
}
