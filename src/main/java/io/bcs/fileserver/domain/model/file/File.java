package io.bcs.fileserver.domain.model.file;

import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise;
import io.bcs.fileserver.domain.model.file.content.ContentDownloader;
import io.bcs.fileserver.domain.model.file.content.ContentUploader;
import io.bcs.fileserver.domain.model.file.content.FileUploadStatistic;
import io.bcs.fileserver.domain.model.file.state.FileState;
import io.bcs.fileserver.domain.model.file.state.FileState.FileEntityAccessor;
import io.bcs.fileserver.domain.model.file.state.FileStatus;
import io.bcs.fileserver.domain.model.storage.ContentLocator;
import io.bcs.fileserver.domain.model.storage.FileStorage;
import java.util.Collection;
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
   * Upload file content.
   *
   * @param fileStorage     The file storage
   * @param contentUploader The content uploader
   * @return The upload process completion promise
   */
  public Promise<FileUploadStatistic> uploadContent(FileStorage fileStorage,
      ContentUploader contentUploader) {
    return getFileState().uploadContent(fileStorage, contentUploader);
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
    return getFileState().downloadContent(fileStorage, contentDownloader, ranges);
  }

  private FileState getFileState() {
    return this.status.createState(createEntityAccessor());
  }

  private void startFileDistribution(Long contentLength) {
    this.totalLength = contentLength;
    this.status = FileStatus.DISTRIBUTING;
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
      public void startFileDistribution(Long contentLength) {
        File.this.startFileDistribution(contentLength);
      }
    };
  }
}
