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
import io.bcs.fileserver.domain.model.file.content.download.ContentReceiver;
import io.bcs.fileserver.domain.model.file.content.download.DownloadableContent;
import io.bcs.fileserver.domain.model.file.content.download.DownloadableContentRepository;
import io.bcs.fileserver.domain.model.file.content.download.Range;
import io.bcs.fileserver.domain.model.file.content.upload.ContentSource;
import io.bcs.fileserver.domain.model.file.content.upload.ContentUploadSpace;
import io.bcs.fileserver.domain.model.file.content.upload.ContentUploadSpaceRepository;
import io.bcs.fileserver.domain.model.file.content.upload.FileUploadStatistic;
import io.bcs.fileserver.domain.model.storage.StorageDescriptorRepository;
import io.bcs.fileserver.infrastructure.storage.FilesystemSpaceManager;
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
  private final FilesystemSpaceManager fileSpaceManager;
  private final StorageDescriptorRepository storageDescriptorRepository;
  private final ContentUploadSpaceRepository uploadableContentRepository;
  private final DownloadableContentRepository downloadableContentRepository;
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
      ContentUploadSpace content = retrieveExistingUploadableFileContent(
          extractStorageFileName(command::getStorageFileName));
      content.uploadContent(contentSource, command.getContentLength(),
          (mediaType, storageFileName, contentLength) -> {
            String storageName =
                fileSpaceManager.allocateSpace(mediaType, storageFileName, contentLength);
            return storageDescriptorRepository.findStorageDescriptor(storageName).get();
          }).then(ContentService.this::notifyAboutUploadedContent).delegate(deferred);
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
      DownloadableContent content = retrieveExistingDownloadableFileContent(
          extractStorageFileName(command::getStorageFileName));
      content.downloadContent(command.getRanges(), contentReceiver).delegate(deferred);
    });
  }

  private void notifyAboutUploadedContent(FileUploadStatistic uploadStatistic) {
    EventPublisher<FileContentHasBeenUploaded> publisher =
        eventBus.getPublisher(Constants.CONTEXT, FileContentHasBeenUploaded.EVENT_TYPE);
    publisher.publish(new FileContentHasBeenUploaded(uploadStatistic));
  }

  private ContentUploadSpace retrieveExistingUploadableFileContent(String storageFileName) {
    return uploadableContentRepository.findBy(storageFileName).orElseThrow(() -> {
      return new FileNotExistsException();
    });
  }

  private DownloadableContent retrieveExistingDownloadableFileContent(String storageFileName) {
    return downloadableContentRepository.findBy(storageFileName).orElseThrow(() -> {
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
