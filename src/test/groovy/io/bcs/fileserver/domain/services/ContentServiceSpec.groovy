package io.bcs.fileserver.domain.services

import static io.bcs.fileserver.domain.model.file.FileStatus.DISPOSED
import static io.bcs.fileserver.domain.model.file.FileStatus.DISTRIBUTING
import static io.bcs.fileserver.domain.model.file.FileStatus.DRAFT

import io.bce.domain.EventBus
import io.bce.domain.EventPublisher
import io.bce.domain.errors.ErrorDescriptor.ErrorSeverity
import io.bce.interaction.streaming.Source
import io.bce.interaction.streaming.binary.BinaryChunk
import io.bce.promises.Promises
import io.bce.promises.WaitingPromise
import io.bce.promises.Promise.ErrorHandler
import io.bce.promises.Promise.ResponseHandler
import io.bcs.fileserver.domain.Constants
import io.bcs.fileserver.domain.errors.ContentNotUploadedException
import io.bcs.fileserver.domain.errors.ContentUploadedException
import io.bcs.fileserver.domain.errors.FileDisposedException
import io.bcs.fileserver.domain.errors.FileNotExistsException
import io.bcs.fileserver.domain.errors.FileNotSpecifiedException
import io.bcs.fileserver.domain.errors.FileStorageException
import io.bcs.fileserver.domain.errors.UnsatisfiableRangeFormatException
import io.bcs.fileserver.domain.events.FileDownloadHasBeenRequested
import io.bcs.fileserver.domain.model.file.File
import io.bcs.fileserver.domain.model.file.FileRepository
import io.bcs.fileserver.domain.model.file.FileStatus
import io.bcs.fileserver.domain.model.file.Range
import io.bcs.fileserver.domain.model.file.content.FileContent
import io.bcs.fileserver.domain.model.file.content.FileUploadStatistic
import io.bcs.fileserver.domain.model.file.content.Downloader.ContentReceiver
import io.bcs.fileserver.domain.model.file.content.FileContent.ContentPart
import io.bcs.fileserver.domain.model.file.content.FileContent.ContentType
import io.bcs.fileserver.domain.model.file.content.Uploader.ContentSource
import io.bcs.fileserver.domain.model.storage.ContentLocator
import io.bcs.fileserver.domain.model.storage.FileStorage
import io.bcs.fileserver.domain.services.ContentService.DownloadCommand
import spock.lang.Specification

class ContentServiceSpec extends Specification {
  public static final String STORAGE_NAME = "storage.0001"
  public static final String STORAGE_FILE_NAME = "instance--${Thread.currentThread()}--${UUID.randomUUID()}"
  public static final String MEDIA_TYPE = "application/media-type-xxx"
  public static final String FILE_NAME = "file.txt"
  public static final Long DEFAULT_CONTENT_LENGTH = 0L
  public static final Long DISTRIBUTIONING_CONTENT_LENGTH = 100L


  private FileRepository fileRepository
  private FileStorage fileStorage
  private EventBus eventBus
  private EventPublisher eventPublisher
  private ContentService contentService

  def setup() {
    this.fileRepository = Mock(FileRepository)
    this.fileStorage = Mock(FileStorage)
    this.eventBus = Mock(EventBus)
    this.eventPublisher = Mock(EventPublisher)
    this.eventBus.getPublisher(_, _) >> eventPublisher
    this.contentService = new ContentService(fileRepository, fileStorage, eventBus)
  }

  def "Scenario: unsuccessfully upload file content to the unspecified file"() {
    FileNotSpecifiedException error

    given: "The file content source"
    ContentSource contentSource = Mock(ContentSource)

    and: "The error handler"
    ErrorHandler errorHandler = Mock(ErrorHandler)

    when: "The file is uploaded for unspecified file storage name"
    WaitingPromise.of(contentService.upload(Optional.empty(), DISTRIBUTIONING_CONTENT_LENGTH, contentSource))
        .error(errorHandler).await()

    then: "The file is not specified error should be happened"
    1 * errorHandler.onError(_) >> {error = it[0]}
    error.getContextId() == Constants.CONTEXT
    error.getErrorSeverity() == ErrorSeverity.BUSINESS
    error.getErrorCode() == Constants.FILE_IS_NOT_SPECIFIED
  }

  def "Scenario: unsuccessfully upload file content to the unknown file"() {
    FileNotExistsException error
    given: "The draft file, missing into repository"
    this.fileRepository.findById(STORAGE_FILE_NAME) >> Optional.empty()

    and: "The file content source"
    ContentSource contentSource = Mock(ContentSource)

    and: "The error handler"
    ErrorHandler errorHandler = Mock(ErrorHandler)

    when: "The file is uploaded"
    WaitingPromise.of(contentService.upload(Optional.ofNullable(STORAGE_FILE_NAME), DISTRIBUTIONING_CONTENT_LENGTH, contentSource))
        .error(errorHandler).await()

    then: "The file not exists error should be happened"
    1 * errorHandler.onError(_) >> {error = it[0]}
    error.getContextId() == Constants.CONTEXT
    error.getErrorSeverity() == ErrorSeverity.BUSINESS
    error.getErrorCode() == Constants.FILE_NOT_EXIST_ERROR
  }

  def "Scenario: successfully upload file content to the draft file"() {
    File file
    FileUploadStatistic statistic

    given: "The draft file, existing into repository."+
    "Storage file name: ${STORAGE_FILE_NAME}" +
    "Storage name: ${STORAGE_NAME}" +
    "Media type: ${MEDIA_TYPE}" +
    "File name: ${FILE_NAME}" +
    "Total length: ${DEFAULT_CONTENT_LENGTH}"
    this.fileRepository.findById(STORAGE_FILE_NAME) >> Optional.of(createDraftFile())

    and: "The file will be created"
    fileStorage.create(_, _) >> contentLocator()

    and: "The file content source"
    ContentSource contentSource = Mock(ContentSource)
    contentSource.sendContent(_, _) >> Promises.resolvedBy(fileUploadStatistic())

    and: "The response handler"
    ResponseHandler responseHandler = Mock(ResponseHandler)

    when: "The file is uploaded"
    WaitingPromise.of(contentService.upload(Optional.ofNullable(STORAGE_FILE_NAME), DISTRIBUTIONING_CONTENT_LENGTH, contentSource))
        .then(responseHandler).await(100L)

    then: "The file should be stored in the distributing state"
    1 * fileRepository.save(_) >> {file = it[0]}

    and: "The storage file name should be ${STORAGE_FILE_NAME}"
    file.getStorageFileName() == STORAGE_FILE_NAME

    and: "The storage name should be ${STORAGE_NAME}"
    file.getStorageName() == Optional.of(STORAGE_NAME)

    and: "The file status should be ${FileStatus.DISTRIBUTING}"

    and: "The media type should be ${MEDIA_TYPE}"
    file.getMediaType() == MEDIA_TYPE

    and: "The file name should be ${FILE_NAME}"
    file.getFileName() == FILE_NAME

    and: "The total length should be ${DISTRIBUTIONING_CONTENT_LENGTH}"
    file.getTotalLength() == DISTRIBUTIONING_CONTENT_LENGTH

    and: "The response handler should be resolved"
    1 * responseHandler.onResponse(_) >> {statistic = it[0]}
    ContentLocator locator = statistic.getLocator()
    locator.getStorageFileName() == STORAGE_FILE_NAME
    locator.getStorageName() == STORAGE_NAME
    statistic.getTotalLength() ==  DISTRIBUTIONING_CONTENT_LENGTH
  }

  def "Scenario: unsuccessfully upload file content if we can't get access on write"() {
    FileStorageException error
    given: "The draft file, existing into repository."+
    "Storage file name: ${STORAGE_FILE_NAME}" +
    "Storage name: ${STORAGE_NAME}" +
    "Media type: ${MEDIA_TYPE}" +
    "File name: ${FILE_NAME}" +
    "Total length: ${DEFAULT_CONTENT_LENGTH}"
    this.fileRepository.findById(STORAGE_FILE_NAME) >> Optional.of(createDraftFile())

    and: "The file will be created"
    fileStorage.create(_, _) >> contentLocator()

    and: "The file content source"
    ContentSource contentSource = Mock(ContentSource)
    contentSource.sendContent(_, _) >> Promises.rejectedBy(new FileStorageException(new IOException()))

    and: "The promise reject error handler"
    ErrorHandler errorHandler = Mock(ErrorHandler)

    when: "The file is uploaded"
    WaitingPromise.of(contentService.upload(Optional.ofNullable(STORAGE_FILE_NAME), DISTRIBUTIONING_CONTENT_LENGTH, contentSource))
        .error(errorHandler).await(100L)

    then: "The file storage exception should be happened"
    1 * errorHandler.onError(_) >> {error = it[0]}
    error.getContextId() == Constants.CONTEXT
    error.getErrorCode() == Constants.FILE_STORAGE_INCIDENT_ERROR
    error.getErrorSeverity() == ErrorSeverity.INCIDENT
  }

  def "Scenario: unsuccessfully upload file content to the distributed file"() {
    ContentUploadedException error
    given: "The distributing file, existing into repository."+
    "Storage file name: ${STORAGE_FILE_NAME}" +
    "Storage name: ${STORAGE_NAME}" +
    "Media type: ${MEDIA_TYPE}" +
    "File name: ${FILE_NAME}" +
    "Total length: ${DEFAULT_CONTENT_LENGTH}"
    this.fileRepository.findById(STORAGE_FILE_NAME) >> Optional.of(createDistributedFile(DISTRIBUTIONING_CONTENT_LENGTH))

    and: "The file content source"
    ContentSource contentSource = Mock(ContentSource)

    and: "The promise reject error handler"
    ErrorHandler errorHandler = Mock(ErrorHandler)

    when: "The file is uploaded"
    WaitingPromise.of(contentService.upload(Optional.ofNullable(STORAGE_FILE_NAME), DISTRIBUTIONING_CONTENT_LENGTH, contentSource))
        .error(errorHandler).await()

    then: "The file has already been uploaded exception should be happened"
    1 * errorHandler.onError(_) >> {error = it[0]}
    error.getContextId() == Constants.CONTEXT
    error.getErrorCode() == Constants.CONTENT_IS_UPLOADED_ERROR
    error.getErrorSeverity() == ErrorSeverity.BUSINESS

    and: "Access on write shouldn't be requested"
    0 * fileStorage.getAccessOnWrite(_)
  }

  def "Scenario: unsuccessfully upload file content to the disposed file"() {
    FileDisposedException error
    given: "The distributing file, existing into repository."+
    "Storage file name: ${STORAGE_FILE_NAME}" +
    "Storage name: ${STORAGE_NAME}" +
    "Media type: ${MEDIA_TYPE}" +
    "File name: ${FILE_NAME}" +
    "Total length: ${DEFAULT_CONTENT_LENGTH}"
    this.fileRepository.findById(STORAGE_FILE_NAME) >> Optional.of(createDisposedFile())

    and: "The file content source"
    ContentSource contentSource = Mock(ContentSource)

    and: "The promise reject error handler"
    ErrorHandler errorHandler = Mock(ErrorHandler)

    when: "The file is uploaded"
    WaitingPromise.of(contentService.upload(Optional.ofNullable(STORAGE_FILE_NAME), DISTRIBUTIONING_CONTENT_LENGTH, contentSource))
        .error(errorHandler).await()

    then: "The file has already been disposed exception should be happened"
    1 * errorHandler.onError(_) >> {error = it[0]}
    error.getContextId() == Constants.CONTEXT
    error.getErrorCode() == Constants.FILE_IS_DISPOSED_ERROR
    error.getErrorSeverity() == ErrorSeverity.BUSINESS

    and: "Access on write shouldn't be requested"
    0 * fileStorage.getAccessOnWrite(_)
  }

  def "Scenario: unsuccessfully download file content from the unspecified file"() {
    FileNotSpecifiedException error
    FileDownloadHasBeenRequested event
    given: "The file content receiver"
    ContentReceiver contentReceiver = Mock(ContentReceiver)

    and: "The error handler"
    ErrorHandler errorHandler = Mock(ErrorHandler)

    when: "The file download is requested"
    WaitingPromise.of(contentService.download(downloadCommand(Optional.empty()), contentReceiver))
        .error(errorHandler).await()

    then: "The file is not specified error should be happened"
    1 * errorHandler.onError(_) >> {error = it[0]}
    error.getContextId() == Constants.CONTEXT
    error.getErrorSeverity() == ErrorSeverity.BUSINESS
    error.getErrorCode() == Constants.FILE_IS_NOT_SPECIFIED

    and: "The file download has been requeted event should be published"
    1 * eventPublisher.publish(_) >> {event = it[0]}
    event.getStorageFileName() == Optional.empty()
  }

  def "Scenario: unsuccessfully download file content from the unknown file"() {
    FileNotExistsException error
    FileDownloadHasBeenRequested event
    given: "The draft file, missing into repository"
    this.fileRepository.findById(STORAGE_FILE_NAME) >> Optional.empty()

    and: "The file content receiver"
    ContentReceiver contentReceiver = Mock(ContentReceiver)

    and: "The error handler"
    ErrorHandler errorHandler = Mock(ErrorHandler)

    when: "The file download is requested"
    WaitingPromise.of(contentService.download(downloadCommand(Optional.of(STORAGE_FILE_NAME)), contentReceiver))
        .error(errorHandler).await()

    then: "The file not exists error should be happened"
    1 * errorHandler.onError(_) >> {error = it[0]}
    error.getContextId() == Constants.CONTEXT
    error.getErrorSeverity() == ErrorSeverity.BUSINESS
    error.getErrorCode() == Constants.FILE_NOT_EXIST_ERROR

    and: "The file download has been requeted event should be published"
    1 * eventPublisher.publish(_) >> {event = it[0]}
    event.getStorageFileName() == Optional.of(STORAGE_FILE_NAME)
  }

  def "Scenario: unsuccessfully download file content from draft file"() {
    ContentNotUploadedException error
    FileDownloadHasBeenRequested event
    given: "The existing draft file"
    this.fileRepository.findById(STORAGE_FILE_NAME) >> Optional.of(createDraftFile())

    and: "The file content receiver"
    ContentReceiver contentReceiver = Mock(ContentReceiver)


    and: "The file storage is going get access on write"
    fileStorage.getAccessOnRead(_, _) >> Stub(Source)

    and: "The promise reject error handler"
    ErrorHandler errorHandler = Mock(ErrorHandler)


    when: "The file download is requested"
    WaitingPromise.of(contentService.download(downloadCommand(Optional.of(STORAGE_FILE_NAME)), contentReceiver))
        .error(errorHandler).await()


    then: "The content not uploaded error should be happened"
    1 * errorHandler.onError(_) >> {error = it[0]}
    error.getContextId() == Constants.CONTEXT
    error.getErrorSeverity() == ErrorSeverity.BUSINESS
    error.getErrorCode() == Constants.CONTENT_IS_NOT_UPLOADED_ERROR

    and: "The file download has been requeted event should be published"
    1 * eventPublisher.publish(_) >> {event = it[0]}
    event.getStorageFileName() == Optional.of(STORAGE_FILE_NAME)
  }

  def "Scenario: unsuccessfully download file from disposed file"() {
    FileDisposedException error
    FileDownloadHasBeenRequested event
    given: "The existing disposed file"
    this.fileRepository.findById(STORAGE_FILE_NAME) >> Optional.of(createDisposedFile())

    and: "The file content receiver"
    ContentReceiver contentReceiver = Mock(ContentReceiver)


    and: "The file storage is going get access on write"
    fileStorage.getAccessOnRead(_, _) >> Stub(Source)

    and: "The promise reject error handler"
    ErrorHandler errorHandler = Mock(ErrorHandler)


    when: "The file download is requested"
    WaitingPromise.of(contentService.download(downloadCommand(Optional.of(STORAGE_FILE_NAME)), contentReceiver))
        .error(errorHandler).await()

    then: "The content not uploaded error should be happened"
    1 * errorHandler.onError(_) >> {error = it[0]}
    error.getContextId() == Constants.CONTEXT
    error.getErrorSeverity() == ErrorSeverity.BUSINESS
    error.getErrorCode() == Constants.FILE_IS_DISPOSED_ERROR

    and: "The file download has been requeted event should be published"
    1 * eventPublisher.publish(_) >> {event = it[0]}
    event.getStorageFileName() == Optional.of(STORAGE_FILE_NAME)
  }

  def "Scenario: successfully download full file content from distributed file"() {
    FileContent fileContent
    FileDownloadHasBeenRequested event
    given: "The existing distributing file"
    this.fileRepository.findById(STORAGE_FILE_NAME) >> Optional.of(createDistributedFile(DISTRIBUTIONING_CONTENT_LENGTH))

    and: "The content parts should not be passed"
    Collection<Range> contentParts = Collections.emptyList()

    and: "The file content receiver"
    ContentReceiver contentReceiver = Mock(ContentReceiver)

    and: "The file storage is going get access on write"
    Source<BinaryChunk> source = Stub(Source)
    fileStorage.getAccessOnRead(_, _) >> source

    and: "The response handler"
    ResponseHandler responseHandler = Mock(ResponseHandler)

    when: "The file download is requested"
    WaitingPromise.of(contentService.download(downloadCommand(Optional.of(STORAGE_FILE_NAME), contentParts), contentReceiver))
        .then(responseHandler).await(100L)

    then: "The file content downloading should be started"
    1 * contentReceiver.receiveFullContent(_) >> {
      fileContent = it[0]
      return Promises.resolvedBy(null)
    }

    and: "The file content type should be ${ContentType.FULL}"
    fileContent.getType() == ContentType.FULL

    and: "The content locator should specify to ${STORAGE_FILE_NAME} inside ${STORAGE_NAME} storage"
    ContentLocator locator = fileContent.getLocator()
    locator.getStorageFileName() == STORAGE_FILE_NAME
    locator.getStorageName() == STORAGE_NAME

    and: "The file content should contains one part"
    fileContent.getParts().size() == 1

    and: "The content part should represent whole file"
    ContentPart contentPart = fileContent.getParts()[0]
    contentPart.getContentFragment().getOffset() == 0L
    contentPart.getContentFragment().getLength() == DISTRIBUTIONING_CONTENT_LENGTH
    contentPart.getContentSource() == source

    and: "The response handler should be resolved"
    1 * responseHandler.onResponse(_)

    and: "The file download has been requeted event should be published"
    1 * eventPublisher.publish(_) >> {event = it[0]}
    event.getStorageFileName() == Optional.of(STORAGE_FILE_NAME)
  }


  def "Scenario: unsuccessfully download partial content from distributed file if negative size fragment is requested"() {
    FileDownloadHasBeenRequested event
    UnsatisfiableRangeFormatException error
    given: "The existing distributing file"
    this.fileRepository.findById(STORAGE_FILE_NAME) >> Optional.of(createDistributedFile(DISTRIBUTIONING_CONTENT_LENGTH))

    and: "The content parts contains a negative size requested fragment"
    Collection<Range> contentParts = [
      createRange(null, DISTRIBUTIONING_CONTENT_LENGTH + 30L)
    ]


    and: "The file content receiver"
    ContentReceiver contentReceiver = Mock(ContentReceiver)

    and: "The file storage is going get access on write"
    Source<BinaryChunk> source = Stub(Source)
    fileStorage.getAccessOnRead(_, _) >> source

    and: "The promise reject error handler"
    ErrorHandler errorHandler = Mock(ErrorHandler)


    when: "The file is uploaded"
    DownloadCommand downloadCommand = downloadCommand(Optional.of(STORAGE_FILE_NAME), contentParts)
    WaitingPromise.of(contentService.download(downloadCommand, contentReceiver))
        .error(errorHandler).await(100L)


    then: "The unsatisfiable range format error should be happened"
    1 * errorHandler.onError(_) >> {error = it[0]}
    error.getContextId() == Constants.CONTEXT
    error.getErrorSeverity() == ErrorSeverity.BUSINESS
    error.getErrorCode() == Constants.UNSATISFIABLE_RANGES_FORMAT_ERROR

    and: "The file download has been requeted event should be published"
    1 * eventPublisher.publish(_) >> {event = it[0]}
    event.getStorageFileName() == Optional.of(STORAGE_FILE_NAME)
  }


  def "Scenario: unsuccessfully download partial content from distributed file if start of range out of the total size"() {
    FileDownloadHasBeenRequested event
    UnsatisfiableRangeFormatException error
    given: "The existing distributing file"
    this.fileRepository.findById(STORAGE_FILE_NAME) >> Optional.of(createDistributedFile(DISTRIBUTIONING_CONTENT_LENGTH))

    and: "The content parts contains a negative size requested fragment"
    Collection<Range> contentParts = [
      createRange(DISTRIBUTIONING_CONTENT_LENGTH + 30L, null)
    ]


    and: "The file content receiver"
    ContentReceiver contentReceiver = Mock(ContentReceiver)

    and: "The file storage is going get access on write"
    Source<BinaryChunk> source = Stub(Source)
    fileStorage.getAccessOnRead(_, _) >> source

    and: "The promise reject error handler"
    ErrorHandler errorHandler = Mock(ErrorHandler)


    when: "The file is uploaded"
    DownloadCommand downloadCommand = downloadCommand(Optional.of(STORAGE_FILE_NAME), contentParts)
    WaitingPromise.of(contentService.download(downloadCommand, contentReceiver))
        .error(errorHandler).await(100L)


    then: "The unsatisfiable range format error should be happened"
    1 * errorHandler.onError(_) >> {error = it[0]}
    error.getContextId() == Constants.CONTEXT
    error.getErrorSeverity() == ErrorSeverity.BUSINESS
    error.getErrorCode() == Constants.UNSATISFIABLE_RANGES_FORMAT_ERROR

    and: "The file download has been requeted event should be published"
    1 * eventPublisher.publish(_) >> {event = it[0]}
    event.getStorageFileName() == Optional.of(STORAGE_FILE_NAME)
  }

  def "Scenario: unsuccessfully download partial content from distributed file if start of range is negative"() {
    FileDownloadHasBeenRequested event
    UnsatisfiableRangeFormatException error
    given: "The existing distributing file"
    this.fileRepository.findById(STORAGE_FILE_NAME) >> Optional.of(createDistributedFile(DISTRIBUTIONING_CONTENT_LENGTH))

    and: "The content parts contains a negative fragment offset"
    Collection<Range> contentParts = [
      createRange(-10, 10)
    ]

    and: "The file content receiver"
    ContentReceiver contentReceiver = Mock(ContentReceiver)

    and: "The file storage is going get access on write"
    Source<BinaryChunk> source = Stub(Source)
    fileStorage.getAccessOnRead(_, _) >> source

    and: "The promise reject error handler"
    ErrorHandler errorHandler = Mock(ErrorHandler)


    when: "The file is uploaded"
    DownloadCommand downloadCommand = downloadCommand(Optional.of(STORAGE_FILE_NAME), contentParts)
    WaitingPromise.of(contentService.download(downloadCommand, contentReceiver))
        .error(errorHandler).await(100L)


    then: "The unsatisfiable range format error should be happened"
    1 * errorHandler.onError(_) >> {error = it[0]}
    error.getContextId() == Constants.CONTEXT
    error.getErrorSeverity() == ErrorSeverity.BUSINESS
    error.getErrorCode() == Constants.UNSATISFIABLE_RANGES_FORMAT_ERROR

    and: "The file download has been requeted event should be published"
    1 * eventPublisher.publish(_) >> {event = it[0]}
    event.getStorageFileName() == Optional.of(STORAGE_FILE_NAME)
  }

  def "Scenario: successfully download partial content from distributed file for single part"() {
    FileContent fileContent
    FileDownloadHasBeenRequested event
    given: "The existing distributing file"
    this.fileRepository.findById(STORAGE_FILE_NAME) >> Optional.of(createDistributedFile(DISTRIBUTIONING_CONTENT_LENGTH))

    and: "The content parts contains valid range"
    Collection<Range> contentParts = [
      createRange(rangeStart, rangeEnd)
    ]

    and: "The file content receiver"
    ContentReceiver contentReceiver = Mock(ContentReceiver)

    and: "The file storage is going get access on write"
    Source<BinaryChunk> source = Stub(Source)
    fileStorage.getAccessOnRead(_, _) >> source

    and: "The response handler"
    ResponseHandler responseHandler = Mock(ResponseHandler)

    when: "The file download is requested"
    WaitingPromise.of(contentService.download(downloadCommand(Optional.of(STORAGE_FILE_NAME), contentParts), contentReceiver))
        .then(responseHandler).await()

    then: "The file content downloading should be started"
    1 * contentReceiver.receiveContentRange(_) >> {
      fileContent = it[0]
      return Promises.resolvedBy(null)
    }

    and: "The file content type should be ${ContentType.RANGE}"
    fileContent.getType() == ContentType.RANGE

    and: "The content locator should specify to ${STORAGE_FILE_NAME} inside ${STORAGE_NAME} storage"
    ContentLocator locator = fileContent.getLocator()
    locator.getStorageFileName() == STORAGE_FILE_NAME
    locator.getStorageName() == STORAGE_NAME

    and: "The file content should contains one part"
    fileContent.getParts().size() == 1

    and: "The content part should represent fragment: offset=${partOffset}, size=${partSize}"
    ContentPart contentPart = fileContent.getParts()[0]
    contentPart.getContentFragment().getOffset() == partOffset
    contentPart.getContentFragment().getLength() == partSize
    contentPart.getContentSource() == source

    and: "The response handler should be resolved"
    1 * responseHandler.onResponse(_)

    and: "The file download has been requeted event should be published"
    1 * eventPublisher.publish(_) >> {event = it[0]}
    event.getStorageFileName() == Optional.of(STORAGE_FILE_NAME)

    where:
    rangeStart      | rangeEnd                             | partOffset                           | partSize
    null            | null                                 | 0L                                   | DISTRIBUTIONING_CONTENT_LENGTH
    10L             | null                                 | 10L                                  | DISTRIBUTIONING_CONTENT_LENGTH - 10L
    null            | 10L                                  | DISTRIBUTIONING_CONTENT_LENGTH - 10L | 10L
    0L              | 0L                                   | 0L                                   | 1L
    10L             | 19L                                  | 10L                                  | 10L
    0L              | DISTRIBUTIONING_CONTENT_LENGTH + 10L | 0L                                   | DISTRIBUTIONING_CONTENT_LENGTH
    10L             | DISTRIBUTIONING_CONTENT_LENGTH + 10L | 10L                                  | DISTRIBUTIONING_CONTENT_LENGTH - 10L
  }

  def "Scenario: successfully download partial content from distributed file for multiple part"() {
    FileContent fileContent
    FileDownloadHasBeenRequested event
    given: "The existing distributing file"
    this.fileRepository.findById(STORAGE_FILE_NAME) >> Optional.of(createDistributedFile(DISTRIBUTIONING_CONTENT_LENGTH))

    and: "The content parts contains valid range"
    Collection<Range> contentParts = [
      createRange(0L, 9L),
      createRange(10L, 19L),
    ]

    and: "The file content receiver"
    ContentReceiver contentReceiver = Mock(ContentReceiver)

    and: "The file storage is going get access on write"
    Source<BinaryChunk> firstSource = Stub(Source)
    Source<BinaryChunk> secondSource = Stub(Source)
    fileStorage.getAccessOnRead(_, _) >> firstSource >> secondSource

    and: "The response handler"
    ResponseHandler responseHandler = Mock(ResponseHandler)

    when: "The file download is requested"
    WaitingPromise.of(contentService.download(downloadCommand(Optional.of(STORAGE_FILE_NAME), contentParts), contentReceiver))
        .then(responseHandler).await()

    then: "The file content downloading should be started"
    1 * contentReceiver.receiveContentRanges(_) >> {
      fileContent = it[0]
      return Promises.resolvedBy(null)
    }

    and: "The file content type should be ${ContentType.MULTIRANGE}"
    fileContent.getType() == ContentType.MULTIRANGE

    and: "The content locator should specify to ${STORAGE_FILE_NAME} inside ${STORAGE_NAME} storage"
    ContentLocator locator = fileContent.getLocator()
    locator.getStorageFileName() == STORAGE_FILE_NAME
    locator.getStorageName() == STORAGE_NAME

    and: "The file content should contains two parts"
    fileContent.getParts().size() == 2

    and: "The first content part should represent fragment: offset=0, size=10"
    ContentPart firstContentPart = fileContent.getParts()[0]
    firstContentPart.getContentFragment().getOffset() == 0L
    firstContentPart.getContentFragment().getLength() == 10L
    firstContentPart.getContentSource() == firstSource

    and: "The second content part should represent fragment: offset=10, size=10"
    ContentPart secondContentPart = fileContent.getParts()[1]
    secondContentPart.getContentFragment().getOffset() == 10L
    secondContentPart.getContentFragment().getLength() == 10L
    secondContentPart.getContentSource() == secondSource

    and: "The response handler should be resolved"
    1 * responseHandler.onResponse(_)

    and: "The file download has been requeted event should be published"
    1 * eventPublisher.publish(_) >> {event = it[0]}
    event.getStorageFileName() == Optional.of(STORAGE_FILE_NAME)
  }

  def "Scenario: clean disposed files"() {
    File file
    given: "The disposed file will be returned from repository"
    fileRepository.findNotRemovedDisposedFiles() >> [createDisposedFile()] >> []

    when: "The files clear operation is called"
    contentService.clearDisposedFiles()

    then: "Polled file should be deleted"
    1 * fileStorage.delete(_)

    and: "Polled file should be stored"
    1 * fileRepository.save(_) >> {file = it[0]}

    and: "Stored file total length should be clear"
    file.getTotalLength() == 0L

    and: "Stored file storage name should be clear"
    file.getStorageName().isPresent() == false
  }

  private Range createRange(Long start, Long end) {
    return new Range() {
          @Override
          public Optional<Long> getStart() {
            return Optional.ofNullable(start);
          }

          @Override
          public Optional<Long> getEnd() {
            return Optional.ofNullable(end);
          }
        }
  }

  private DownloadCommand downloadCommand(Optional<String> storageFileName) {
    return downloadCommand(storageFileName, [])
  }

  private DownloadCommand downloadCommand(Optional<String> storageFileName, Collection<Range> ranges) {
    return Stub(DownloadCommand) {
      getStorageFileName() >> storageFileName
      getRanges() >> ranges
    }
  }


  private File createDraftFile() {
    return createFile(DRAFT, DEFAULT_CONTENT_LENGTH)
  }

  private File createDistributedFile(Long contentLength) {
    return createFile(DISTRIBUTING, contentLength)
  }

  private File createMirrorDistributedFile(Long contentLength) {
    return createFile(DISTRIBUTING, contentLength)
  }

  private File createDisposedFile() {
    return createFile(DISPOSED, DISTRIBUTIONING_CONTENT_LENGTH)
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

  private FileUploadStatistic fileUploadStatistic() {
    return Stub(FileUploadStatistic) {
      getLocator() >> contentLocator()
      getTotalLength() >> DISTRIBUTIONING_CONTENT_LENGTH
    }
  }

  private ContentLocator contentLocator() {
    ContentLocator contentLocator = Stub(ContentLocator)
    contentLocator.getStorageName() >> STORAGE_NAME
    contentLocator.getStorageFileName() >> STORAGE_FILE_NAME
    return contentLocator
  }
}
