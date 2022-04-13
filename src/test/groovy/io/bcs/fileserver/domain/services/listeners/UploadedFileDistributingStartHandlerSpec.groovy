package io.bcs.fileserver.domain.services.listeners

import static io.bcs.fileserver.domain.model.file.FileStatus.DRAFT

import io.bce.domain.EventBus
import io.bce.domain.EventPublisher
import io.bcs.fileserver.domain.events.FileContentHasBeenUploaded
import io.bcs.fileserver.domain.events.FileDistributionHasBeenStarted
import io.bcs.fileserver.domain.model.file.File
import io.bcs.fileserver.domain.model.file.FileRepository
import io.bcs.fileserver.domain.model.file.FileStatus
import io.bcs.fileserver.domain.model.file.content.upload.FileUploadStatistic
import io.bcs.fileserver.domain.model.storage.ContentLocator
import java.time.LocalDateTime
import spock.lang.Specification

class UploadedFileDistributingStartHandlerSpec extends Specification {
  private static final String DISTRIBUTION_POINT_NAME = "DEFAULT"
  private static final String STORAGE_NAME = "storage.0001"
  private static final String STORAGE_FILE_NAME = "instance--${Thread.currentThread()}--${UUID.randomUUID()}"
  private static final String MEDIA_TYPE = "application/media-type-xxx"
  private static final String FILE_NAME = "file.txt"
  private static final Long DISTRIBUTIONING_CONTENT_LENGTH = 100L
  private static final LocalDateTime CREATED_TIME = LocalDateTime.now()

  private FileRepository fileRepository
  private EventPublisher eventPublisher
  private EventBus eventBus
  private UploadedFileDistributingStartHandler eventHandler

  def setup() {
    this.fileRepository = Mock(FileRepository)
    this.eventPublisher = Mock(EventPublisher)
    this.eventBus = Mock(EventBus)
    this.eventBus.getPublisher(_, _) >> eventPublisher
    this.eventHandler = new UploadedFileDistributingStartHandler(fileRepository, eventBus)
  }

  def "Scenario: apply event for existing draft file which content has been uploaded"() {
    File file
    FileDistributionHasBeenStarted distributionStartedEvent
    
    given: "The file content has been upload event"
    FileContentHasBeenUploaded event = createEvent()
     
    and: "The file is exists"
    this.fileRepository.findLocatedOnCurrentPoint(STORAGE_FILE_NAME) >> Optional.ofNullable(createFile(DRAFT))
    
    when: "The event is received"
    this.eventHandler.onEvent(event)
    
    then: "The file should be stored"
    1 * fileRepository.save(_) >> {file = it[0]}
    
    and: "The file distribution point should be ${DISTRIBUTION_POINT_NAME}"
    file.getDistributionPoint() == DISTRIBUTION_POINT_NAME
    
    and: "The file storage name should be ${STORAGE_NAME}"
    file.getStorageName().get() == STORAGE_NAME
    
    and: "The file storage file name should be ${STORAGE_FILE_NAME}"
    file.getStorageFileName() == STORAGE_FILE_NAME
    
    and: "The file status should be ${FileStatus.DISTRIBUTING}"
    file.getStatus() == FileStatus.DISTRIBUTING
    
    and: "The file media type should be ${MEDIA_TYPE}"
    file.getMediaType() == MEDIA_TYPE
    
    and: "The file name should be ${FILE_NAME}"
    file.getFileName() == FILE_NAME
    
    and: "The file total length should be ${DISTRIBUTIONING_CONTENT_LENGTH}"
    file.getTotalLength() == DISTRIBUTIONING_CONTENT_LENGTH
    
    and: "The created time should be ${CREATED_TIME}"
    file.getCreatedAt() == CREATED_TIME
    
    and: "The file distribution has been started event should be published"
    1 * eventPublisher.publish(_) >> {distributionStartedEvent = it[0]}
    
    and: "The event storage name should be ${STORAGE_NAME}"
    distributionStartedEvent.getLocator().getStorageName() == STORAGE_NAME
    
    and: "The event storage file name should be ${STORAGE_FILE_NAME}"
    distributionStartedEvent.getLocator().getStorageFileName() == STORAGE_FILE_NAME
    
    and: "The total length should be ${DISTRIBUTIONING_CONTENT_LENGTH}"
    distributionStartedEvent.getTotalLength() == DISTRIBUTIONING_CONTENT_LENGTH
  }

  private FileContentHasBeenUploaded createEvent() {
    return new FileContentHasBeenUploaded(createUploadStatistic())
  }
  
  private FileUploadStatistic createUploadStatistic() {
    FileUploadStatistic statistic = Mock(FileUploadStatistic)
    statistic.getLocator() >> createContentLocator()
    statistic.getTotalLength() >> DISTRIBUTIONING_CONTENT_LENGTH
    return statistic
  }
  
  private ContentLocator createContentLocator() {
    ContentLocator locator = Mock(ContentLocator)
    locator.getStorageName() >> STORAGE_NAME
    locator.getStorageFileName() >> STORAGE_FILE_NAME
    return locator
  }
  
  private File createFile(FileStatus status) {
    return File.builder()
        .distributionPoint(DISTRIBUTION_POINT_NAME)
        .storageFileName(STORAGE_FILE_NAME)
        .status(status)
        .mediaType(MEDIA_TYPE)
        .fileName(FILE_NAME)
        .createdAt(CREATED_TIME)
        .build()
  }
}
