package io.bcs.fileserver.domain.model.file;

import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bcs.fileserver.domain.model.file.content.ContentDownloader;
import io.bcs.fileserver.domain.model.file.lifecycle.Lifecycle;
import io.bcs.fileserver.domain.model.file.state.FileStatus;
import io.bcs.fileserver.domain.model.file.state.FileStatus.FileEntityAccessor;
import io.bcs.fileserver.domain.model.file.state.FileStatus.FileState;
import io.bcs.fileserver.domain.model.storage.ContentFragment;
import io.bcs.fileserver.domain.model.storage.ContentLocator;
import io.bcs.fileserver.domain.model.storage.FileStorage;
import java.util.Collection;
import java.util.Optional;
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

  private File(ContentLocator contentLocator, CreateFile createFileCommand) {
    super();
    this.storageName = contentLocator.getStorageName();
    this.mediaType = extractMediaType(createFileCommand);
    this.storageFileName = contentLocator.getStorageFileName();
    this.fileName = createFileCommand.getFileName().orElse(storageFileName);
    this.status = FileStatus.DRAFT;
    this.totalLength = 0L;
  }

  /**
   * Create a file entity.
   *
   * @param fileStorage The file storage
   * @param command     The file creation command
   * @return The operation result promise
   */
  public static final Promise<File> create(FileStorage fileStorage, CreateFile command) {
    return Promises.of(deferred -> {
      deferred.resolve(new File(fileStorage.create(extractMediaType(command)), command));
    });
  }

  /**
   * Get the file content locator.
   *
   * @return The file content locator
   */
  public ContentLocator getLocator() {
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
   * Download file content.
   *
   * @param fileStorage       The file storage
   * @param contentDownloader The content downloader
   * @param ranges            The file ranges
   * @return The downloading process completion promise
   */
  public Promise<Void> downloadContent(FileStorage fileStorage, ContentDownloader contentDownloader,
      Collection<Range> ranges) {
    return Promises.of(deferred -> {
      getFileState().getContentAccess(fileStorage, createFragments(ranges)).chain(fileContent -> {
        return contentDownloader.downloadContent(fileContent);
      }).delegate(deferred);
    });
  }

  private Collection<ContentFragment> createFragments(Collection<Range> ranges) {
    FileFragments fragments = new FileFragments(ranges, totalLength);
    return fragments.getParts();
  }

  public Lifecycle getLifecycle(FileStorage storage) {
    return getFileState().getLifecycle(storage);
  }

  private static String extractMediaType(CreateFile command) {
    return command.getMediaType().orElse(DEFAULT_MEDIA_TYPE);
  }

  private FileState getFileState() {
    return this.status.createState(createEntityAccessor());
  }

  private FileEntityAccessor createEntityAccessor() {
    return new FileEntityAccessor() {
      @Override
      public ContentLocator getLocator() {
        return File.this.getLocator();
      }

      @Override
      public Long getTotalLength() {
        return File.this.totalLength;
      }

      @Override
      public void dispose() {
        File.this.dispose();
      }

      @Override
      public void startFileDistribution(Long contentLength) {
        File.this.startFileDistribution(contentLength);
      }
    };
  }

  private void startFileDistribution(Long contentLength) {
    this.totalLength = contentLength;
    this.status = FileStatus.DISTRIBUTING;
  }

  private void dispose() {
    this.status = FileStatus.DISPOSED;
    this.totalLength = 0L;
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
