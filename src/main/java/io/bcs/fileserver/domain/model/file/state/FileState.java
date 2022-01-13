package io.bcs.fileserver.domain.model.file.state;

import io.bce.promises.Promise;
import io.bcs.fileserver.domain.model.file.File;
import io.bcs.fileserver.domain.model.file.Range;
import io.bcs.fileserver.domain.model.file.content.ContentDownloader;
import io.bcs.fileserver.domain.model.file.content.ContentUploader;
import io.bcs.fileserver.domain.model.file.content.FileUploadStatistic;
import java.util.Collection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This abstract class describes a file state, which affects to the life-cycle coordination.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class FileState {
  @Getter(value = AccessLevel.PROTECTED)
  private final File file;

  /**
   * Upload file content.
   *
   * @param contentUploader The content uploader
   * @return The upload process completion promise
   */
  public abstract Promise<FileUploadStatistic> uploadContent(ContentUploader contentUploader);

  /**
   * Download file content.
   *
   * @param contentDownloader The content downloader
   * @param ranges            The file ranges
   * @return The downloading process completion promise
   */
  public abstract Promise<Void> downloadContent(ContentDownloader contentDownloader,
      Collection<Range> ranges);
}