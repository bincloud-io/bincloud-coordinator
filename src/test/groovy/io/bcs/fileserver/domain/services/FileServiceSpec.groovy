package io.bcs.fileserver.domain.services

import io.bce.Generator
import io.bce.domain.EventBus
import io.bce.domain.EventPublisher
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
import io.bcs.fileserver.domain.events.FileHasBeenCreated
import io.bcs.fileserver.domain.events.FileHasBeenDisposed
import io.bcs.fileserver.domain.model.DistributionPointNameProvider
import io.bcs.fileserver.domain.model.content.ContentLocator
import io.bcs.fileserver.domain.model.file.File
import io.bcs.fileserver.domain.model.file.FileRepository
import io.bcs.fileserver.domain.model.file.FileStatus
import io.bcs.fileserver.domain.services.FileService.CreateFile
import java.time.LocalDateTime
import spock.lang.Specification

class FileServiceSpec extends Specification {
  public static final String DISTRIBUTION_POINT_NAME = "DEFAULT"
  public static final String STORAGE_NAME = "storage.0001"
  public static final String STORAGE_FILE_NAME = "instance--${Thread.currentThread()}--${UUID.randomUUID()}"
  public static final String MEDIA_TYPE = "application/media-type-xxx"
  public static final String FILE_NAME = "file.txt"
  public static final Long DEFAULT_CONTENT_LENGTH = 0L
  public static final Long DISTRIBUTIONING_CONTENT_LENGTH = 100L
  public static final String DEFAULT_MEDIA_TYPE = "application/octet-stream";


  private ValidationService validationService
  private FileRepository fileRepository;
  private EventBus eventBus;
  private FileService fileService
  private Generator<String> fileNameGenerator
  private EventPublisher eventPublisher
  private DistributionPointNameProvider distributionPointNameProvider

  def setup() {
    this.validationService = Mock(ValidationService)
    this.fileRepository = Mock(FileRepository)
    this.fileNameGenerator = Mock(Generator)
    this.eventBus = Mock(EventBus)
    this.eventPublisher = Mock(EventPublisher)
    this.distributionPointNameProvider = Mock(DistributionPointNameProvider)
    this.eventBus.getPublisher(_, _) >> eventPublisher
    this.fileNameGenerator.generateNext() >> STORAGE_FILE_NAME
    this.distributionPointNameProvider.getDistributionPointName() >> DISTRIBUTION_POINT_NAME
    this.fileService = new FileService(validationService, fileRepository, fileNameGenerator, distributionPointNameProvider, eventBus)
  }

  def "Scenario: successfully create new file"() {
    File file
    FileHasBeenCreated event
    given: "The create file command"
    CreateFile command = createFileCommand(MEDIA_TYPE, FILE_NAME)

    and: "The command passes validation"
    this.validationService.validate(command) >> new ValidationState()

    and: "The response handler"
    ResponseHandler responseHandler = Mock(ResponseHandler)


    when: "The file is created"
    WaitingPromise.of(fileService.createFile(command)).then(responseHandler).await()

    then: "The draft file should be stored into repository"
    1 * fileRepository.save(_) >> {file = it[0]}

    and: "The distribution point should be ${DISTRIBUTION_POINT_NAME}"
    file.getDistributionPoint() == DISTRIBUTION_POINT_NAME

    and: "The storage file name should be ${STORAGE_FILE_NAME}"
    file.getStorageFileName() == STORAGE_FILE_NAME

    and: "The storage name shouldn't be assigned"
    file.getStorageName() == Optional.empty()

    and: "The status should be ${FileStatus.DRAFT}"
    file.getStatus() == FileStatus.DRAFT

    and: "The media type should be ${MEDIA_TYPE}"
    file.getMediaType() == MEDIA_TYPE

    and: "The file name should be ${FILE_NAME}"
    file.getFileName() == FILE_NAME

    and: "The file creation time is initialized"
    file.getCreatedAt().isBefore(LocalDateTime.now().plusMinutes(1)) &&
        file.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(1L))

    and: "The file dispose time shouldn't be specified"
    file.getDisposedAt().isPresent() == false

    and: "The total length should be 0"
    file.getTotalLength() == 0L

    and: "The system should be notified about file creation"
    1 * eventPublisher.publish(_) >> {event = it[0]}
    event.getStorageFileName() == STORAGE_FILE_NAME
    event.getMediaType() == MEDIA_TYPE
  }

  def "Scenario: successfully create new file for missing mediatype"() {
    File file
    FileHasBeenCreated event
    given: "The create file command"
    CreateFile command = createFileCommand(null, FILE_NAME)

    and: "The command passes validation"
    this.validationService.validate(command) >> new ValidationState()

    and: "The response handler"
    ResponseHandler responseHandler = Mock(ResponseHandler)


    when: "The file is created"
    WaitingPromise.of(fileService.createFile(command)).then(responseHandler).await()

    then: "The draft file should be stored into repository"
    1 * fileRepository.save(_) >> {file = it[0]}

    and: "The distribution point should be ${DISTRIBUTION_POINT_NAME}"
    file.getDistributionPoint() == DISTRIBUTION_POINT_NAME

    and: "The storage file name should be ${STORAGE_FILE_NAME}"
    file.getStorageFileName() == STORAGE_FILE_NAME

    and: "The storage name shouldn't be assigned"
    file.getStorageName() == Optional.empty()

    and: "The status should be ${FileStatus.DRAFT}"
    file.getStatus() == FileStatus.DRAFT

    and: "The media type should be ${DEFAULT_MEDIA_TYPE}"
    file.getMediaType() == DEFAULT_MEDIA_TYPE

    and: "The file name should be ${FILE_NAME}"
    file.getFileName() == FILE_NAME

    and: "The total length should be 0"
    file.getTotalLength() == 0L

    and: "The file creation time is initialized"
    file.getCreatedAt().isBefore(LocalDateTime.now().plusMinutes(1)) &&
        file.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(1L))

    and: "The file dispose time shouldn't be specified"
    file.getDisposedAt().isPresent() == false

    and: "The system should be notified about file creation"
    1 * eventPublisher.publish(_) >> {event = it[0]}
    event.getStorageFileName() == STORAGE_FILE_NAME
    event.getMediaType() == DEFAULT_MEDIA_TYPE
  }

  def "Scenario: successfully create new file for missing file name"() {
    File file
    FileHasBeenCreated event
    given: "The create file command"
    CreateFile command = createFileCommand(MEDIA_TYPE, null)

    and: "The command passes validation"
    this.validationService.validate(command) >> new ValidationState()

    and: "The response handler"
    ResponseHandler responseHandler = Mock(ResponseHandler)

    when: "The file is created"
    WaitingPromise.of(fileService.createFile(command)).then(responseHandler).await()

    then: "The draft file should be stored into repository"
    1 * fileRepository.save(_) >> {file = it[0]}

    and: "The distribution point should be ${DISTRIBUTION_POINT_NAME}"
    file.getDistributionPoint() == DISTRIBUTION_POINT_NAME

    and: "The storage file name should be ${STORAGE_FILE_NAME}"
    file.getStorageFileName() == STORAGE_FILE_NAME

    and: "The storage name shouldn't be assigned"
    file.getStorageName() == Optional.empty()

    and: "The status should be ${FileStatus.DRAFT}"
    file.getStatus() == FileStatus.DRAFT

    and: "The media type should be ${MEDIA_TYPE}"
    file.getMediaType() == MEDIA_TYPE

    and: "The file name should be equal to storage file name"
    file.getFileName() == file.getStorageFileName()

    and: "The total length should be 0"
    file.getTotalLength() == 0L

    and: "The file creation time is initialized"
    file.getCreatedAt().isBefore(LocalDateTime.now().plusMinutes(1)) &&
        file.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(1L))

    and: "The file dispose time shouldn't be specified"
    file.getDisposedAt().isPresent() == false

    and: "The system should be notified about file creation"
    1 * eventPublisher.publish(_) >> {event = it[0]}
    event.getStorageFileName() == STORAGE_FILE_NAME
    event.getMediaType() == MEDIA_TYPE
  }

  def "Scenario: create new file for invalid request"() {
    PrimaryValidationException error
    given: "The create file command"
    CreateFile command = createFileCommand(MEDIA_TYPE, FILE_NAME)

    and: "The command passes validation"
    this.validationService.validate(command) >> new ValidationState().withUngrouped(ErrorMessage.createFor("Smth went wrong"))

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
    FileHasBeenDisposed event
    given: "The distributing file, existing into repository"
    this.fileRepository.findById(_) >> Optional.of(createFileDescriptor(FileStatus.DISTRIBUTING, DISTRIBUTIONING_CONTENT_LENGTH))

    and: "The response handler"
    ResponseHandler responseHandler = Mock(ResponseHandler)

    when: "The file is disposed"
    WaitingPromise.of(fileService.disposeFile(STORAGE_FILE_NAME)).then(responseHandler).await(100)

    then: "The file should be disposed and stored into repository"
    1 * fileRepository.save(_) >> {file = it[0]}

    and: "The storage file name should be ${STORAGE_FILE_NAME}"
    file.getStorageFileName() == STORAGE_FILE_NAME

    and: "The storage name shouldn't be assigned"
    file.getStorageName() == Optional.of(STORAGE_NAME)

    and: "The status should be ${FileStatus.DRAFT}"
    file.getStatus() == FileStatus.DISPOSED

    and: "The media type should be ${MEDIA_TYPE}"
    file.getMediaType() == MEDIA_TYPE

    and: "The file name should be equal to storage file name"
    file.getFileName() == FILE_NAME

    and: "The total length should not be clear"
    file.getTotalLength() == DISTRIBUTIONING_CONTENT_LENGTH

    and: "The file dispose time should be initialized"
    LocalDateTime disposedAt = file.getDisposedAt().get()
    disposedAt.isBefore(LocalDateTime.now().plusMinutes(1L)) &&
        disposedAt.isAfter(LocalDateTime.now().minusMinutes(1L))

    and: "The system should be notified that file has been disposed"
    1 * eventPublisher.publish(_) >> {event = it[0]}
    event.getStorageFileName() == STORAGE_FILE_NAME
  }

  def "Scenario: dispose existing file which had already disposed before"() {
    FileDisposedException error
    given: "The distributing file, existing into repository"
    this.fileRepository.findById(_) >> Optional.of(createFileDescriptor(FileStatus.DISPOSED, DISTRIBUTIONING_CONTENT_LENGTH))

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

  private File createFileDescriptor(FileStatus status, Long contentLength) {
    return File.builder()
        .distributionPoint(DISTRIBUTION_POINT_NAME)
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
