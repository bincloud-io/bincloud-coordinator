package io.bcs.fileserver.domain.model.file.content;

import io.bce.promises.Promise;
import io.bcs.fileserver.domain.model.file.Range;
import java.util.Collection;
import java.util.Optional;

/**
 * This interface describes the content management operations.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface ContentManagement {

  /**
   * Upload file content.
   *
   * @param storageFileName The storage file name
   * @param uploader        The file content uploader
   * @return The file upload statistic promise
   */
  Promise<FileUploadStatistic> upload(Optional<String> storageFileName, ContentUploader uploader);

  /**
   * Download file content.
   *
   * @param command    The storage command
   * @param downloader The file download command
   * @return The file download completion promise
   */
  Promise<Void> download(DownloadCommand command, ContentDownloader downloader);

  /**
   * This interface describes a content download command.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public interface DownloadCommand {
    Optional<String> getStorageFileName();

    Collection<Range> getRanges();
  }
}