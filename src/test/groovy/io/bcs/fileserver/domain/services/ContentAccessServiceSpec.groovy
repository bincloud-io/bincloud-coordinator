package io.bcs.fileserver.domain.services

import io.bce.interaction.streaming.Destination
import io.bce.interaction.streaming.Source
import io.bce.interaction.streaming.binary.BinaryChunk
import io.bcs.fileserver.domain.model.storage.ContentFragment
import io.bcs.fileserver.domain.model.storage.ContentLocator
import io.bcs.fileserver.domain.model.storage.FileStorage
import io.bcs.fileserver.domain.model.storage.StorageDescriptorRepository
import io.bcs.fileserver.domain.model.storage.StorageType
import io.bcs.fileserver.domain.model.storage.types.LocalStorageDescriptor
import io.bcs.fileserver.domain.model.storage.types.RemoteStorageDescriptor
import spock.lang.Specification

class ContentAccessServiceSpec extends Specification {
  private static final String DISTRIBUTION_POINT = "DPOINT"
  private static final String STORAGE_NAME = "storage.0001"
  private static final String STORAGE_FILE_NAME = "instance--${Thread.currentThread()}--${UUID.randomUUID()}"
  private static final String MEDIA_TYPE = "application/media-type-xxx"
  private static final Long FRAGMENT_OFFSET = 0L
  private static final Long FRAGMENT_LENGTH = 100L
  private static final String STORAGE_GATEWAY_WSDL = "http://storage.gateway/internal?wsdl"
  private static final String BASE_DIRECTORY = "./DPOINT/BASEDIR"
  private static final Long DISK_QUOTE = 1000L


  private Source<BinaryChunk> source
  private Destination<BinaryChunk> destination
  private StorageDescriptorRepository storageDescriptorRepository
  private ContentAccessService accessService
  private FileStorage fileStorage

  def setup() {
    this.fileStorage = createFileStorage()
    this.storageDescriptorRepository = Mock(StorageDescriptorRepository)
    this.accessService = new ContentAccessService(storageDescriptorRepository)
    StorageType.LOCAL.registerFileStorageProvider({fileStorage})
    StorageType.REMOTE.registerFileStorageProvider({fileStorage})
  }

  def "Scenario: create file"() {
    given: "The storage descriptor is returned for ${STORAGE_NAME} storage name"
    storageDescriptorRepository.findStorageDescriptor(_) >> Optional.of(storageDescriptor)

    when: "The file is created"
    ContentLocator contentLocator = accessService.create(createFileDescriptor(), FRAGMENT_LENGTH)

    then: "The content locator, returned by original storage, should be returned"
    contentLocator.getStorageName() == STORAGE_NAME
    contentLocator.getStorageFileName() == STORAGE_FILE_NAME

    where:
    storageDescriptor << [
      createLocalStorageDescriptor(),
      createRemoteStorageDescriptor()
    ]
  }

  def "Scenario: get access on write"() {
    given: "The storage descriptor is returned for ${STORAGE_NAME} storage name"
    storageDescriptorRepository.findStorageDescriptor(_) >> Optional.of(storageDescriptor)

    expect: "The destination, passed by original storage, should be returned"
    destination == accessService.getAccessOnWrite(createContentLocator())

    where:
    storageDescriptor << [
      createLocalStorageDescriptor(),
      createRemoteStorageDescriptor()
    ]
  }

  def "Scenario: get access on read"() {
    given: "The storage descriptor is returned for ${STORAGE_NAME} storage name"
    storageDescriptorRepository.findStorageDescriptor(_) >> Optional.of(storageDescriptor)

    expect: "The destination, passed by original storage, should be returned"
    source == accessService.getAccessOnRead(createContentLocator(), createContentFragment())

    where:
    storageDescriptor << [
      createLocalStorageDescriptor(),
      createRemoteStorageDescriptor()
    ]
  }

  def "Scenario: delete file"() {
    given: "The storage descriptor is returned for ${STORAGE_NAME} storage name"
    storageDescriptorRepository.findStorageDescriptor(_) >> Optional.of(storageDescriptor)

    when: "The file is created"
    ContentLocator contentLocator = accessService.delete(createFileDescriptor())

    then: "The delete operation should be delegated to the original storage"
    1 * this.fileStorage.delete(_)

    where:
    storageDescriptor << [
      createLocalStorageDescriptor(),
      createRemoteStorageDescriptor()
    ]
  }

  private LocalStorageDescriptor createLocalStorageDescriptor() {
    return LocalStorageDescriptor.builder()
        .storageName(STORAGE_NAME)
        .distributionPoint(DISTRIBUTION_POINT)
        .baseDirectory(BASE_DIRECTORY)
        .diskQuote(DISK_QUOTE)
        .build()
  }

  private RemoteStorageDescriptor createRemoteStorageDescriptor() {
    return RemoteStorageDescriptor.builder()
        .storageName(STORAGE_NAME)
        .distributionPoint(DISTRIBUTION_POINT)
        .remoteStorageGatewayWsdl(STORAGE_GATEWAY_WSDL)
        .build()
  }

  private FileStorage.FileDescriptor createFileDescriptor() {
    return Stub(FileStorage.FileDescriptor) {
      getStorageFileName() >> STORAGE_FILE_NAME
      getStorageName() >> Optional.of(STORAGE_NAME)
      getMediaType() >> MEDIA_TYPE
    }
  }

  private ContentLocator createContentLocator() {
    return Stub(ContentLocator) {
      getStorageName() >> STORAGE_NAME
      getStorageFileName() >> STORAGE_FILE_NAME
    }
  }

  private ContentFragment createContentFragment() {
    return Stub(ContentFragment) {
      getOffset() >> FRAGMENT_OFFSET
      getLength() >> FRAGMENT_LENGTH
    }
  }

  private FileStorage createFileStorage() {
    return Mock(FileStorage) {
      create(_, _) >> createContentLocator()
      getAccessOnRead(_, _) >> source
      getAccessOnWrite(_) >> destination
    }
  }
}
