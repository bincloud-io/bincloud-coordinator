package io.bcs.fileserver.domain.model.file;

import io.bcs.fileserver.domain.errors.FileDisposedException;
import io.bcs.fileserver.domain.model.file.state.FileStatus;
import io.bcs.fileserver.domain.model.storage.ContentLocator;
import io.bcs.fileserver.domain.model.storage.FileStorage;
import java.util.Optional;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * This class represents the entity, describes a file, which is registered into system.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@Getter
@ToString
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class FileDescriptor {
  static final String DEFAULT_STORAGE_NAME = "unknown";
  static final String DEFAULT_STORAGE_FILE_NAME = "unknown";
  static final String DEFAULT_MEDIA_TYPE = "application/octet-stream";
  static final String DEFAULT_FILE_NAME = "unknown";

  @Include
  @Default
  private String storageFileName = DEFAULT_STORAGE_FILE_NAME;
  @Default
  private String storageName = DEFAULT_STORAGE_NAME;
  @Default
  private FileStatus status = FileStatus.DRAFT;
  @Default
  private String mediaType = DEFAULT_MEDIA_TYPE;
  @Default
  private String fileName = DEFAULT_FILE_NAME;
  @Default
  private Long totalLength = 0L;

  /**
   * Create file descriptor.
   *
   * @param fileStorage The file storage
   * @param command     The file descriptor creating command
   */
  public FileDescriptor(FileStorage fileStorage, CreateFile command) {
    super();
    String mediaType = command.getMediaType().orElse(DEFAULT_MEDIA_TYPE);
    ContentLocator createdFile = fileStorage.create(mediaType);
    this.storageFileName = createdFile.getStorageFileName();
    this.storageName = createdFile.getStorageName();
    this.status = FileStatus.DRAFT;
    this.mediaType = mediaType;
    this.fileName = command.getFileName().orElse(storageFileName);
    this.totalLength = 0L;
  }

  /**
   * Get content locator, specifying file and where it is keeping.
   *
   * @return The content locator
   */
  public ContentLocator getContentLocator() {
    return new ContentLocator() {
      @Override
      public String getStorageName() {
        return storageName;
      }

      @Override
      public String getStorageFileName() {
        return storageFileName;
      }
    };
  }

  /**
   * Dispose file if it had not been disposed yet.
   */
  public void dispose() {
    checkThatFileHasNotBeenDisposedYet();
    this.status = FileStatus.DISPOSED;
    this.totalLength = 0L;
  }

  private void checkThatFileHasNotBeenDisposedYet() {
    if (status == FileStatus.DISPOSED) {
      throw new FileDisposedException();
    }
  }

  /**
   * This interface describes a file entity creation command.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public interface CreateFile {
    Optional<String> getMediaType();

    Optional<String> getFileName();
  }
}
