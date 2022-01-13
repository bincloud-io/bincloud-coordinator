package io.bcs.fileserver.domain.model.file.state;

import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bcs.fileserver.domain.errors.ContentNotUploadedException;
import io.bcs.fileserver.domain.model.file.File;
import io.bcs.fileserver.domain.model.file.Range;
import io.bcs.fileserver.domain.model.file.content.ContentDownloader;
import io.bcs.fileserver.domain.model.file.content.ContentUploader;
import io.bcs.fileserver.domain.model.file.content.FileUploadStatistic;
import java.util.Collection;

/**
 * This class implements the draft file state.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class FileDraftState extends FileState {
  private static final ApplicationLogger log = Loggers.applicationLogger(FileDraftState.class);

  public FileDraftState(File file) {
    super(file);
  }

  @Override
  public Promise<FileUploadStatistic> uploadContent(ContentUploader contentUploader) {
    return Promises.of(deferred -> {
      File file = getFile();
      contentUploader.upload(file).chain(result -> {
        file.startFileDistribution(result.getTotalLength());
        return Promises.resolvedBy(result);
      }).then(deferred);
    });
  }

  @Override
  public Promise<Void> downloadContent(ContentDownloader contentDownloader,
      Collection<Range> ranges) {
    log.debug("The file content download is going to be performed from draft file");
    return Promises.rejectedBy(new ContentNotUploadedException());
  }
}
