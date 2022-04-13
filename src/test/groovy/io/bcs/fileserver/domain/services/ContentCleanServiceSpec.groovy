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
import io.bcs.fileserver.domain.model.file.content.download.FileContent
import io.bcs.fileserver.domain.model.file.content.download.Range
import io.bcs.fileserver.domain.model.file.content.download.FileContent.ContentPart
import io.bcs.fileserver.domain.model.file.content.download.FileContent.ContentType
import io.bcs.fileserver.domain.model.file.content.upload.FileUploadStatistic
import io.bcs.fileserver.domain.model.storage.ContentLocator
import io.bcs.fileserver.domain.model.storage.FileStorage
import spock.lang.Specification

class ContentCleanServiceSpec extends Specification {
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
  private ContentCleanService contentService

  def setup() {
    this.fileRepository = Mock(FileRepository)
    this.fileStorage = Mock(FileStorage)
    this.eventBus = Mock(EventBus)
    this.eventPublisher = Mock(EventPublisher)
    this.eventBus.getPublisher(_, _) >> eventPublisher
    this.contentService = new ContentCleanService(fileRepository, fileStorage)
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
}
