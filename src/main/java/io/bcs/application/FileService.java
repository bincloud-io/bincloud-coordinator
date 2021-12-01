package io.bcs.application;

import java.util.Optional;

import io.bce.domain.usecases.RequestReplyUseCase;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bce.validation.ValidationService;
import io.bce.validation.ValidationState;
import io.bcs.domain.model.Constants;
import io.bcs.domain.model.PrimaryValidationException;
import io.bcs.domain.model.file.ContentReceiver;
import io.bcs.domain.model.file.ContentLocator;
import io.bcs.domain.model.file.ContentUploader;
import io.bcs.domain.model.file.File;
import io.bcs.domain.model.file.File.CreateFile;
import io.bcs.domain.model.file.FileNotExistsException;
import io.bcs.domain.model.file.FileRepository;
import io.bcs.domain.model.file.FileStorage;
import io.bcs.domain.model.file.FileNotSpecifiedException;
import io.bcs.domain.model.file.Lifecycle.FileUploadStatistic;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileService {
    private final ValidationService validationService;
    private final FileRepository fileRepository;
    private final FileStorage fileStorage;

    public RequestReplyUseCase<CreateFile, Promise<String>> createFile() {
        return command -> Promises.of(deferred -> {
            checkThatCommandIsValid(command);
            File.create(fileStorage, command).chain(file -> {
                ContentLocator locator = file.getLocator();
                fileRepository.save(file);
                return Promises.resolvedBy(locator.getStorageFileName());
            }).delegate(deferred);
        });
    }

    public RequestReplyUseCase<String, Promise<Void>> disposeFile() {
        return storageFileName -> Promises.of(deferred -> {
            File file = retrieveExistingFile(storageFileName);
            file.getLifecycle(fileStorage).dispose().execute().chain(v -> {
                fileRepository.save(file);
                return (Promise<Void>) Promises.<Void>resolvedBy(null);
            }).delegate(deferred);
        });
    }

    public RequestReplyUseCase<Optional<String>, Promise<FileUploadStatistic>> upload(ContentUploader uploader) {
        return storageFileName -> Promises.of(deferred -> {
            File file = retrieveExistingFile(extractStorageFileName(storageFileName));
            file.getLifecycle(fileStorage).upload(uploader).execute().chain(statistic -> {
                fileRepository.save(file);
                return Promises.resolvedBy(statistic);
            }).delegate(deferred);
        });
    }

    public RequestReplyUseCase<DownloadCommand, Promise<Void>> download(ContentReceiver downloader) {
        return command -> Promises.of(deferred -> {
            File file = retrieveExistingFile(extractStorageFileName(command.getStorageFileName()));
            file.downloadContent(fileStorage, downloader, command.getRanges()).delegate(deferred);
        });
    }
    
    private static String extractStorageFileName(Optional<String> storageFileName) {
        return storageFileName
                .orElseThrow(() -> new FileNotSpecifiedException(Constants.FILE_IS_NOT_SPECIFIED));
    }
    
    private File retrieveExistingFile(String storageFileName) {
        return fileRepository.findById(storageFileName).orElseThrow(FileNotExistsException::new);
    }

    private <C> void checkThatCommandIsValid(C command) {
        ValidationState validationState = validationService.validate(command);
        if (!validationState.isValid()) {
            throw new PrimaryValidationException(validationState);
        }
    }
}
