package io.bcs.fileserver.domain.services;

import io.bce.domain.EventBus;
import io.bce.domain.EventPublisher;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bcs.fileserver.domain.Constants;
import io.bcs.fileserver.domain.errors.FileNotExistsException;
import io.bcs.fileserver.domain.errors.FileNotSpecifiedException;
import io.bcs.fileserver.domain.events.FileContentHasBeenUploaded;
import io.bcs.fileserver.domain.events.FileDownloadHasBeenRequested;
import io.bcs.fileserver.domain.model.content.Content;
import io.bcs.fileserver.domain.model.content.ContentReceiver;
import io.bcs.fileserver.domain.model.content.ContentRepository;
import io.bcs.fileserver.domain.model.content.ContentSource;
import io.bcs.fileserver.domain.model.content.FileUploadStatistic;
import io.bcs.fileserver.domain.model.content.Range;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

/**
 * This class implements the service, managing file content.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class ContentService {
  private final ContentRepository contentRepository;
  private final EventBus eventBus;

  /**
   * Upload file content.
   *
   * @param command       The upload command
   * @param contentSource The content source
   * @return The file upload promise
   */
  public Promise<FileUploadStatistic> upload(UploadCommand command, ContentSource contentSource) {
    return Promises.of(deferred -> {
      Content content =
          retrieveExistingFileContent(extractStorageFileName(command::getStorageFileName));
      content.uploadContent(contentSource, command.getContentLength())
          .then(ContentService.this::notifyAboutUploadedContent).delegate(deferred);
    });
  }

  /**
   * Download file content.
   *
   * @param command         The file download command
   * @param contentReceiver The content receiver
   * @return The file download promise
   */
  public Promise<Void> download(DownloadCommand command, ContentReceiver contentReceiver) {
    EventPublisher<FileDownloadHasBeenRequested> publisher =
        eventBus.getPublisher(Constants.CONTEXT, FileDownloadHasBeenRequested.EVENT_TYPE);
    return Promises.of(deferred -> {
      publisher.publish(new FileDownloadHasBeenRequested(command.getStorageFileName()));
      Content content =
          retrieveExistingFileContent(extractStorageFileName(command::getStorageFileName));
      content.downloadContent(command.getRanges(), contentReceiver).delegate(deferred);
    });
  }

  private void notifyAboutUploadedContent(FileUploadStatistic uploadStatistic) {
    EventPublisher<FileContentHasBeenUploaded> publisher =
        eventBus.getPublisher(Constants.CONTEXT, FileContentHasBeenUploaded.EVENT_TYPE);
    publisher.publish(new FileContentHasBeenUploaded(uploadStatistic));
  }

  private Content retrieveExistingFileContent(String storageFileName) {
    return contentRepository.findBy(storageFileName).orElseThrow(() -> {
      return new FileNotExistsException();
    });
  }

  private String extractStorageFileName(Supplier<Optional<String>> storageFileNameProvider) {
    return storageFileNameProvider.get().orElseThrow(() -> {
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

  /**
   * This interface describes a content upload command.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public interface UploadCommand {
    /**
     * Get storage file name.
     *
     * @return The storage file name
     */
    Optional<String> getStorageFileName();

    /**
     * The file content length.
     *
     * @return The content length
     */
    Long getContentLength();
  }
}
