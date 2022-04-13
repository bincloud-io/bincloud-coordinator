package io.bcs.fileserver.domain.services.listeners

import static io.bcs.fileserver.domain.model.file.FileStatus.DISTRIBUTING

import io.bcs.fileserver.domain.events.FileDistributionHasBeenStarted
import io.bcs.fileserver.domain.events.FileHasBeenDisposed
import io.bcs.fileserver.domain.model.file.File
import io.bcs.fileserver.domain.model.file.FileRepository
import io.bcs.fileserver.domain.model.file.FileStatus
import java.time.LocalDateTime
import spock.lang.Specification

class DistributioningFileDisposingHandlerSpec extends Specification {
  private static final String CURRENT_DISTRIBUTION_POINT_NAME = "CURRENT"
  private static final String DISTRIBUTION_POINT_NAME_1 = "DP_1"
  private static final String DISTRIBUTION_POINT_NAME_2 = "DP_2"
  private static final String STORAGE_NAME = "storage.0001"
  private static final String STORAGE_FILE_NAME = "instance--${Thread.currentThread()}--${UUID.randomUUID()}"
  private static final String MEDIA_TYPE = "application/media-type-xxx"
  private static final String FILE_NAME = "file.txt"
  private static final Long DISTRIBUTIONING_CONTENT_LENGTH = 100L
  private static final LocalDateTime CREATED_TIME = LocalDateTime.now()

  private FileRepository fileRepository
  private DistributioningFileDisposingHandler eventHandler

  def setup() {
    this.fileRepository = Mock(FileRepository)
    this.eventHandler = new DistributioningFileDisposingHandler(fileRepository)
  }

  def "Scenario: apply event for existing distributioning files on another distribution points"() {
    File firstFile
    File secondFile
    given: "The file distribution has been started event"
    FileHasBeenDisposed event = createEvent()

    and: "The files is exists for ${[DISTRIBUTION_POINT_NAME_1, DISTRIBUTION_POINT_NAME_2]} "
    this.fileRepository.findAllReplicatedFiles(STORAGE_FILE_NAME) >> [
      createFile(DISTRIBUTION_POINT_NAME_1, DISTRIBUTING),
      createFile(DISTRIBUTION_POINT_NAME_2, DISTRIBUTING)
    ]

    when: "The event is received"
    eventHandler.onEvent(event)

    then: "The two activated files should be stored"
    2 * fileRepository.save(_) >> {firstFile = it[0]} >> {secondFile = it[0]}
    
    and: "Files states should be equivalent excepts distribution points"
    firstFile.getDistributionPoint() == DISTRIBUTION_POINT_NAME_1
    secondFile.getDistributionPoint() == DISTRIBUTION_POINT_NAME_2
    secondFile.getStorageFileName() == firstFile.getStorageFileName()
    secondFile.getStorageName() == firstFile.getStorageName()
    secondFile.getStatus() == firstFile.getStatus()
    secondFile.getMediaType() == firstFile.getMediaType()
    secondFile.getFileName() == firstFile.getFileName()
    secondFile.getTotalLength() == firstFile.getTotalLength()
    secondFile.getCreatedAt() == firstFile.getCreatedAt()
    secondFile.getDisposedAt() == firstFile.getDisposedAt()
  }

  private FileHasBeenDisposed createEvent() {
    return new FileHasBeenDisposed(STORAGE_FILE_NAME)
  }
  
  private File createFile(String distributionPointName, FileStatus status) {
    return File.builder()
        .distributionPoint(distributionPointName)
        .storageFileName(STORAGE_FILE_NAME)
        .status(status)
        .mediaType(MEDIA_TYPE)
        .fileName(FILE_NAME)
        .createdAt(CREATED_TIME)
        .build()
  }
}