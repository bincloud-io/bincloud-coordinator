package io.bcs.domain.model.file


import static io.bcs.domain.model.file.FileStatus.DISPOSED
import static io.bcs.domain.model.file.FileStatus.DISTRIBUTING
import static io.bcs.domain.model.file.FileStatus.DRAFT


import io.bce.interaction.streaming.Destination
import io.bce.interaction.streaming.Source
import io.bce.interaction.streaming.Stream
import io.bce.interaction.streaming.Stream.Stat
import io.bce.interaction.streaming.Streamer
import io.bce.interaction.streaming.binary.BinaryChunk
import io.bce.promises.Promise
import io.bce.promises.Promises
import io.bce.promises.WaitingPromise
import io.bce.promises.Promise.ErrorHandler
import io.bce.promises.Promise.ResponseHandler
import io.bcs.domain.model.Constants
import io.bcs.domain.model.ContentLocator
import io.bcs.domain.model.FileStorage
import io.bcs.domain.model.FileStorageException
import io.bcs.domain.model.file.File.CreateFile
import io.bcs.domain.model.file.Lifecycle.FileUploadStatistic
import io.bcs.domain.model.file.states.FileHasBeenDisposedException
import io.bcs.domain.model.file.states.FileHasBeenUploadedException
import spock.lang.Specification

class FileSpec extends Specification {
    public static final String STORAGE_NAME = "storage.0001"
    public static final String STORAGE_FILE_NAME = "instance--${Thread.currentThread()}--${UUID.randomUUID()}"
    public static final String MEDIA_TYPE = "application/media-type-xxx"
    public static final String FILE_NAME = "file.txt"
    public static final Long DEFAULT_CONTENT_LENGTH = 0L
    public static final Long DISTRIBUTIONING_CONTENT_LENGTH = 100L

    private FileStorage fileStorage;

    def setup() {
        this.fileStorage = Stub(FileStorage)
    }

    def "Scenario: create file by default constructor"() {
        when: "The file is created by default constructor"
        File file = new File();
        ContentLocator fileContentLocator = file.getLocator();
        FileMetadata fileMetadata = file.getFileMetadata()

        then: "The storage file name should be ${File.DEFAULT_STORAGE_FILE_NAME}"
        fileContentLocator.getStorageFileName() == File.DEFAULT_STORAGE_FILE_NAME

        and: "The storage name should be ${File.DEFAULT_STORAGE_NAME}"
        fileContentLocator.getStorageName() == File.DEFAULT_STORAGE_NAME

        and: "The file name should be ${File.DEFAULT_FILE_NAME}"
        fileMetadata.getFileName() == File.DEFAULT_FILE_NAME

        and: "The file media type should be ${File.DEFAULT_MEDIA_TYPE}"
        fileMetadata.getMediaType() == File.DEFAULT_MEDIA_TYPE

        and: "The file status should be ${FileStatus.DRAFT}"
        fileMetadata.getStatus() == FileStatus.DRAFT

        and: "The file content length should be 0 bytes"
        fileMetadata.getTotalLength() == 0L
    }

    def "Scenario: successfully create new file"() {
        File file
        given: """The file storage creates the file, located: {storageName: ${STORAGE_NAME}, storageFileName: ${STORAGE_FILE_NAME}} for ${MEDIA_TYPE}"""
        this.fileStorage.create(MEDIA_TYPE) >> contentLocator()

        and: "The file creation command with media type ${MEDIA_TYPE}"
        CreateFile command = createFileCommand()

        and: "The file create response handler"
        ResponseHandler responseHandler = Mock(ResponseHandler)

        when: "The file is created"
        WaitingPromise.of(File.create(fileStorage, command)).then(responseHandler).await()

        then: "The response handler shoudl accept resolved file"
        1 * responseHandler.onResponse(_) >> {file = it[0]}
        ContentLocator fileContentLocator = file.getLocator();
        FileMetadata fileMetadata = file.getFileMetadata()

        and: "The storage file name should be ${STORAGE_FILE_NAME}"
        fileContentLocator.getStorageFileName() == STORAGE_FILE_NAME

        and: "The storage name should be ${STORAGE_NAME}"
        fileContentLocator.getStorageName() == STORAGE_NAME

        and: "The file name should be ${FILE_NAME}"
        fileMetadata.getFileName() == FILE_NAME

        and: "The file media type should be ${MEDIA_TYPE}"
        fileMetadata.getMediaType() == MEDIA_TYPE

        and: "The file status should be ${FileStatus.DRAFT}"
        fileMetadata.getStatus() == FileStatus.DRAFT

        and: "The file content length should be 0 bytes"
        fileMetadata.getTotalLength() == 0L
    }
    
    def "Scenario: create new file with file storage error"() {
        FileStorageException fileStorageException
        given: """The file storage creates the file, located: {storageName: ${STORAGE_NAME}, storageFileName: ${STORAGE_FILE_NAME}} for ${MEDIA_TYPE}"""
        this.fileStorage.create(MEDIA_TYPE) >> {throw new FileStorageException(new RuntimeException())}

        and: "The file creation command with media type ${MEDIA_TYPE}"
        CreateFile command = createFileCommand()

        and: "The promise reject error handler"
        ErrorHandler errorHandler = Mock(ErrorHandler)

        when: "The file is created"
        WaitingPromise.of(File.create(fileStorage, command)).error(errorHandler).await()
        
        then: "The file storage error should be happened"
        1 * errorHandler.onError(_) >> {fileStorageException = it[0]}
        fileStorageException.getContextId() == Constants.CONTEXT
        fileStorageException.getErrorCode() == Constants.FILE_STORAGE_ERROR
    }

    def "Scenario: dispose draft file"() {
        given: "The file in draft state"
        File file = createDraftFile()

        and: "The promise resolve response handler"
        ResponseHandler responseHandler = Mock(ResponseHandler)

        when: "The file is disposed"
        WaitingPromise.of(disposeFile(file)).then(responseHandler).await()
        FileMetadata fileMetadata = file.getFileMetadata()

        then: "The response handler should be resolved"
        1 * responseHandler.onResponse(_)

        and: "The file status should be changed to the disposed"
        fileMetadata.getStatus() == FileStatus.DISPOSED

        and: "The content length should be set to zero"
        fileMetadata.getTotalLength() == 0L
    }


    def "Scenario: dispose distributioning file"() {
        given: "The file in distribution state"
        File file = createDistributionFile()

        and: "The promise resolve response handler"
        ResponseHandler responseHandler = Mock(ResponseHandler)

        when: "The file is disposed"
        WaitingPromise.of(disposeFile(file)).then(responseHandler).await()
        FileMetadata fileMetadata = file.getFileMetadata()

        then: "The response handler should be resolved"
        1 * responseHandler.onResponse(_)

        and: "The file status should be changed to the disposed"
        fileMetadata.getStatus() == FileStatus.DISPOSED

        and: "The content length should be set to zero"
        fileMetadata.getTotalLength() == 0L
    }

    def "Scenario: dispose disposed file"() {
        FileHasBeenDisposedException error
        given: "The file in disposed state"
        File file = createDisposedFile()

        and: "The promise reject error handler"
        ErrorHandler errorHandler = Mock(ErrorHandler)

        when: "The file is disposed"
        WaitingPromise.of(disposeFile(file)).error(errorHandler).await()

        then: "The file has already been exception should be happened"
        1 * errorHandler.onError(_) >> {error = it[0]}
        error.getContextId() == Constants.CONTEXT
        error.getErrorCode() == Constants.FILE_IS_DISPOSED_ERROR
    }

    def "Scenario: successfully upload file content to file in the draft state"() {
        FileUploadStatistic uploadStatistic
        
        given: "The file in draft state"
        File file = createDraftFile()

        and: "The content source"
        Source<BinaryChunk> source = Mock(Source)

        and: "The data streamer"
        Streamer streamer = Mock(Streamer)
        Destination<BinaryChunk> destination = Mock(Destination)
        fileStorage.getAccessOnWrite(_) >> destination
        Stream<BinaryChunk> stream = Mock(Stream)
        streamer.createStream(source, destination) >> stream
        stream.start() >> Promises.resolvedBy(createStat(DISTRIBUTIONING_CONTENT_LENGTH))
        
        and: "The promise response handler"
        ResponseHandler<FileUploadStatistic> responseHandler = Mock(ResponseHandler)
        
        when: "The file is uploaded"
        WaitingPromise.of(uploadFile(file, streamer, source)).then(responseHandler).await()
        FileMetadata fileMetadata = file.getFileMetadata()
        ContentLocator fileLocator = file.getLocator()
        
        then: "The file upload should be completed successfully"
        1 * responseHandler.onResponse(_) >> {uploadStatistic = it[0]}
        ContentLocator statisticLocator = uploadStatistic.getLocator()
        statisticLocator.getStorageFileName() == fileLocator.getStorageFileName()
        statisticLocator.getStorageName() == fileLocator.getStorageName()
        uploadStatistic.getContentLength() == DISTRIBUTIONING_CONTENT_LENGTH
        
        and: "The content length should be updated"
        fileMetadata.getTotalLength() == DISTRIBUTIONING_CONTENT_LENGTH
    }

    def "Scenario: upload file content with error to file in the draft state"() {
        FileStorageException fileStorageException
        
        given: "The file in draft state"
        File file = createDraftFile()

        and: "The content source"
        Source<BinaryChunk> source = Mock(Source)

        and: "The data streamer"
        Streamer streamer = Mock(Streamer)
        Destination<BinaryChunk> destination = Mock(Destination)
        fileStorage.getAccessOnWrite(_) >> {throw new FileStorageException(new RuntimeException())}
            
        and: "The promise reject error handler"
        ErrorHandler errorHandler = Mock(ErrorHandler)
        
        when: "The file is uploaded"
        WaitingPromise.of(uploadFile(file, streamer, source)).error(errorHandler).await()
        FileMetadata fileMetadata = file.getFileMetadata()
        ContentLocator fileLocator = file.getLocator()
        
        then: "The file storage error should be happened"
        1 * errorHandler.onError(_) >> {fileStorageException = it[0]}
        fileStorageException.getContextId() == Constants.CONTEXT
        fileStorageException.getErrorCode() == Constants.FILE_STORAGE_ERROR
        
        and: "The content length should not be updated"
        fileMetadata.getTotalLength() == DEFAULT_CONTENT_LENGTH
    }

    def "Scenario: upload file content to file in the distributioning state"() {
        FileHasBeenUploadedException error
        given: "The file in distribution state"
        File file = createDistributionFile()

        and: "The content source"
        Source<BinaryChunk> source = Mock(Source)

        and: "The data streamer"
        Streamer streamer = Mock(Streamer)

        and: "The promise reject error handler"
        ErrorHandler errorHandler = Mock(ErrorHandler)

        when: "The file is uploaded"
        WaitingPromise.of(uploadFile(file, streamer, source)).error(errorHandler).await()

        then: "The file has already been exception should be happened"
        1 * errorHandler.onError(_) >> {error = it[0]}
        error.getContextId() == Constants.CONTEXT
        error.getErrorCode() == Constants.FILE_HAS_BEEN_UPLOADED_ERROR
    }

    def "Scenario: upload file content to file in the disposed state"() {
        FileHasBeenDisposedException error
        given: "The file in disposed state"
        File file = createDisposedFile()

        and: "The content source"
        Source<BinaryChunk> source = Mock(Source)

        and: "The data streamer"
        Streamer streamer = Mock(Streamer)

        and: "The promise reject error handler"
        ErrorHandler errorHandler = Mock(ErrorHandler)

        when: "The file is uploaded"
        WaitingPromise.of(uploadFile(file, streamer, source)).error(errorHandler).await()

        then: "The file has already been exception should be happened"
        1 * errorHandler.onError(_) >> {error = it[0]}
        error.getContextId() == Constants.CONTEXT
        error.getErrorCode() == Constants.FILE_IS_DISPOSED_ERROR
    }
    
    

    private File createDraftFile() {
        return createFile(DRAFT, 0L)
    }

    private File createDistributionFile() {
        return createFile(DISTRIBUTING, DISTRIBUTIONING_CONTENT_LENGTH)
    }

    private File createDisposedFile() {
        return createFile(DISPOSED, 0L)
    }

    private Promise<FileUploadStatistic> uploadFile(File file, Streamer streamer, Source<BinaryChunk> contentSource) {
        return file.getLifecycle(fileStorage).upload(streamer, contentSource).execute()
    }

    private Promise<Void> disposeFile(File file) {
        return file.getLifecycle(fileStorage).dispose().execute()
    }

    private Stat createStat(Long contentSize) {
        return new Stat() {
                    @Override
                    public Long getSize() {
                        return contentSize;
                    }
                }
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

    private CreateFile createFileCommand() {
        CreateFile command = Stub(CreateFile)
        command.getMediaType() >> MEDIA_TYPE
        command.getFileName() >> FILE_NAME
        return command
    }

    private ContentLocator contentLocator() {
        ContentLocator contentLocator = Stub(ContentLocator)
        contentLocator.getStorageName() >> STORAGE_NAME
        contentLocator.getStorageFileName() >> STORAGE_FILE_NAME
        return contentLocator
    }
}
