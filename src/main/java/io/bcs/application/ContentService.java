package io.bcs.application;

import java.util.Optional;
import java.util.function.Supplier;

import io.bce.domain.usecases.RequestReplyUseCase;
import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bcs.domain.model.Constants;
import io.bcs.domain.model.file.ContentReceiver;
import io.bcs.domain.model.file.ContentUploader;
import io.bcs.domain.model.file.File;
import io.bcs.domain.model.file.FileNotSpecifiedException;
import io.bcs.domain.model.file.FileRepository;
import io.bcs.domain.model.file.FileStorage;
import io.bcs.domain.model.file.Lifecycle.FileUploadStatistic;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ContentService {
    private static final ApplicationLogger log = Loggers.applicationLogger(ContentService.class);

    private final FileRepository fileRepository;
    private final FileStorage fileStorage;

    public RequestReplyUseCase<Optional<String>, Promise<FileUploadStatistic>> upload(ContentUploader uploader) {
        return storageFileName -> Promises.of(deferred -> {
            log.info("Use-case: Download file.");
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

    private File retrieveExistingFile(String storageFileName) {
        Supplier<File> fileProvider = new FileProvider(storageFileName, fileRepository, log);
        return fileProvider.get();
    }

    private static String extractStorageFileName(Optional<String> storageFileName) {
        return storageFileName.orElseThrow(() -> {
            log.warn("Storage file name has not been specified");
            return new FileNotSpecifiedException(Constants.FILE_IS_NOT_SPECIFIED);
        });
    }
}
