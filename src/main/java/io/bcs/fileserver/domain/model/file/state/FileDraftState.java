package io.bcs.fileserver.domain.model.file.state;

import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise;
import io.bcs.fileserver.domain.errors.ContentNotUploadedException;
import io.bcs.fileserver.domain.model.file.content.ContentUploader;
import io.bcs.fileserver.domain.model.file.content.FileContent;
import io.bcs.fileserver.domain.model.file.lifecycle.Lifecycle;
import io.bcs.fileserver.domain.model.file.lifecycle.LifecycleUploadFileMethod;
import io.bcs.fileserver.domain.model.file.state.FileStatus.FileEntityAccessor;
import io.bcs.fileserver.domain.model.file.state.FileStatus.FileState;
import io.bcs.fileserver.domain.model.storage.ContentFragment;
import io.bcs.fileserver.domain.model.storage.FileStorage;
import java.util.Collection;

/**
 * This class implements the draft file state.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class FileDraftState extends FileState {
  private static final ApplicationLogger log = Loggers.applicationLogger(FileDraftState.class);

  public FileDraftState(FileEntityAccessor fileEntityAccessor) {
    super(fileEntityAccessor);
  }

  @Override
  public Promise<FileContent> getContentAccess(FileStorage fileStorage,
      Collection<ContentFragment> contentFragments) {
    log.debug("The file content download is going to be performed from draft file");
    throw new ContentNotUploadedException();
  }

  @Override
  public Lifecycle getLifecycle(FileStorage storage) {
    return new Lifecycle() {
      @Override
      public LifecycleMethod<FileUploadStatistic> upload(ContentUploader uploader) {
        log.debug("The file content upload is going to be performed for draft file");
        return new LifecycleUploadFileMethod(getFileEntityAccessor(), storage, uploader);
      }
    };
  }
}
