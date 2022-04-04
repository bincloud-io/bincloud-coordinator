package io.bcs.fileserver.domain.services;

import io.bce.interaction.polling.Polling;
import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bcs.fileserver.domain.errors.FileNotDisposedException;
import io.bcs.fileserver.domain.model.content.FileStorage;
import io.bcs.fileserver.domain.model.file.File;
import io.bcs.fileserver.domain.model.file.FileRepository;
import lombok.RequiredArgsConstructor;

/**
 * This class implements the service, managing file content.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class ContentCleanService {
  private static final ApplicationLogger log = Loggers.applicationLogger(ContentCleanService.class);

  private final FileRepository fileRepository;
  private final FileStorage fileStorage;

  /**
   * Clear all disposed files.
   */
  public void clearDisposedFiles() {
    log.info("Use-case: Close all disposed files.");
    Polling.sequentialPolling(fileRepository::findNotRemovedDisposedFiles)
        .forEach(this::removeDisposedFile);
  }

  private void removeDisposedFile(File file) {
    checkThatFileHasNotBeenDisposed(file);
    fileStorage.delete(file);
    file.clearContentPlacement();
    fileRepository.save(file);
  }

  private void checkThatFileHasNotBeenDisposed(File file) {
    if (file.isNotDisposed()) {
      throw new FileNotDisposedException();
    }
  }
}
