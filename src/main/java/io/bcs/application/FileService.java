package io.bcs.application;

import java.util.function.Supplier;

import io.bce.domain.usecases.RequestReplyUseCase;
import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bce.text.TextTemplates;
import io.bce.validation.ValidationService;
import io.bce.validation.ValidationState;
import io.bcs.domain.model.PrimaryValidationException;
import io.bcs.domain.model.file.ContentLocator;
import io.bcs.domain.model.file.File;
import io.bcs.domain.model.file.File.CreateFile;
import io.bcs.domain.model.file.FileRepository;
import io.bcs.domain.model.file.FileStorage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileService {
  private static final ApplicationLogger log = Loggers.applicationLogger(FileService.class);

  private final ValidationService validationService;
  private final FileRepository fileRepository;
  private final FileStorage fileStorage;

  public RequestReplyUseCase<CreateFile, Promise<String>> createFile() {
    return command -> Promises.of(deferred -> {
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

  public RequestReplyUseCase<String, Promise<Void>> disposeFile() {
    return storageFileName -> Promises.of(deferred -> {
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
