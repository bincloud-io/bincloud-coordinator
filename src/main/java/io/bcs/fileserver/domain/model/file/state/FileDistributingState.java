package io.bcs.fileserver.domain.model.file.state;

import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bcs.fileserver.domain.errors.ContentUploadedException;
import io.bcs.fileserver.domain.model.file.File;
import io.bcs.fileserver.domain.model.file.Range;
import io.bcs.fileserver.domain.model.file.content.ContentDownloader;
import io.bcs.fileserver.domain.model.file.content.ContentUploader;
import io.bcs.fileserver.domain.model.file.content.FileUploadStatistic;
import java.util.Collection;

/**
 * This class implements the distributing file state.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class FileDistributingState extends FileState {
  private static final ApplicationLogger log =
      Loggers.applicationLogger(FileDistributingState.class);

  public FileDistributingState(File fileEntityAccessor) {
    super(fileEntityAccessor);
  }

  @Override
  public Promise<FileUploadStatistic> uploadContent(ContentUploader contentUploader) {
    log.debug("The file content upload is going to be performed from distributed file");
    return Promises.rejectedBy(new ContentUploadedException());
  }

  @Override
  public Promise<Void> downloadContent(ContentDownloader contentDownloader,
      Collection<Range> ranges) {
    return Promises.of(deferred -> {
      File file = getFile();
      log.debug("The file content download is going to be performed from distributioning file");
      contentDownloader.downloadContent(file, ranges).delegate(deferred);
    });
  }



 


}
