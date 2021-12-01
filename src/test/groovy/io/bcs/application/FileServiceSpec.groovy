package io.bcs.application

import static io.bcs.domain.model.file.FileStatus.DISTRIBUTING
import static io.bcs.domain.model.file.FileStatus.DRAFT

import io.bce.domain.errors.ErrorDescriptor.ErrorSeverity
import io.bce.interaction.streaming.Destination
import io.bce.interaction.streaming.Source
import io.bce.promises.Promises
import io.bce.promises.WaitingPromise
import io.bce.promises.Promise.ErrorHandler
import io.bce.promises.Promise.ResponseHandler
import io.bce.validation.ErrorMessage
import io.bce.validation.ValidationService
import io.bce.validation.ValidationState
import io.bcs.domain.model.Constants
import io.bcs.domain.model.PrimaryValidationException
import io.bcs.domain.model.file.ContentLocator
import io.bcs.domain.model.file.ContentReceiver
import io.bcs.domain.model.file.ContentUploader
import io.bcs.domain.model.file.File
import io.bcs.domain.model.file.FileNotExistsException
import io.bcs.domain.model.file.FileRepository
import io.bcs.domain.model.file.FileStatus
import io.bcs.domain.model.file.FileStorage
import io.bcs.domain.model.file.File.CreateFile
import io.bcs.domain.model.file.Lifecycle.FileUploadStatistic
import io.bcs.domain.model.file.FileNotSpecifiedException
import spock.lang.Specification

class FileServiceSpec extends Specification {
    public static final String STORAGE_NAME = "storage.0001"
    public static final String STORAGE_FILE_NAME = "instance--${Thread.currentThread()}--${UUID.randomUUID()}"
    public static final String MEDIA_TYPE = "application/media-type-xxx"
    public static final String FILE_NAME = "file.txt"
    public static final Long DEFAULT_CONTENT_LENGTH = 0L
    public static final Long DISTRIBUTIONING_CONTENT_LENGTH = 100L


    private ValidationService validationService
    private FileRepository fileRepository
    private FileStorage fileStorage
    private FileService fileService

    def setup() {
        this.validationService = Mock(ValidationService)
        this.fileRepository = Mock(FileRepository)
        this.fileStorage = Mock(FileStorage)
        this.fileService = new FileService(validationService, fileRepository, fileStorage)
    }

    def "Scenario: create new file"() {
        File file
        given: "The create file command"
        CreateFile command = createFileCommand(MEDIA_TYPE, FILE_NAME)

        and: "The command passes validation"
        this.validationService.validate(command) >> new ValidationState()

        and: "The file storage is going to create file successfully"
        this.fileStorage.create(MEDIA_TYPE) >> contentLocator()

        and: "The response handler"
        ResponseHandler responseHandler = Mock(ResponseHandler)

        when: "The file is created"
        WaitingPromise.of(fileService.createFile().execute(command)).then(responseHandler).await()

        then: "The draft file should be stored into repository"
        1 * fileRepository.save(_) >> {file = it[0]}
        file.getLocator().getStorageFileName() == STORAGE_FILE_NAME
        file.getLocator().getStorageName() == STORAGE_NAME
        file.getFileMetadata().getStatus() == FileStatus.DRAFT
    }

    def "Scenario: create new file with invalid request"() {
        PrimaryValidationException error
        given: "The create file command"
        CreateFile command = createFileCommand(MEDIA_TYPE, FILE_NAME)

        and: "The command passes validation"
        this.validationService.validate(command) >> new ValidationState().withUngrouped(ErrorMessage.createFor("Smth went wrong"))

        and: "The file storage is going to create file successfully"
        this.fileStorage.create(MEDIA_TYPE) >> contentLocator()

        and: "The error handler"
        ErrorHandler errorHandler = Mock(ErrorHandler)

        when: "The file is created"
        WaitingPromise.of(fileService.createFile().execute(command)).error(errorHandler).await()

        then: "Nothing should be stored into repository"
        0 * fileRepository.save(_)

        and: "The primary validation error should be happened"
        1 * errorHandler.onError(_) >> {error = it[0]}
        error.getContextId() == Constants.CONTEXT
        error.getErrorCode() == Constants.PRIMARY_VALIDATION_ERROR
        error.getErrorSeverity() == ErrorSeverity.BUSINESS
    }

    def "Scenario: dispose existing file"() {
        File file
        ContentLocator removedFileLocator
        given: "The distributing file, existing into repository"
        this.fileRepository.findById(_) >> Optional.of(createFile(FileStatus.DISTRIBUTING, DISTRIBUTIONING_CONTENT_LENGTH))

        and: "The response handler"
        ResponseHandler responseHandler = Mock(ResponseHandler)

        when: "The file is disposed"
        WaitingPromise.of(fileService.disposeFile().execute(STORAGE_FILE_NAME)).then(responseHandler).await(100)

        then: "The file should be disposed and stored into repository"
        1 * fileRepository.save(_) >> {file = it[0]}
        file.getLocator().getStorageFileName() == STORAGE_FILE_NAME
        file.getLocator().getStorageName() == STORAGE_NAME
        file.getFileMetadata().getStatus() == FileStatus.DISPOSED

        and: "The file content should be removed from file storage"
        1 * this.fileStorage.delete(_) >> {removedFileLocator = it[0]}
        removedFileLocator.getStorageFileName() == STORAGE_FILE_NAME
        removedFileLocator.getStorageName() == STORAGE_NAME
    }

    def "Scenario: dispose unknown file"() {
        FileNotExistsException error
        given: "The distributing file, existing into repository"
        this.fileRepository.findById(STORAGE_FILE_NAME) >> Optional.empty()

        and: "The error handler"
        ErrorHandler errorHandler = Mock(ErrorHandler)

        when: "The file is disposed"
        WaitingPromise.of(fileService.disposeFile().execute(STORAGE_FILE_NAME)).error(errorHandler).await()

        then: "The file not exists error should be happened"
        1 * errorHandler.onError(_) >> {error = it[0]}
        error.getContextId() == Constants.CONTEXT
        error.getErrorSeverity() == ErrorSeverity.BUSINESS
        error.getErrorCode() == Constants.FILE_NOT_EXIST_ERROR
    }

    def "Scenario: upload content for unknown file"() {
        FileNotSpecifiedException error
        given: "The file uploader"
        ContentUploader contentUploader = Mock(ContentUploader)
        
        and: "The error handler"
        ErrorHandler errorHandler = Mock(ErrorHandler)

        
        when: "The file is uploaded for unspecified file storage name"
        WaitingPromise.of(fileService.upload(contentUploader).execute(Optional.empty())).error(errorHandler).await()
        
        then: "The file is not specified error should be happened"
        1 * errorHandler.onError(_) >> {error = it[0]}
        error.getContextId() == Constants.CONTEXT
        error.getErrorSeverity() == ErrorSeverity.BUSINESS
        error.getErrorCode() == Constants.FILE_IS_NOT_SPECIFIED
    }
    
    def "Scenario: upload content to existing file"() {
        File file
        FileUploadStatistic statistic
        given: "The draft file"
        this.fileRepository.findById(STORAGE_FILE_NAME) >> Optional.of(createFile(DRAFT, DEFAULT_CONTENT_LENGTH))

        and: "The file uploader"
        ContentUploader contentUploader = Mock(ContentUploader)
        contentUploader.upload(_, _) >> Promises.resolvedBy(fileUploadStatistic())

        and: "The file storage is going get access on write"
        fileStorage.getAccessOnWrite(_) >> Stub(Destination)

        and: "The response handler"
        ResponseHandler responseHandler = Mock(ResponseHandler)

        when: "The file is uploaded"
        WaitingPromise.of(fileService.upload(contentUploader).execute(Optional.ofNullable(STORAGE_FILE_NAME))).then(responseHandler).await()

        then: "The file should be stored in the distributing state"
        1 * fileRepository.save(_) >> {file = it[0]}
        file.getLocator().getStorageFileName() == STORAGE_FILE_NAME
        file.getLocator().getStorageName() == STORAGE_NAME
        file.getFileMetadata().getTotalLength() == DISTRIBUTIONING_CONTENT_LENGTH

        and: "The response handler should be resolved"
        1 * responseHandler.onResponse(_) >> {statistic = it[0]}
        statistic.getLocator().getStorageFileName() == STORAGE_FILE_NAME
        statistic.getLocator().getStorageName() == STORAGE_NAME
        statistic.getContentLength() ==  DISTRIBUTIONING_CONTENT_LENGTH
    }

    def "Scenario: upload content to unknown file"() {
        FileNotExistsException error
        given: "The draft file, missing into repository"
        this.fileRepository.findById(STORAGE_FILE_NAME) >> Optional.empty()

        and: "The content uploader"
        ContentUploader contentUploader = Mock(ContentUploader)

        and: "The error handler"
        ErrorHandler errorHandler = Mock(ErrorHandler)

        when: "The file is uploaded"
        WaitingPromise.of(fileService.upload(contentUploader).execute(Optional.ofNullable(STORAGE_FILE_NAME))).error(errorHandler).await()

        then: "The file not exists error should be happened"
        1 * errorHandler.onError(_) >> {error = it[0]}
        error.getContextId() == Constants.CONTEXT
        error.getErrorSeverity() == ErrorSeverity.BUSINESS
        error.getErrorCode() == Constants.FILE_NOT_EXIST_ERROR
    }
    
    def "Scenario: download content from unknown file"() {
        FileNotSpecifiedException error
        given: "The file content receiver"
        ContentReceiver contentReceiver = Mock(ContentReceiver)
        
        and: "The error handler"
        ErrorHandler errorHandler = Mock(ErrorHandler)

        
        when: "The file is uploaded for unspecified file storage name"
        WaitingPromise.of(fileService.download(contentReceiver).execute(downloadCommand(Optional.empty()))).error(errorHandler).await()
        
        then: "The file is not specified error should be happened"
        1 * errorHandler.onError(_) >> {error = it[0]}
        error.getContextId() == Constants.CONTEXT
        error.getErrorSeverity() == ErrorSeverity.BUSINESS
        error.getErrorCode() == Constants.FILE_IS_NOT_SPECIFIED
    }

    def "Scenario: download existing file content"() {
        given: "The distributing file"
        this.fileRepository.findById(STORAGE_FILE_NAME) >> Optional.of(createFile(DISTRIBUTING, DISTRIBUTIONING_CONTENT_LENGTH))

        and: "The content downloader"
        ContentReceiver contentDownloader = Mock(ContentReceiver)


        and: "The file storage is going get access on write"
        fileStorage.getAccessOnRead(_, _) >> Stub(Source)

        and: "The response handler"
        ResponseHandler responseHandler = Mock(ResponseHandler)

        when: "The file is uploaded"
        WaitingPromise.of(fileService.download(contentDownloader).execute(downloadCommand(Optional.of(STORAGE_FILE_NAME)))).then(responseHandler).await()

        then: "The file content downloading should be started"
        1 * contentDownloader.receiveFullContent(_) >> Promises.resolvedBy(null)

        and: "The response handler should be resolved"
        1 * responseHandler.onResponse(_)
    }

    def "Scenario: download unknown file contentn"() {
        FileNotExistsException error
        given: "The distributing file, existing into repository"
        this.fileRepository.findById(STORAGE_FILE_NAME) >> Optional.empty()

        and: "The content downloader"
        ContentReceiver contentDownloader = Mock(ContentReceiver)

        and: "The error handler"
        ErrorHandler errorHandler = Mock(ErrorHandler)

        when: "The file is downloaded"
        WaitingPromise.of(fileService.download(contentDownloader).execute(downloadCommand(Optional.of(STORAGE_FILE_NAME)))).error(errorHandler).await()

        then: "The file not exists error should be happened"
        1 * errorHandler.onError(_) >> {error = it[0]}
        error.getContextId() == Constants.CONTEXT
        error.getErrorSeverity() == ErrorSeverity.BUSINESS
        error.getErrorCode() == Constants.FILE_NOT_EXIST_ERROR
    }

    private File createFile(FileStatus status, Long contentLength) {
        return File.builder()
                .storageName(STORAGE_NAME)
                .storageFileName(STORAGE_FILE_NAME)
                .status(status)
                .mediaType(MEDIA_TYPE)
                .fileName(FILE_NAME)
                .contentLength(contentLength)
                .build()
    }

    private CreateFile createFileCommand(String mediaType, String fileName) {
        CreateFile command = Stub(CreateFile)
        command.getMediaType() >> Optional.ofNullable(mediaType)
        command.getFileName() >> Optional.ofNullable(fileName)
        return command
    }

    private ContentLocator contentLocator() {
        ContentLocator contentLocator = Stub(ContentLocator)
        contentLocator.getStorageName() >> STORAGE_NAME
        contentLocator.getStorageFileName() >> STORAGE_FILE_NAME
        return contentLocator
    }

    private DownloadCommand downloadCommand(Optional<String> storageFileName) {
        return Stub(DownloadCommand) {
            getStorageFileName() >> storageFileName
            getRanges() >> []
        }
    }

    private FileUploadStatistic fileUploadStatistic() {
        return Stub(FileUploadStatistic) {
            getLocator() >> contentLocator()
            getContentLength() >> DISTRIBUTIONING_CONTENT_LENGTH
        }
    }
}
