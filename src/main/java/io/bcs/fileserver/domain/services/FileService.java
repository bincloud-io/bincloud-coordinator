package io.bcs.fileserver.domain.services;

import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bce.text.TextTemplates;
import io.bce.validation.ValidationService;
import io.bce.validation.ValidationState;
import io.bcs.fileserver.domain.errors.FileNotExistsException;
import io.bcs.fileserver.domain.errors.PrimaryValidationException;
import io.bcs.fileserver.domain.model.file.FileDescriptor;
import io.bcs.fileserver.domain.model.file.FileDescriptor.CreateFile;
import io.bcs.fileserver.domain.model.file.FileDescriptorRepository;
import io.bcs.fileserver.domain.model.file.FileManagement;
import io.bcs.fileserver.domain.model.storage.FileStorage;
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
  private final FileStorage fileStorage;
  private final FileDescriptorRepository fileDescriptorRepository;

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
      FileDescriptor fileDescriptor = new FileDescriptor(fileStorage, command);
      log.debug(TextTemplates
          .createBy("The file {{storageFileName}} has been successfully created in "
              + "the storage {{storageName}}")
          .withParameter("storageFileName", fileDescriptor.getStorageFileName())
          .withParameter("storageName", fileDescriptor.getStorageName()));
      fileDescriptorRepository.save(fileDescriptor);
      deferred.resolve(fileDescriptor.getStorageFileName());
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
      FileDescriptor fileDescriptor = retrieveExistingFileDescriptor(storageFileName);
      log.debug(TextTemplates
          .createBy("The file {{storageFileName}} has been successfully disposed "
              + "from the storage {{storageName}}")
          .withParameter("storageFileName", fileDescriptor.getStorageFileName())
          .withParameter("storageName", fileDescriptor.getStorageName()));
      fileStorage.delete(fileDescriptor.getContentLocator());
      fileDescriptor.dispose();
      fileDescriptorRepository.save(fileDescriptor);
      deferred.resolve(null);
    });
  }

  private FileDescriptor retrieveExistingFileDescriptor(String storageFileName) {
    return fileDescriptorRepository.findById(storageFileName).orElseThrow(() -> {
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
}
