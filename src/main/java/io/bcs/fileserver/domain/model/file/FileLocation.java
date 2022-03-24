package io.bcs.fileserver.domain.model.file;

import io.bcs.fileserver.domain.model.storage.ContentLocator;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

  @Default
  private boolean active = false;

  /**
   * Create file location entity from created file state.
   *
   * @param createdFileState The created file state
   */
  public FileLocation(ContentLocator createdFileState) {
    super();
    this.storageFileName = createdFileState.getStorageFileName();
    this.storageName = createdFileState.getStorageName();
    this.lastModification = LocalDateTime.now();
    this.active = true;
  }

  /**
   * Relocate file to the anotyer storage.
   *
   * @param storageName The storage name
   * @return The collection, containing old passivated location and new active location
   */
  public Collection<FileLocation> relocateTo(String storageName) {
    Collection<FileLocation> locations = new HashSet<FileLocation>();
    FileLocation fileLocation =
        new FileLocation(this.storageFileName, storageName, this.lastModification, true);
    locations.add(fileLocation);
    locations.add(this);
    this.active = false;
    return locations;
  }
}
