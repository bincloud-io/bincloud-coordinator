package io.bcs.fileserver.domain.model.file.state;

import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise;
import io.bcs.fileserver.domain.errors.FileDisposedException;
import io.bcs.fileserver.domain.model.file.content.FileContent;
import io.bcs.fileserver.domain.model.file.state.FileStatus.FileEntityAccessor;
import io.bcs.fileserver.domain.model.file.state.FileStatus.FileState;
import io.bcs.fileserver.domain.model.storage.ContentFragment;
import io.bcs.fileserver.domain.model.storage.FileStorage;
import java.util.Collection;

/**
 * This class implements the disposed file state.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class FileDisposedState extends FileState {
  private static final ApplicationLogger log = Loggers.applicationLogger(FileDisposedState.class);

  public FileDisposedState(FileEntityAccessor fileEntityAccessor) {
    super(fileEntityAccessor);
  }

  @Override
  public Promise<FileContent> getContentAccess(FileStorage fileStorage,
      Collection<ContentFragment> contentFragments) {
    log.debug("The file content download is going to be performed from disposed file");
    throw new FileDisposedException();
  }

  @Override
  public Destination<BinaryChunk> getContentWriter(FileStorage fileStorage) {
    throw new FileDisposedException();
  }
}
