package io.bcs.fileserver.domain.services

import static io.bcs.fileserver.domain.model.file.state.FileStatus.DISTRIBUTING
import static io.bcs.fileserver.domain.model.file.state.FileStatus.DRAFT

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
import io.bcs.fileserver.domain.Constants
import io.bcs.fileserver.domain.errors.FileNotExistsException
import io.bcs.fileserver.domain.errors.FileNotSpecifiedException
import io.bcs.fileserver.domain.errors.PrimaryValidationException
import io.bcs.fileserver.domain.model.file.File
import io.bcs.fileserver.domain.model.file.FileManagement
import io.bcs.fileserver.domain.model.file.FileRepository
import io.bcs.fileserver.domain.model.file.File.CreateFile
import io.bcs.fileserver.domain.model.file.content.ContentReceiver
import io.bcs.fileserver.domain.model.file.content.ContentUploader
import io.bcs.fileserver.domain.model.file.lifecycle.Lifecycle.FileUploadStatistic
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


  private ValidationService validationService
  private FileRepository fileRepository
  private FileStorage fileStorage
  private FileManagement fileService

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
    WaitingPromise.of(fileService.createFile(command)).then(responseHandler).await()

    then: "The draft file should be stored into repository"
    1 * fileRepository.save(_) >> {file = it[0]}
    file.getLocator().getStorageFileName() == STORAGE_FILE_NAME
    file.getLocator().getStorageName() == STORAGE_NAME
    file.getStatus() == FileStatus.DRAFT
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
    WaitingPromise.of(fileService.createFile(command)).error(errorHandler).await()

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
    WaitingPromise.of(fileService.disposeFile(STORAGE_FILE_NAME)).then(responseHandler).await(100)

    then: "The file should be disposed and stored into repository"
    1 * fileRepository.save(_) >> {file = it[0]}
    file.getLocator().getStorageFileName() == STORAGE_FILE_NAME
    file.getLocator().getStorageName() == STORAGE_NAME
    file.getStatus() == FileStatus.DISPOSED

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
    WaitingPromise.of(fileService.disposeFile(STORAGE_FILE_NAME)).error(errorHandler).await()

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
