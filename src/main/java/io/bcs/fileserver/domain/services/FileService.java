package io.bcs.fileserver.domain.services;

import io.bce.Generator;
import io.bce.domain.EventBus;
import io.bce.domain.EventPublisher;
import io.bce.domain.EventType;
import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bce.text.TextTemplates;
import io.bce.validation.ValidationService;
import io.bce.validation.ValidationState;
import io.bcs.fileserver.domain.Constants;
import io.bcs.fileserver.domain.errors.FileNotExistsException;
import io.bcs.fileserver.domain.errors.PrimaryValidationException;
import io.bcs.fileserver.domain.model.file.File;
import io.bcs.fileserver.domain.model.file.File.CreationData;
import io.bcs.fileserver.domain.model.file.FileContentLocator;
import io.bcs.fileserver.domain.model.file.FileHasBeenCreated;
import io.bcs.fileserver.domain.model.file.FileHasBeenDisposed;
import io.bcs.fileserver.domain.model.file.FileRepository;
import io.bcs.fileserver.domain.model.storage.ContentLocator;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This class implements the service, managing files.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class FileService {
  private static final ApplicationLogger log = Loggers.applicationLogger(FileService.class);

  private final ValidationService validationService;
  private final FileRepository fileRepository;
  private final Generator<String> fileNameGenerator;
  private final EventBus eventBus;

  /**
   * Create a file.
   *
   * @param command The file creation command
   * @return The file creation result promise
   */
  public Promise<String> createFile(CreateFile command) {
    EventPublisher<FileHasBeenCreated> eventPublisher =
        createPublisher(FileHasBeenCreated.EVENT_TYPE);
    return Promises.of(deferred -> {
      log.info("Use-case: Create file.");
      checkThatCommandIsValid(command);
      FileCreationData fileCreationData = new FileCreationData(command);
      File file = new File(fileNameGenerator, fileCreationData);
      log.debug(TextTemplates.createBy("The file {{storageFileName}} has been successfully created")
          .withParameter("storageFileName", file.getStorageFileName()));
      fileRepository.save(file);
      eventPublisher
          .publish(new FileHasBeenCreated(file.getStorageFileName(), file.getMediaType()));
      deferred.resolve(file.getStorageFileName());
    });
  }

  /**
   * Dispose file.
   *
   * @param storageFileName The storage file name
   * @return The file dispose complete promise
   */
  public Promise<Void> disposeFile(String storageFileName) {
    EventPublisher<FileHasBeenDisposed> eventPublisher =
        createPublisher(FileHasBeenDisposed.EVENT_TYPE);
    return Promises.of(deferred -> {
      log.info("Use-case: Dispose file.");
      File file = retrieveExistingFileDescriptor(storageFileName);
      ContentLocator contentLocator = new FileContentLocator(file);
      file.dispose();
      log.debug(TextTemplates
          .createBy("The file {{storageFileName}} has been successfully disposed "
              + "from the storage {{storageName}}")
          .withParameter("storageFileName", contentLocator.getStorageFileName())
          .withParameter("storageName", contentLocator.getStorageName()));
      fileRepository.save(file);
      eventPublisher.publish(new FileHasBeenDisposed(contentLocator));
      deferred.resolve(null);
    });
  }

  private File retrieveExistingFileDescriptor(String storageFileName) {
    return fileRepository.findById(storageFileName).orElseThrow(() -> {
      log.warn(TextTemplates.createBy("The file with {{storageFileName}} hasn't been found.")
          .withParameter("storageFileName", storageFileName));
      return new FileNotExistsException();
    });
  }

  private <C> void checkThatCommandIsValid(C command) {
    ValidationState validationState = validationService.validate(command);
    if (!validationState.isValid()) {
      log.warn(TextTemplates
          .createBy("The {{command}} command is invalid. Validation state: {{validationState}}")
          .withParameter("command", command).withParameter("validationState", validationState));
      throw new PrimaryValidationException(validationState);
    }
  }

  private <E> EventPublisher<E> createPublisher(EventType<E> eventType) {
    return eventBus.getPublisher(Constants.CONTEXT, eventType);
  }

  /**
   * This interface describes a file entity creation command.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public interface CreateFile {
    Optional<String> getMediaType();

    Optional<String> getFileName();
  }

  @Getter
  private class FileCreationData implements CreationData {
    private static final String DEFAULT_MEDIA_TYPE = "application/octet-stream";
    private final String mediaType;
    private final Optional<String> fileName;

    public FileCreationData(CreateFile createCommand) {
      this.mediaType = createCommand.getMediaType().orElse(DEFAULT_MEDIA_TYPE);
      this.fileName = createCommand.getFileName();
    }
  }
}
