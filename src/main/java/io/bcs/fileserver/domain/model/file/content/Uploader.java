package io.bcs.fileserver.domain.model.file.content;

import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bcs.fileserver.domain.errors.ContentUploadedException;
import io.bcs.fileserver.domain.errors.FileDisposedException;
import io.bcs.fileserver.domain.model.file.File;
import io.bcs.fileserver.domain.model.file.FileStatus;
import io.bcs.fileserver.domain.model.storage.ContentLocator;
import io.bcs.fileserver.domain.model.storage.FileStorage;
import lombok.RequiredArgsConstructor;

/**
 * This class is responsible for content uploading.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class Uploader {
  private static final ApplicationLogger log = Loggers.applicationLogger(Uploader.class);

  private final File file;
  private final FileStorage fileStorage;

  /**
   * Upload content to the specified file.
   *
   * @param contentSource The content source
   * @param contentLength The content length
   * @return The file upload statistic promise
   */
  public Promise<FileUploadStatistic> uploadContent(ContentSource contentSource,
      Long contentLength) {
    return Promises.of(deferred -> {
      getFileUploadState(file).uploadContent(contentSource, contentLength).delegate(deferred);
    });
  }

  private FileUploadState getFileUploadState(File file) {
    FileStatus status = file.getStatus();
    if (status == FileStatus.DRAFT) {
      return new DraftUploadState(file);
    }

    if (status == FileStatus.DISTRIBUTING) {
      return new DistributingUploadState();
    }

    return new DisposedUploadState();
  }

  /**
   * This interface describes the component, sending file content to the storage.
   *
   * @author Dmitry Mikhaylenko
   */
  public interface ContentSource {
    /**
     * Send content to the file.
     *
     * @param contentLocator The content locator
     * @param destination    The content destination
     * @return The promise of uploaded content statistic
     */
    Promise<FileUploadStatistic> sendContent(ContentLocator contentLocator,
        Destination<BinaryChunk> destination);
  }

  private interface FileUploadState {
    Promise<FileUploadStatistic> uploadContent(ContentSource contentSource, Long contentLength);
  }

  @RequiredArgsConstructor
  private class DraftUploadState implements FileUploadState {
    private final File file;

    @Override
    public Promise<FileUploadStatistic> uploadContent(ContentSource contentSource,
        Long contentLength) {
      return Promises.<FileUploadStatistic>of(deferred -> {
        ContentLocator contentLocator = fileStorage.create(file, contentLength);
        file.specifyContentPlacement(contentLocator.getStorageName(), contentLength);
        Destination<BinaryChunk> destination = fileStorage.getAccessOnWrite(file);
        contentSource.sendContent(contentLocator, destination).then(statistic -> {
          file.startFileDistribution();
        }).then(deferred).error(err -> {
          try {
            fileStorage.delete(file);
          } finally {
            deferred.reject(err);
          }
        });
      });
    }
  }

  private class DistributingUploadState implements FileUploadState {
    @Override
    public Promise<FileUploadStatistic> uploadContent(ContentSource contentSource,
        Long contentLength) {
      log.debug("The file content upload is going to be performed from distributed file");
      return Promises.rejectedBy(new ContentUploadedException());
    }
  }

  private class DisposedUploadState implements FileUploadState {
    @Override
    public Promise<FileUploadStatistic> uploadContent(ContentSource contentSource,
        Long contentLength) {
      log.debug("The file content upload is going to be performed from disposed file");
      return Promises.rejectedBy(new FileDisposedException());
    }
  }
}
