package io.bcs.application;

import java.util.Collection;

import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bce.validation.ValidationService;
import io.bce.validation.ValidationState;
import io.bcs.domain.model.ContentLocator;
import io.bcs.domain.model.FileStorage;
import io.bcs.domain.model.PrimaryValidationException;
import io.bcs.domain.model.file.ContentDownloader;
import io.bcs.domain.model.file.ContentUploader;
import io.bcs.domain.model.file.ContentUseCases;
import io.bcs.domain.model.file.File;
import io.bcs.domain.model.file.File.CreateFile;
import io.bcs.domain.model.file.FileRepository;
import io.bcs.domain.model.file.FileUseCases;
import io.bcs.domain.model.file.Lifecycle.FileUploadStatistic;
import io.bcs.domain.model.file.Range;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileService implements FileUseCases, ContentUseCases {
    private final ValidationService validationService;
    private final FileRepository fileRepository;
    private final FileStorage fileStorage;

    @Override
    public Promise<String> createFile(CreateFile command) {
        return Promises.of(deferred -> {
            checkThatCommandIsValid(command);
            File.create(fileStorage, command).chain(file -> {
                ContentLocator locator = file.getLocator();
                fileRepository.save(file);
                return Promises.resolvedBy(locator.getStorageFileName());
            }).delegate(deferred);
        });
    }

    @Override
    public Promise<Void> disposeFile(String storageFileName) {
        return Promises.of(deferred -> {
            File file = retrieveExistingFile(storageFileName);
            file.getLifecycle(fileStorage).dispose().execute().chain(v -> {
                fileRepository.save(file);
                return (Promise<Void>) Promises.<Void>resolvedBy(null);
            }).delegate(deferred);
        });
    }

    @Override
    public Promise<FileUploadStatistic> upload(String storageFileName, ContentUploader uploader) {
        return Promises.of(deferred -> {
            File file = retrieveExistingFile(storageFileName);
            file.getLifecycle(fileStorage).upload(uploader).execute().chain(statistic -> {
                fileRepository.save(file);
                return Promises.resolvedBy(statistic);
            }).delegate(deferred);
        });
    }

    @Override
    public Promise<Void> download(String storageFileName, Collection<Range> ranges, ContentDownloader downloader) {
        return Promises.of(deferred -> {
            File file = retrieveExistingFile(storageFileName);
            file.downloadContent(fileStorage, downloader, ranges).delegate(deferred);
        });
    }

    private File retrieveExistingFile(String storageFileName) {
        return fileRepository.findById(storageFileName).orElseThrow(UnsupportedOperationException::new);
    }

    private <C> void checkThatCommandIsValid(C command) {
        ValidationState validationState = validationService.validate(command);
        if (!validationState.isValid()) {
            throw new PrimaryValidationException(validationState);
        }
    }
}
