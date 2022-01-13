package io.bcs.fileserver.domain.services;

import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bce.text.TextTemplates;
import io.bcs.fileserver.domain.errors.FileNotExistsException;
import io.bcs.fileserver.domain.errors.FileNotSpecifiedException;
import io.bcs.fileserver.domain.model.file.File;
import io.bcs.fileserver.domain.model.file.FileRepository;
import io.bcs.fileserver.domain.model.file.Range;
import io.bcs.fileserver.domain.model.file.content.ContentDownloader;
import io.bcs.fileserver.domain.model.file.content.ContentDownloader.ContentReceiver;
import io.bcs.fileserver.domain.model.file.content.ContentUploader;
import io.bcs.fileserver.domain.model.file.content.ContentUploader.ContentSource;
import io.bcs.fileserver.domain.model.file.content.FileUploadStatistic;
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

  /**
   * Upload file content.
   *
   * @param storageFileName The storage file name
   * @param contentSender   The file content sender
   * @return The file upload statistic promise
   */
  public Promise<FileUploadStatistic> upload(Optional<String> storageFileName,
      ContentSource contentSender) {
    return Promises.of(deferred -> {
      log.info("Use-case: Download file.");
      File file = retrieveExistingFile(extractStorageFileName(storageFileName));
      file.uploadContent(new ContentUploader(fileStorage, contentSender)).chain(statistic -> {
        fileRepository.save(file);
        return Promises.resolvedBy(statistic);
      }).delegate(deferred);
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
    return Promises.of(deferred -> {
      File file = retrieveExistingFile(extractStorageFileName(command.getStorageFileName()));
      file.downloadContent(new ContentDownloader(fileStorage, contentReceiver), command.getRanges())
          .delegate(deferred);
    });
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
    Optional<String> getStorageFileName();

    Collection<Range> getRanges();
  }
}
