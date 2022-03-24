package io.bcs.fileserver.domain.services;

import io.bce.domain.EventBus;
import io.bce.domain.EventPublisher;
import io.bce.domain.EventType;
import io.bce.interaction.polling.Polling;
import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bce.text.TextTemplates;
import io.bcs.fileserver.domain.Constants;
import io.bcs.fileserver.domain.errors.FileNotDisposedException;
import io.bcs.fileserver.domain.errors.FileNotExistsException;
import io.bcs.fileserver.domain.errors.FileNotSpecifiedException;
import io.bcs.fileserver.domain.events.FileDistributionHasBeenStarted;
import io.bcs.fileserver.domain.events.FileDownloadHasBeenRequested;
import io.bcs.fileserver.domain.model.file.File;
import io.bcs.fileserver.domain.model.file.FileRepository;
import io.bcs.fileserver.domain.model.file.Range;
import io.bcs.fileserver.domain.model.file.content.Downloader;
import io.bcs.fileserver.domain.model.file.content.Downloader.ContentReceiver;
import io.bcs.fileserver.domain.model.file.content.FileUploadStatistic;
import io.bcs.fileserver.domain.model.file.content.Uploader;
import io.bcs.fileserver.domain.model.file.content.Uploader.ContentSource;
import io.bcs.fileserver.domain.model.storage.FileContentLocator;
import io.bcs.fileserver.domain.model.storage.FileStorage;
import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/**
 * This class implements the service, managing file content.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class ContentService {
  private static final ApplicationLogger log = Loggers.applicationLogger(ContentService.class);

  private final FileRepository fileRepository;
  private final FileStorage fileStorage;
  private final EventBus eventBus;

  /**
   * Upload file content.
   *
   * @param storageFileName The storage file name
   * @param contentLength   The upload content length
   * @param contentSender   The file content sender
   * @return The file upload statistic promise
   */
  public Promise<FileUploadStatistic> upload(Optional<String> storageFileName, Long contentLength,
      ContentSource contentSender) {
    EventPublisher<FileDistributionHasBeenStarted> eventPublisher =
        createPublisher(FileDistributionHasBeenStarted.EVENT_TYPE);
    return Promises.of(deferred -> {
      log.info("Use-case: Upload file.");
      File file = retrieveExistingFile(extractStorageFileName(storageFileName));
      Uploader uploader = new Uploader(file, fileStorage);
      uploader.uploadContent(contentSender, contentLength).then(statistic -> {
        fileRepository.save(file);
        eventPublisher.publish(new FileDistributionHasBeenStarted(new FileContentLocator(file)));
        deferred.resolve(statistic);
      }).error(deferred);
    });
  }

  /**
   * Download file content.
   *
   * @param command         The storage command
   * @param contentReceiver The file content receiver
   * @return The file download completion promise
   */
  public Promise<Void> download(DownloadCommand command, ContentReceiver contentReceiver) {
    EventPublisher<FileDownloadHasBeenRequested> eventPublisher =
        createPublisher(FileDownloadHasBeenRequested.EVENT_TYPE);
    return Promises.of(deferred -> {
      log.info("Use-case: Download file.");
      eventPublisher.publish(new FileDownloadHasBeenRequested(command.getStorageFileName()));
      File file = retrieveExistingFile(extractStorageFileName(command.getStorageFileName()));
      Downloader downloader = new Downloader(file, fileStorage);
      downloader.receiveContent(command.getRanges(), contentReceiver).delegate(deferred);
    });
  }

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

  private <E> EventPublisher<E> createPublisher(EventType<E> eventType) {
    return eventBus.getPublisher(Constants.CONTEXT, eventType);
  }

  private File retrieveExistingFile(String storageFileName) {
    return fileRepository.findById(storageFileName).orElseThrow(() -> {
      log.warn(TextTemplates.createBy("The file with {{storageFileName}} hasn't been found.")
          .withParameter("storageFileName", storageFileName));
      return new FileNotExistsException();
    });
  }

  private static String extractStorageFileName(Optional<String> storageFileName) {
    return storageFileName.orElseThrow(() -> {
      log.warn("Storage file name has not been specified");
      return new FileNotSpecifiedException();
    });
  }

  /**
   * This interface describes a content download command.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public interface DownloadCommand {
    /**
     * Get storage file name.
     *
     * @return The storage file name
     */
    Optional<String> getStorageFileName();

    /**
     * Get content ranges.
     *
     * @return The content ranges
     */
    Collection<Range> getRanges();
  }
}
