package io.bcs.fileserver.domain.services;

import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bce.text.TextTemplates;
import io.bce.validation.ValidationService;
import io.bce.validation.ValidationState;
import io.bcs.fileserver.domain.errors.PrimaryValidationException;
import io.bcs.fileserver.domain.model.file.File;
import io.bcs.fileserver.domain.model.file.File.CreateFile;
import io.bcs.fileserver.domain.model.file.FileManagement;
import io.bcs.fileserver.domain.model.file.FileRepository;
import io.bcs.fileserver.domain.model.storage.ContentLocator;
import io.bcs.fileserver.domain.model.storage.FileStorage;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

/**
 * This class implements the service, managing files.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class FileService implements FileManagement {
  private static final ApplicationLogger log = Loggers.applicationLogger(FileService.class);

  private final ValidationService validationService;
  private final FileRepository fileRepository;
  private final FileStorage fileStorage;

  /**
   * Create a file.
   *
   * @param command The file creation command
   * @return The file creation result promise
   */
  @Override
  public Promise<String> createFile(CreateFile command) {
    return Promises.of(deferred -> {
      log.info("Use-case: Create file.");
      checkThatCommandIsValid(command);
      File.create(fileStorage, command).chain(file -> {
        ContentLocator locator = file.getLocator();
        log.debug(TextTemplates
            .createBy("The file {{locator}} has been successfully created in the storage")
            .withParameter("locator", locator));
        fileRepository.save(file);
        return Promises.resolvedBy(locator.getStorageFileName());
      }).delegate(deferred);
    });
  }

  /**
   * Dispose file.
   *
   * @param storageFileName The storage file name
   * @return The file dispose complete promise
   */
  @Override
  public Promise<Void> disposeFile(String storageFileName) {
    return Promises.of(deferred -> {
      log.info("Use-case: Dispose file.");
      File file = retrieveExistingFile(storageFileName);
      file.getLifecycle(fileStorage).dispose().execute().chain(v -> {
        log.debug(TextTemplates
            .createBy("The file {{locator}} has been successfully disposed from the storage")
            .withParameter("locator", file.getLocator()));
        fileRepository.save(file);
        return (Promise<Void>) Promises.<Void>resolvedBy(null);
      }).delegate(deferred);
    });
  }

  private File retrieveExistingFile(String storageFileName) {
    Supplier<File> fileProvider = new FileProvider(storageFileName, fileRepository, log);
    return fileProvider.get();
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
}
