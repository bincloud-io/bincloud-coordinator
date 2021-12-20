package io.bcs.fileserver.domain.model.file.content;

import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise;
import io.bcs.fileserver.domain.model.file.File;
import io.bcs.fileserver.domain.model.file.content.FileContent.ContentType;
import lombok.RequiredArgsConstructor;

/**
 * This class performs file content downloading process.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class ContentDownloader {
  private static final ApplicationLogger log = Loggers.applicationLogger(File.class);
  
  private final ContentReceiver contentReceiver;

  /**
   * Download file content.
   *
   * @param fileContent The file content
   * @return Download operation complete promise
   */
  public Promise<Void> downloadContent(FileContent fileContent) {
    if (fileContent.getType() == ContentType.RANGE) {
      log.debug("Download single-range file content");
      return contentReceiver.receiveContentRange(fileContent);
    }

    if (fileContent.getType() == ContentType.MULTIRANGE) {
      log.debug("Download multi-range file content");
      return contentReceiver.receiveContentRanges(fileContent);
    }

    log.debug("Download full-size file content");
    return contentReceiver.receiveFullContent(fileContent);
  }
}
