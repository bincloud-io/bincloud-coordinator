package io.bcs.fileserver.domain.model.file;

import io.bce.Generator;
import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bcs.fileserver.domain.errors.FileDisposedException;
import io.bcs.fileserver.domain.model.content.FileStorage;
import java.time.LocalDateTime;
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
public class File implements FileStorage.FileDescriptor {
  protected static final ApplicationLogger log = Loggers.applicationLogger(File.class);
  static final String DEFAULT_STORAGE_FILE_NAME = "UNKNOWN";
  static final String DEFAULT_MEDIA_TYPE = "application/octet-stream";
  static final String DEFAULT_FILE_NAME = "UNKNOWN";
  static final String DEFAULT_DISTRIBUTION_POINT = "UNKNOWN";

  @Include
  @Default
  private String distributionPoint = DEFAULT_DISTRIBUTION_POINT;

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

  @Default
  private LocalDateTime createdAt = LocalDateTime.now();

  private LocalDateTime disposedAt;

  /**
   * Create file.
   *
   * @param distributionPoint The distribution point name
   * @param filenameGenerator The file name generator, generating unique filename
   * @param creationData      The file creation data
   */
  public File(String distributionPoint, Generator<String> filenameGenerator,
      CreationData creationData) {
    super();
    this.distributionPoint = distributionPoint;
    this.storageFileName = filenameGenerator.generateNext();
    this.mediaType = creationData.getMediaType();
    this.fileName = creationData.getFileName().orElse(storageFileName);
    this.createdAt = LocalDateTime.now();
    this.status = FileStatus.DRAFT;
    this.totalLength = 0L;
  }

  /**
   * Create file entity copy, created on another node.
   *
   * @param distributionPoint The distribution point, which the file is going to be replicated on
   * @param createdFileState  The file state, created on another node.
   */
  public File(String distributionPoint, CreatedFileState createdFileState) {
    super();
    this.distributionPoint = distributionPoint;
    this.storageFileName = createdFileState.getStorageFileName();
    this.mediaType = createdFileState.getMediaType();
    this.fileName = createdFileState.getFileName();
    this.createdAt = createdFileState.getCreatedAt();
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
   * Check that the file was disposed.
   *
   * @return True if file is disposed and false otherwise
   */
  public boolean isDisposed() {
    return status == FileStatus.DISPOSED;
  }

  /**
   * Check that the file wasn't disposed.
   *
   * @return True if file wasn't disposed and false otherwise
   */
  public boolean isNotDisposed() {
    return !isDisposed();
  }

  /**
   * Get file dispose time.
   *
   * @return The file dispose time
   */
  public Optional<LocalDateTime> getDisposedAt() {
    return Optional.ofNullable(disposedAt);
  }

  /**
   * Start file distribution.
   */
  public void startFileDistribution() {
    this.status = FileStatus.DISTRIBUTING;
  }

  /**
   * Specify where content is going to be placed and how much space is allocated on storage.
   *
   * @param storageName   The storage file name
   * @param contentLength The distributed content length
   */
  public void specifyContentPlacement(String storageName, Long contentLength) {
    this.totalLength = contentLength;
    this.storageName = storageName;
  }

  /**
   * Relocate file to another storage.
   *
   * @param storageName The storage name
   */
  public void relocateFile(String storageName) {
    this.storageName = storageName;
  }

  /**
   * Clear information about content placement.
   */
  public void clearContentPlacement() {
    this.storageName = null;
    this.totalLength = 0L;
  }

  /**
   * Dispose file if it had not been disposed yet.
   */
  public void dispose() {
    checkThatFileHasNotBeenDisposedYet();
    this.status = FileStatus.DISPOSED;
    this.disposedAt = LocalDateTime.now();
  }

  private void checkThatFileHasNotBeenDisposedYet() {
    if (isDisposed()) {
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

  /**
   * This interface describes the file state, created on another distribution point.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public interface CreatedFileState {
    /**
     * Get storage file name.
     *
     * @return The storage file name
     */
    String getStorageFileName();

    /**
     * Get media type.
     *
     * @return The media type
     */
    String getMediaType();

    /**
     * Get file name.
     *
     * @return The file name
     */
    String getFileName();

    /**
     * Get creation moment.
     *
     * @return The file created time
     */
    LocalDateTime getCreatedAt();
  }
}
