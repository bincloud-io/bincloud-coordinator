package io.bcs.fileserver.domain.services;

import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bcs.fileserver.domain.errors.FileNotSpecifiedException;
import io.bcs.fileserver.domain.model.file.File;
import io.bcs.fileserver.domain.model.file.FileRepository;
import io.bcs.fileserver.domain.model.file.content.ContentManagement;
import io.bcs.fileserver.domain.model.file.content.ContentReceiver;
import io.bcs.fileserver.domain.model.file.content.ContentUploader;
import io.bcs.fileserver.domain.model.file.lifecycle.Lifecycle.FileUploadStatistic;
import io.bcs.fileserver.domain.model.storage.FileStorage;
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
public class ContentService implements ContentManagement {
  private static final ApplicationLogger log = Loggers.applicationLogger(ContentService.class);

  private final FileRepository fileRepository;
  private final FileStorage fileStorage;

  /**
   * Upload file content.
   *
   * @param storageFileName The storage file name
   * @param uploader        The file content uploader
   * @return The file upload statistic promise
   */
  @Override
  public Promise<FileUploadStatistic> upload(Optional<String> storageFileName,
      ContentUploader uploader) {
    return Promises.of(deferred -> {
      log.info("Use-case: Download file.");
      File file = retrieveExistingFile(extractStorageFileName(storageFileName));
      file.getLifecycle(fileStorage).upload(uploader).execute().chain(statistic -> {
        fileRepository.save(file);
        return Promises.resolvedBy(statistic);
      }).delegate(deferred);
    });
  }

  /**
   * Download file content.
   *
   * @param command    The storage command
   * @param downloader The file download command
   * @return The file download completion promise
   */
  @Override
  public Promise<Void> download(DownloadCommand command, ContentReceiver downloader) {
    return Promises.of(deferred -> {
      File file = retrieveExistingFile(extractStorageFileName(command.getStorageFileName()));
      file.downloadContent(fileStorage, downloader, command.getRanges()).delegate(deferred);
    });
  }

  private File retrieveExistingFile(String storageFileName) {
    Supplier<File> fileProvider = new FileProvider(storageFileName, fileRepository, log);
    return fileProvider.get();
  }

  private static String extractStorageFileName(Optional<String> storageFileName) {
    return storageFileName.orElseThrow(() -> {
      log.warn("Storage file name has not been specified");
      return new FileNotSpecifiedException();
    });
  }
}
