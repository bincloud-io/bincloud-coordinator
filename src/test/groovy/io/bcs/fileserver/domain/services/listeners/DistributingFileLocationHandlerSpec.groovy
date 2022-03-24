package io.bcs.fileserver.domain.services.listeners

import io.bcs.fileserver.domain.events.FileDistributionHasBeenStarted
import io.bcs.fileserver.domain.model.file.FileLocation
import io.bcs.fileserver.domain.model.file.FileLocationRepository
import io.bcs.fileserver.domain.model.storage.DefaultContentLocator
import java.time.LocalDate
import java.time.LocalDateTime
import spock.lang.Specification

class DistributingFileLocationHandlerSpec extends Specification {
  private static final String STORAGE_NAME = "storage.0001"
  private static final String STORAGE_FILE_NAME = "instance--${Thread.currentThread()}--${UUID.randomUUID()}"
  private static final String MEDIA_TYPE = "application/media-type-xxx"
  private static final LocalDateTime TODAY = LocalDateTime.now()
  private static final String FILE_NAME = "file.txt"
  private static final Long CONTENT_LENGTH = 100L

  private FileLocationRepository fileLocationRepository;
  private DistributingFileLocationHandler eventListener;

  def setup() {
    this.fileLocationRepository = Mock(FileLocationRepository)
    this.eventListener = new DistributingFileLocationHandler(fileLocationRepository)
  }

  def "Scenario: create file location after file distribution started"() {
    Collection<FileLocation> fileLocations
    given: "The file distribution has been started event for file with ${STORAGE_FILE_NAME} on ${STORAGE_NAME} storage"
    FileDistributionHasBeenStarted event = FileDistributionHasBeenStarted.builder()
        .storageFileName(STORAGE_FILE_NAME)
        .storageName(STORAGE_NAME)
        .build()
    when: "The event is received by listener"
    eventListener.onEvent(event)

    then: "The file location entity should be created and stored to the repository"
    1 * fileLocationRepository.save(_) >> {
      fileLocations = it[0]
    }

    and: "Only one event should be stored"
    fileLocations.size() == 1

    FileLocation fileLocation = fileLocations[0]
    and: "The file location storage file name should be ${STORAGE_FILE_NAME}"
    fileLocation.getStorageFileName() == STORAGE_FILE_NAME

    and: "The file location storage name should be ${STORAGE_NAME}"
    fileLocation.getStorageName() == STORAGE_NAME

    and: "The file location created date is between ${TODAY.minusMinutes(10)} and ${TODAY.plusMinutes(10)}"
    fileLocation.getLastModification() >= TODAY.minusMinutes(10)
    fileLocation.getLastModification() <= TODAY.plusMinutes(10)

    and: "The file location should be active"
    fileLocation.isActive() == true
  }
}
