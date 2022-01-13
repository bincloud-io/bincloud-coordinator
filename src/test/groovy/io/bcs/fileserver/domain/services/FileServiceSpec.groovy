package io.bcs.fileserver.domain.services

import io.bce.domain.errors.ErrorDescriptor.ErrorSeverity
import io.bce.promises.WaitingPromise
import io.bce.promises.Promise.ErrorHandler
import io.bce.promises.Promise.ResponseHandler
import io.bce.validation.ErrorMessage
import io.bce.validation.ValidationService
import io.bce.validation.ValidationState
import io.bcs.fileserver.domain.Constants
import io.bcs.fileserver.domain.errors.FileDisposedException
import io.bcs.fileserver.domain.errors.FileNotExistsException
import io.bcs.fileserver.domain.errors.PrimaryValidationException
import io.bcs.fileserver.domain.model.file.FileDescriptor
import io.bcs.fileserver.domain.model.file.FileDescriptorRepository
import io.bcs.fileserver.domain.model.file.FileDescriptor.CreateFile
import io.bcs.fileserver.domain.model.file.state.FileStatus
import io.bcs.fileserver.domain.model.storage.ContentLocator
import io.bcs.fileserver.domain.model.storage.FileStorage
import spock.lang.Specification

class FileServiceSpec extends Specification {
  public static final String STORAGE_NAME = "storage.0001"
  public static final String STORAGE_FILE_NAME = "instance--${Thread.currentThread()}--${UUID.randomUUID()}"
  public static final String MEDIA_TYPE = "application/media-type-xxx"
  public static final String FILE_NAME = "file.txt"
  public static final Long DEFAULT_CONTENT_LENGTH = 0L
  public static final Long DISTRIBUTIONING_CONTENT_LENGTH = 100L
  public static final String DEFAULT_MEDIA_TYPE = "application/octet-stream";


  private ValidationService validationService
  private FileDescriptorRepository fileDescriptorRepository;
  private FileStorage fileStorage
  private FileService fileService

  def setup() {
    this.validationService = Mock(ValidationService)
    this.fileStorage = Mock(FileStorage)
    this.fileDescriptorRepository = Mock(FileDescriptorRepository)
    this.fileService = new FileService(validationService, fileStorage, fileDescriptorRepository)
  }

  def "Scenario: successfully create new file"() {
    FileDescriptor fileDescriptor
    given: "The create file command"
    CreateFile command = createFileCommand(MEDIA_TYPE, FILE_NAME)

    and: "The command passes validation"
    this.validationService.validate(command) >> new ValidationState()

    and: "The file storage is going to create file successfully"
    this.fileStorage.create(MEDIA_TYPE) >> contentLocator()

    and: "The response handler"
    ResponseHandler responseHandler = Mock(ResponseHandler)


    when: "The file is created"
    WaitingPromise.of(fileService.createFile(command)).then(responseHandler).await()

    then: "The draft file should be stored into repository"
    1 * fileDescriptorRepository.save(_) >> {fileDescriptor = it[0]}

    and: "The storage file name should be ${STORAGE_FILE_NAME}"
    fileDescriptor.getStorageFileName() == STORAGE_FILE_NAME

    and: "The storage name should be ${STORAGE_NAME}"
    fileDescriptor.getStorageName() == STORAGE_NAME

    and: "The status should be ${FileStatus.DRAFT}"
    fileDescriptor.getStatus() == FileStatus.DRAFT

    and: "The media type should be ${MEDIA_TYPE}"
    fileDescriptor.getMediaType() == MEDIA_TYPE

    and: "The file name should be ${FILE_NAME}"
    fileDescriptor.getFileName() == FILE_NAME

    and: "The total length should be 0"
    fileDescriptor.getTotalLength() == 0L
  }

  def "Scenario: successfully create new file for missing mediatype"() {
    FileDescriptor fileDescriptor
    given: "The create file command"
    CreateFile command = createFileCommand(null, FILE_NAME)

    and: "The command passes validation"
    this.validationService.validate(command) >> new ValidationState()

    and: "The file storage is going to create file successfully"
    this.fileStorage.create(DEFAULT_MEDIA_TYPE) >> contentLocator()

    and: "The response handler"
    ResponseHandler responseHandler = Mock(ResponseHandler)


    when: "The file is created"
    WaitingPromise.of(fileService.createFile(command)).then(responseHandler).await()

    then: "The draft file should be stored into repository"
    1 * fileDescriptorRepository.save(_) >> {fileDescriptor = it[0]}

    and: "The storage file name should be ${STORAGE_FILE_NAME}"
    fileDescriptor.getStorageFileName() == STORAGE_FILE_NAME

    and: "The storage name should be ${STORAGE_NAME}"
    fileDescriptor.getStorageName() == STORAGE_NAME

    and: "The status should be ${FileStatus.DRAFT}"
    fileDescriptor.getStatus() == FileStatus.DRAFT

    and: "The media type should be ${DEFAULT_MEDIA_TYPE}"
    fileDescriptor.getMediaType() == DEFAULT_MEDIA_TYPE

    and: "The file name should be ${FILE_NAME}"
    fileDescriptor.getFileName() == FILE_NAME

    and: "The total length should be 0"
    fileDescriptor.getTotalLength() == 0L
  }

  def "Scenario: successfully create new file for missing file name"() {
    FileDescriptor fileDescriptor
    given: "The create file command"
    CreateFile command = createFileCommand(MEDIA_TYPE, null)

    and: "The command passes validation"
    this.validationService.validate(command) >> new ValidationState()

    and: "The file storage is going to create file successfully"
    this.fileStorage.create(MEDIA_TYPE) >> contentLocator()

    and: "The response handler"
    ResponseHandler responseHandler = Mock(ResponseHandler)


    when: "The file is created"
    WaitingPromise.of(fileService.createFile(command)).then(responseHandler).await()

    then: "The draft file should be stored into repository"
    1 * fileDescriptorRepository.save(_) >> {fileDescriptor = it[0]}

    and: "The storage file name should be ${STORAGE_FILE_NAME}"
    fileDescriptor.getStorageFileName() == STORAGE_FILE_NAME

    and: "The storage name should be ${STORAGE_NAME}"
    fileDescriptor.getStorageName() == STORAGE_NAME

    and: "The status should be ${FileStatus.DRAFT}"
    fileDescriptor.getStatus() == FileStatus.DRAFT

    and: "The media type should be ${MEDIA_TYPE}"
    fileDescriptor.getMediaType() == MEDIA_TYPE

    and: "The file name should be equal to storage file name"
    fileDescriptor.getFileName() == fileDescriptor.getStorageFileName()

    and: "The total length should be 0"
    fileDescriptor.getTotalLength() == 0L
  }

  def "Scenario: create new file for invalid request"() {
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
    WaitingPromise.of(fileService.createFile(command)).error(errorHandler).await()

    then: "Nothing should be stored into repository"
    0 * fileDescriptorRepository.save(_)

    and: "The primary validation error should be happened"
    1 * errorHandler.onError(_) >> {error = it[0]}
    error.getContextId() == Constants.CONTEXT
    error.getErrorCode() == Constants.PRIMARY_VALIDATION_ERROR
    error.getErrorSeverity() == ErrorSeverity.BUSINESS
  }

  def "Scenario: dispose existing file"() {
    FileDescriptor fileDescriptor
    ContentLocator removedFileLocator
    given: "The distributing file, existing into repository"
    this.fileDescriptorRepository.findById(_) >> Optional.of(createFileDescriptor(FileStatus.DISTRIBUTING, DISTRIBUTIONING_CONTENT_LENGTH))

    and: "The response handler"
    ResponseHandler responseHandler = Mock(ResponseHandler)

    when: "The file is disposed"
    WaitingPromise.of(fileService.disposeFile(STORAGE_FILE_NAME)).then(responseHandler).await(100)

    then: "The file should be disposed and stored into repository"
    1 * fileDescriptorRepository.save(_) >> {fileDescriptor = it[0]}

    and: "The storage file name should be ${STORAGE_FILE_NAME}"
    fileDescriptor.getStorageFileName() == STORAGE_FILE_NAME

    and: "The storage name should be ${STORAGE_NAME}"
    fileDescriptor.getStorageName() == STORAGE_NAME

    and: "The status should be ${FileStatus.DRAFT}"
    fileDescriptor.getStatus() == FileStatus.DISPOSED

    and: "The media type should be ${MEDIA_TYPE}"
    fileDescriptor.getMediaType() == MEDIA_TYPE

    and: "The file name should be equal to storage file name"
    fileDescriptor.getFileName() == FILE_NAME

    and: "The total length should be 0"
    fileDescriptor.getTotalLength() == 0L

    and: "The file content should be removed from file storage"
    1 * this.fileStorage.delete(_) >> {removedFileLocator = it[0]}
    removedFileLocator.getStorageFileName() == STORAGE_FILE_NAME
    removedFileLocator.getStorageName() == STORAGE_NAME
  }

  def "Scenario: dispose existing file which had already disposed before"() {
    FileDisposedException error
    given: "The distributing file, existing into repository"
    this.fileDescriptorRepository.findById(_) >> Optional.of(createFileDescriptor(FileStatus.DISPOSED, DISTRIBUTIONING_CONTENT_LENGTH))

    and: "The error handler"
    ErrorHandler errorHandler = Mock(ErrorHandler)

    when: "The file is disposed"
    WaitingPromise.of(fileService.disposeFile(STORAGE_FILE_NAME)).error(errorHandler).await(100)

    then: "The file disposed error should be happened"
    1 * errorHandler.onError(_) >> {error = it[0]}
    error.getContextId() == Constants.CONTEXT
    error.getErrorSeverity() == ErrorSeverity.BUSINESS
    error.getErrorCode() == Constants.FILE_IS_DISPOSED_ERROR
  }

  def "Scenario: dispose unknown file"() {
    FileNotExistsException error
    given: "The distributing file, existing into repository"
    this.fileDescriptorRepository.findById(STORAGE_FILE_NAME) >> Optional.empty()

    and: "The error handler"
    ErrorHandler errorHandler = Mock(ErrorHandler)

    when: "The file is disposed"
    WaitingPromise.of(fileService.disposeFile(STORAGE_FILE_NAME)).error(errorHandler).await()

    then: "The file not exists error should be happened"
    1 * errorHandler.onError(_) >> {error = it[0]}
    error.getContextId() == Constants.CONTEXT
    error.getErrorSeverity() == ErrorSeverity.BUSINESS
    error.getErrorCode() == Constants.FILE_NOT_EXIST_ERROR
  }

  private FileDescriptor createFileDescriptor(FileStatus status, Long contentLength) {
    return FileDescriptor.builder()
        .storageName(STORAGE_NAME)
        .storageFileName(STORAGE_FILE_NAME)
        .status(status)
        .mediaType(MEDIA_TYPE)
        .fileName(FILE_NAME)
        .totalLength(contentLength)
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
}
