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
import io.bcs.fileserver.domain.Constants
import io.bcs.fileserver.domain.errors.FileNotExistsException
import io.bcs.fileserver.domain.errors.FileNotSpecifiedException
import io.bcs.fileserver.domain.model.file.File
import io.bcs.fileserver.domain.model.file.FileRepository
import io.bcs.fileserver.domain.model.file.content.ContentManagement
import io.bcs.fileserver.domain.model.file.content.ContentReceiver
import io.bcs.fileserver.domain.model.file.content.ContentUploader
import io.bcs.fileserver.domain.model.file.content.ContentManagement.DownloadCommand
import io.bcs.fileserver.domain.model.file.lifecycle.Lifecycle.FileUploadStatistic
import io.bcs.fileserver.domain.model.file.state.FileStatus
import io.bcs.fileserver.domain.model.storage.ContentLocator
import io.bcs.fileserver.domain.model.storage.FileStorage
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
  private ContentManagement fileService

  def setup() {
    this.fileRepository = Mock(FileRepository)
    this.fileStorage = Mock(FileStorage)
    this.fileService = new ContentService(fileRepository, fileStorage)
  }

  def "Scenario: upload content for unknown file"() {
    FileNotSpecifiedException error
    given: "The file uploader"
    ContentUploader contentUploader = Mock(ContentUploader)

    and: "The error handler"
    ErrorHandler errorHandler = Mock(ErrorHandler)


    when: "The file is uploaded for unspecified file storage name"
    WaitingPromise.of(fileService.upload(Optional.empty(), contentUploader)).error(errorHandler).await()

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
    WaitingPromise.of(fileService.upload(Optional.ofNullable(STORAGE_FILE_NAME), contentUploader)).then(responseHandler).await()

    then: "The file should be stored in the distributing state"
    1 * fileRepository.save(_) >> {file = it[0]}
    file.getLocator().getStorageFileName() == STORAGE_FILE_NAME
    file.getLocator().getStorageName() == STORAGE_NAME
    file.getTotalLength() == DISTRIBUTIONING_CONTENT_LENGTH

    and: "The response handler should be resolved"
    1 * responseHandler.onResponse(_) >> {statistic = it[0]}
    statistic.getLocator().getStorageFileName() == STORAGE_FILE_NAME
    statistic.getLocator().getStorageName() == STORAGE_NAME
    statistic.getTotalLength() ==  DISTRIBUTIONING_CONTENT_LENGTH
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
    WaitingPromise.of(fileService.upload(Optional.ofNullable(STORAGE_FILE_NAME), contentUploader)).error(errorHandler).await()

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
    WaitingPromise.of(fileService.download(downloadCommand(Optional.empty()), contentReceiver)).error(errorHandler).await()

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
    WaitingPromise.of(fileService.download(downloadCommand(Optional.of(STORAGE_FILE_NAME)), contentDownloader)).then(responseHandler).await()

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
    WaitingPromise.of(fileService.download(downloadCommand(Optional.of(STORAGE_FILE_NAME)), contentDownloader)).error(errorHandler).await()

    then: "The file not exists error should be happened"
    1 * errorHandler.onError(_) >> {error = it[0]}
    error.getContextId() == Constants.CONTEXT
    error.getErrorSeverity() == ErrorSeverity.BUSINESS
    error.getErrorCode() == Constants.FILE_NOT_EXIST_ERROR
  }

  private DownloadCommand downloadCommand(Optional<String> storageFileName) {
    return Stub(DownloadCommand) {
      getStorageFileName() >> storageFileName
      getRanges() >> []
    }
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
