package io.bcs.fileserver.domain.model.file;

import io.bce.Generator;
import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bcs.fileserver.domain.errors.FileDisposedException;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * This class implements the file entity.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@Getter
@ToString
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class File {
  protected static final ApplicationLogger log = Loggers.applicationLogger(File.class);
  static final String DEFAULT_STORAGE_FILE_NAME = "UNKNOWN";
  static final String DEFAULT_MEDIA_TYPE = "application/octet-stream";
  static final String DEFAULT_FILE_NAME = "UNKNOWN";

  @Include
  @Default
  private String storageFileName = DEFAULT_STORAGE_FILE_NAME;
  @Getter(value = AccessLevel.NONE)
  private String storageName;
  @Default
  private FileStatus status = FileStatus.DRAFT;
  @Default
  private String mediaType = DEFAULT_MEDIA_TYPE;
  @Default
  private String fileName = DEFAULT_FILE_NAME;
  @Default
  private Long totalLength = 0L;

  /**
   * Create file.
   *
   * @param filenameGenerator The file name generator, generating unique filename
   * @param creationData      The file creation data
   */
  public File(Generator<String> filenameGenerator, CreationData creationData) {
    super();
    this.storageFileName = filenameGenerator.generateNext();
    this.mediaType = creationData.getMediaType();
    this.fileName = creationData.getFileName().orElse(storageFileName);
    this.status = FileStatus.DRAFT;
    this.totalLength = 0L;
  }

  /**
   * Get the storage name value. This value is optional and may be not assigned.
   *
   * @return The storage name
   */
  public Optional<String> getStorageName() {
    return Optional.ofNullable(storageName);
  }

  /**
   * Start file distribution.
   *
   * @param storageName   The storage file name
   * @param contentLength The distributed content length
   */
  public void startFileDistribution(String storageName, Long contentLength) {
    this.status = FileStatus.DISTRIBUTING;
    this.totalLength = contentLength;
    this.storageName = storageName;
  }

  public void relocateFile(String storageName) {
    this.storageName = storageName;
  }

  /**
   * Dispose file if it had not been disposed yet.
   */
  public void dispose() {
    checkThatFileHasNotBeenDisposedYet();
    this.status = FileStatus.DISPOSED;
    this.storageName = null;
    this.totalLength = 0L;
  }

  private void checkThatFileHasNotBeenDisposedYet() {
    if (status == FileStatus.DISPOSED) {
      throw new FileDisposedException();
    }
  }

  /**
   * This interface describes the data set required for file creation.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public interface CreationData {
    /**
     * Get media type. This is optional value and it may be absent.
     *
     * @return The media type
     */
    public String getMediaType();

    /**
     * Get file name which will be assigned to file on download. This is optional value and may be
     * absent.
     *
     * @return The file name
     */
    public Optional<String> getFileName();
  }
}
