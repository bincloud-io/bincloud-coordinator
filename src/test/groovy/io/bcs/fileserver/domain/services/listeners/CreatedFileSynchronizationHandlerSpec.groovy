package io.bcs.fileserver.domain.services.listeners

import io.bcs.fileserver.domain.events.FileHasBeenCreated
import io.bcs.fileserver.domain.model.file.File
import io.bcs.fileserver.domain.model.file.FileRepository
import io.bcs.fileserver.domain.services.listeners.CreatedFileSynchronizationHandler.DistribuionPointsProvider
import spock.lang.Specification

class CreatedFileSynchronizationHandlerSpec extends Specification {

  private static final String ORIGINAL_DISTRIBUTION_POINT_NAME = "DEFAULT"
  private static final String ADDITIONAL_DISTRIBUTION_POINT_1 = "P-1"
  private static final String ADDITIONAL_DISTRIBUTION_POINT_2 = "P-2"
  private static final String STORAGE_NAME = "storage.0001"
  private static final String STORAGE_FILE_NAME = "instance--${Thread.currentThread()}--${UUID.randomUUID()}"
  private static final String MEDIA_TYPE = "application/media-type-xxx"
  private static final String FILE_NAME = "file.txt"
  private static final Long DEFAULT_CONTENT_LENGTH = 0L
  private static final String DEFAULT_MEDIA_TYPE = "application/octet-stream";

  private FileRepository fileRepository;
  private DistribuionPointsProvider replicationPointsProvider;
  private CreatedFileSynchronizationHandler eventHandler;

  def setup() {
    this.fileRepository = Mock(FileRepository)
    this.replicationPointsProvider = Stub(DistribuionPointsProvider)
    this.eventHandler = new CreatedFileSynchronizationHandler(fileRepository, replicationPointsProvider)
  }

  def "Scenario: replicate file after creation"() {
    File firstFile
    File secondFile
    given: "The file has been created event"
    FileHasBeenCreated event = createFileHasBeenCreatedEvent()

    and: "The provided replication distribution points"
    replicationPointsProvider.findDistributionPoints() >> [
      ADDITIONAL_DISTRIBUTION_POINT_1,
      ADDITIONAL_DISTRIBUTION_POINT_2
    ]

    when: "The event is received"
    eventHandler.onEvent(event)

    then: "The files should be created for two "
    2 * fileRepository.save(_) >> {firstFile = it[0]} >> {secondFile = it[0]}

    and: "The file states should be equivalent excluding distribution point"
    firstFile.getDistributionPoint() == ADDITIONAL_DISTRIBUTION_POINT_1
    secondFile.getDistributionPoint() == ADDITIONAL_DISTRIBUTION_POINT_2
    firstFile.getStorageFileName() == secondFile.getStorageFileName()
    firstFile.getStorageName() == secondFile.getStorageName()
    firstFile.getStatus() == secondFile.getStatus()
    firstFile.getMediaType() == secondFile.getMediaType()
    firstFile.getFileName() == secondFile.getFileName()
    firstFile.getTotalLength() == secondFile.getTotalLength()
    firstFile.getCreatedAt() == secondFile.getCreatedAt()
    firstFile.getDisposedAt() == secondFile.getDisposedAt()
  }

  private FileHasBeenCreated createFileHasBeenCreatedEvent() {
    return FileHasBeenCreated.builder()
        .distributionPoint(ORIGINAL_DISTRIBUTION_POINT_NAME)
        .storageFileName(STORAGE_FILE_NAME)
        .mediaType(DEFAULT_MEDIA_TYPE)
        .fileName(FILE_NAME)
        .build()
  }
}