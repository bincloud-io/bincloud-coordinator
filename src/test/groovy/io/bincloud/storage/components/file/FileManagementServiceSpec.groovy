package io.bincloud.storage.components.file

import java.time.Instant

import io.bincloud.common.ApplicationException
import io.bincloud.common.ApplicationException.Severity
import io.bincloud.common.io.transfer.CompletionCallback
import io.bincloud.common.io.transfer.DestinationPoint
import io.bincloud.common.io.transfer.SourcePoint
import io.bincloud.common.io.transfer.TransferingScheduler
import io.bincloud.common.io.transfer.Transmitter
import io.bincloud.storage.application.file.FileManagementService
import io.bincloud.storage.domain.model.file.File
import io.bincloud.storage.domain.model.file.FileDescriptor
import io.bincloud.storage.domain.model.file.FileNotExistException
import io.bincloud.storage.domain.model.file.FileRepository
import io.bincloud.storage.domain.model.file.FileStorage
import io.bincloud.storage.domain.model.file.FilesystemAccessor
import io.bincloud.storage.domain.model.file.File.IdGenerator
import io.bincloud.storage.domain.model.file.states.FileStatus
import spock.lang.Specification

class FileManagementServiceSpec extends Specification {
	private static final Instant TIMESTAMP_INITIAL_POINT = Instant.now()
	private static final Instant TIMESTAMP_NEXT_POINT = TIMESTAMP_INITIAL_POINT.plusMillis(1)
	private static final String FILE_ID = "12345"
	private static final Long FILE_SIZE = 100L

	private IdGenerator idGenerator;
	private SourcePoint source;
	private DestinationPoint destination;
	private FileRepository fileRepository;
	private FilesystemAccessor filesystemAccessor;
	private TransferingScheduler transferingScheduler;
	private CompletionCallback callback;
	private FileStorage fileStorage;

	def setup() {
		this.idGenerator = Stub(IdGenerator)
		this.source = Stub(SourcePoint)
		this.destination = Stub(DestinationPoint)
		this.fileRepository = Mock(FileRepository)
		this.filesystemAccessor = Mock(FilesystemAccessor)
		this.transferingScheduler = Mock(TransferingScheduler)
		this.callback = Mock(CompletionCallback)
		this.fileStorage = new FileManagementService(idGenerator, fileRepository, filesystemAccessor, transferingScheduler)
	}

	def "Scenario: file is successfully created"() {
		given: "The system generates unique id for new file"
		idGenerator.generateId() >> FILE_ID

		when: "The file creation is requested"
		def fileId = fileStorage.createNewFile()

		then: "The file should be stored to repository with returned id"
		File file;
		1 * fileRepository.save(_) >> {arguments -> file = arguments[0]}
		file.fileId == fileId

		and: "File state should be created"
		file.status == FileStatus.CREATED
	}

	def "Scenario: get file descriptor"() {
		given: "The distribution file is stored in the repository"
		fileRepository.findById(FILE_ID) >> Optional.of(createFile(FileStatus.DISTRIBUTION, FILE_SIZE))

		when: "The file descriptor is requested"
		Optional<FileDescriptor> descriptorOptional = fileStorage.getFileDescriptor(FILE_ID)

		then: "The non empty file descriptor should be returned"
		descriptorOptional.present == true
		FileDescriptor descriptor = descriptorOptional.get()
		
		and: "File descriptor state should be correspond to entity state"
		descriptor.creationMoment == TIMESTAMP_INITIAL_POINT
		descriptor.lastModification == TIMESTAMP_NEXT_POINT
		descriptor.status == FileStatus.DISTRIBUTION
		descriptor.size == FILE_SIZE
	}

	def "Scenario: file is successfully uploaded"() {
		given: "The created file is stored in the repository"
		fileRepository.findById(FILE_ID) >> Optional.of(createFile(FileStatus.CREATED, 0L))

		and: "There is access on write to the file on a filesystem"
		filesystemAccessor.getAccessOnWrite(FILE_ID) >> destination
		transferingScheduler.schedule(source, destination, callback) >> Stub(Transmitter) {
			start() >> {
				filesystemAccessor.getFileSize(FILE_ID) >> FILE_SIZE
				callback.onSuccess()
			}
		}

		when: "The file uploading is requested"
		fileStorage.uploadFile(FILE_ID, source, callback)

		then: "The file entity should be stored in repository after uploading"
		File storedFile;
		1 * fileRepository.save(_) >> {arguments -> storedFile = arguments[0]}

		and: "The file status should be changed to distribution"
		storedFile.status == FileStatus.DISTRIBUTION.name()

		and: "The file size should be changed to uploaded bytes count value"
		storedFile.size == FILE_SIZE

		and: "The completion callback should be called after completion"
		1 * callback.onSuccess()
	}
	
	def "Scenario: file is successfully downloaded"() {
		given: "The distribution file is stored in the repository"
		fileRepository.findById(FILE_ID) >> Optional.of(createFile(FileStatus.DISTRIBUTION, FILE_SIZE))
		
		and: "There is access on read from the file on a filesystem"
		filesystemAccessor.getAccessOnRead(FILE_ID, 0, FILE_SIZE) >> source
		transferingScheduler.schedule(source, destination, callback) >> Stub(Transmitter) {
			start() >> {
				callback.onSuccess()
			}
		}
		
		when: "The downloading is requested"
		fileStorage.downloadFile(FILE_ID, destination, callback)

		then: "The file should be requested from repository"
		1 * fileRepository.findById(FILE_ID)
		
		and: "The access on read should be requested for whole file"
		1 * filesystemAccessor.getAccessOnRead(FILE_ID, 0, FILE_SIZE)
		
		and: "Transferring should be scheduled and started"
		1 * transferingScheduler.schedule(source, destination, callback)
		
		and: "The callback should be called after completion"
		1 * callback.onSuccess()
	}
	
	def "Scenario: file range is successfully downloaded"() {
		given: "The distribution file is stored in the repository"
		fileRepository.findById(FILE_ID) >> Optional.of(createFile(FileStatus.DISTRIBUTION, FILE_SIZE))
		
		and: "There is access on read from the file on a filesystem"
		filesystemAccessor.getAccessOnRead(FILE_ID, 10, 20) >> source
		transferingScheduler.schedule(source, destination, callback) >> Stub(Transmitter) {
			start() >> {
				callback.onSuccess()
			}
		}
		
		when: "The downloading is requested"
		fileStorage.downloadFileRange(FILE_ID, destination, callback, 10, 20)

		then: "The file should be requested from repository"
		1 * fileRepository.findById(FILE_ID)
		
		and: "The access on read should be requested for the specified range only"
		1 * filesystemAccessor.getAccessOnRead(FILE_ID, 10, 20)
		
		and: "Transferring should be scheduled and started"
		1 * transferingScheduler.schedule(source, destination, callback)
		
		and: "The callback should be called after completion"
		1 * callback.onSuccess()
	}
	
	def "Scenario: file is successfully disposed"() {
		given: "The file is stored in the repository"
		fileRepository.findById(FILE_ID) >> Optional.of(createFile(FileStatus.DISTRIBUTION, FILE_SIZE))
		
		when: "The file disposition is requested"
		fileStorage.disposeFile(FILE_ID)
		
		then: "The file should be stored with disposed state"
		1 * fileRepository.save(_) >> {
			File storedFile = it[0]
			storedFile.status == FileStatus.DISPOSED.name()
		} 
	}
	
	def "Scenario: uploading is completed with file not exists error when file is unknown"() {
		given: "The file isn't stored in the repository"
		fileRepository.findById(_) >> Optional.empty()

		when: "The file uploading is requested"
		fileStorage.uploadFile(FILE_ID, source, callback)

		then: "The file not exist error should be sent to callback"
		1 * callback.onError(_) >> {
			ApplicationException error = it[0]
			error.context == FileNotExistException.CONTEXT
			error.errorCode == FileNotExistException.ERROR_CODE
			error.severity == Severity.INCIDENT
		}
	}
	
	def "Scenario: file downoading is completed with file not exists error when file is unknown"() {
		given: "The file isn't stored in the repository"
		fileRepository.findById(_) >> Optional.empty()
		
		when: "The file downloading is requested"
		fileStorage.downloadFile(FILE_ID, destination, callback)
		
		then: "The file not exist error should be sent to callback"
		1 * callback.onError(_) >> {
			ApplicationException error = it[0]
			error.context == FileNotExistException.CONTEXT
			error.errorCode == FileNotExistException.ERROR_CODE
			error.severity == Severity.INCIDENT
		}
	}
	
	def "Scenario: file range downoading is completed with file not exists error when file is unknown"() {
		given: "The file isn't stored in the repository"
		fileRepository.findById(_) >> Optional.empty()
		
		when: "The file downloading is requested"
		fileStorage.downloadFileRange(FILE_ID, destination, callback, 10, 20)
		
		then: "The file not exist error should be sent to callback"
		1 * callback.onError(_) >> {
			ApplicationException error = it[0]
			error.context == FileNotExistException.CONTEXT
			error.errorCode == FileNotExistException.ERROR_CODE
			error.severity == Severity.INCIDENT
		}
	}
	
	def "Scenario: file disposition is completed with file not exists error when file is unknown"() {
		given: "The file isn't stored in the repository"
		fileRepository.findById(_) >> Optional.empty()
		
		when: "The file disposition is requested"
		fileStorage.disposeFile(FILE_ID)
		
		then: "The file not exist error should be sent to callback"
		1 * callback.onError(_) >> {
			ApplicationException error = it[0]
			error.context == FileNotExistException.CONTEXT
			error.errorCode == FileNotExistException.ERROR_CODE
			error.severity == Severity.INCIDENT
		}
	}
	
	private def createFile(FileStatus status, Long initialSize) {
		return File.builder()
		.fileId(FILE_ID)
		.creationMoment(TIMESTAMP_INITIAL_POINT)
		.lastModification(TIMESTAMP_NEXT_POINT)
		.state(status.getFileState())
		.size(initialSize)
		.build()
	}
}
