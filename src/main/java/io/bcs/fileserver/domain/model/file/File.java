package io.bcs.fileserver.domain.model.file;

import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bcs.fileserver.domain.model.file.content.ContentDownloader;
import io.bcs.fileserver.domain.model.file.content.ContentUploader;
import io.bcs.fileserver.domain.model.file.content.FileUploadStatistic;
import io.bcs.fileserver.domain.model.file.state.FileStatus;
import io.bcs.fileserver.domain.model.file.state.FileStatus.FileEntityAccessor;
import io.bcs.fileserver.domain.model.file.state.FileStatus.FileState;
import io.bcs.fileserver.domain.model.storage.ContentFragment;
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
    return Promises.of(deferred -> {
      contentUploader.upload(getLocator(), getFileState().getContentWriter(fileStorage))
          .chain(result -> {
            startFileDistribution(result.getTotalLength());
            return Promises.resolvedBy(result);
          }).then(deferred);
    });
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
    };
  }

  private void startFileDistribution(Long contentLength) {
    this.totalLength = contentLength;
    this.status = FileStatus.DISTRIBUTING;
  }
}
