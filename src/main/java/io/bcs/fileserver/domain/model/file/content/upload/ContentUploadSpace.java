package io.bcs.fileserver.domain.model.file.content.upload;

import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bcs.fileserver.domain.errors.ContentUploadedException;
import io.bcs.fileserver.domain.errors.FileDisposedException;
import io.bcs.fileserver.domain.model.file.FileStatus;
import io.bcs.fileserver.domain.model.storage.ContentLocator;
import io.bcs.fileserver.domain.model.storage.DefaultContentLocator;
import io.bcs.fileserver.domain.model.storage.StorageDescriptor;
import lombok.AccessLevel;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * This class describes the entity, describing a space for content uploading of a concrete file.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@ToString
@SuperBuilder
@NoArgsConstructor
@Getter(value = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ContentUploadSpace {
  private static final ApplicationLogger log = Loggers.applicationLogger(ContentUploadSpace.class);

  private static final String DEFAULT_DISTRIBUTION_POINT = "DEFAULT";
  private static final String DEFAULT_STORAGE_FILE_NAME = "UNKNOWN";
  private static final String DEFAULT_MEDIA_TYPE = "application/octet-stream";

  @Include
  @Default
  private String storageFileName = DEFAULT_STORAGE_FILE_NAME;

  @Include
  @Default
  private String distributionPointName = DEFAULT_DISTRIBUTION_POINT;

  @Default
  private String mediaType = DEFAULT_MEDIA_TYPE;

  @Default
  private FileStatus status = FileStatus.DRAFT;

  /**
   * Upload file content.
   *
   * @param contentSource The content source
   * @param contentLength The content length
   * @return The file upload statistic promise
   */
  public Promise<FileUploadStatistic> uploadContent(ContentSource contentSource, Long contentLength,
      StorageProvider storageProvider) {
    return getFileState().uploadContent(contentSource, contentLength, storageProvider);
  }

  private FileState getFileState() {
    if (status == FileStatus.DRAFT) {
      return new DraftFile();
    }

    if (status == FileStatus.DISTRIBUTING) {
      return new DistributioningFile();
    }

    return new DisposedState();
  }

  /**
   * This component provides the storage descriptor, prepared for upload.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public interface StorageProvider {
    /**
     * Return storage descriptor with allocated space for upload.
     *
     * @param mediaType       The media type
     * @param storageFileName The storage file name
     * @param contentLength   The content length
     * @return The storage descriptor
     */
    StorageDescriptor allocateStorageForUpload(String mediaType, String storageFileName,
        Long contentLength);
  }

  private interface FileState {
    Promise<FileUploadStatistic> uploadContent(ContentSource contentSource, Long contentLength,
        StorageProvider storageProvider);
  }

  private class DraftFile implements FileState {
    @Override
    public Promise<FileUploadStatistic> uploadContent(ContentSource contentSource,
        Long contentLength, StorageProvider storageProvider) {
      return Promises.of(deferred -> {
        StorageDescriptor storageDescriptor =
            storageProvider.allocateStorageForUpload(mediaType, storageFileName, contentLength);
        ContentLocator contentLocator =
            new DefaultContentLocator(storageFileName, storageDescriptor.getStorageName());
        Destination<BinaryChunk> destination =
            storageDescriptor.getStorage().getAccessOnWrite(contentLocator);
        contentSource.sendContent(contentLocator, destination).delegate(deferred);
      });
    }
  }

  private class DistributioningFile implements FileState {
    @Override
    public Promise<FileUploadStatistic> uploadContent(ContentSource contentSource,
        Long contentLength, StorageProvider storageProvider) {
      log.debug("The file content upload is going to be performed from distributed file");
      return Promises.rejectedBy(new ContentUploadedException());
    }
  }

  private class DisposedState implements FileState {
    @Override
    public Promise<FileUploadStatistic> uploadContent(ContentSource contentSource,
        Long contentLength, StorageProvider storageProvider) {
      log.debug("The file content upload is going to be performed from disposed file");
      return Promises.rejectedBy(new FileDisposedException());
    }
  }
}
