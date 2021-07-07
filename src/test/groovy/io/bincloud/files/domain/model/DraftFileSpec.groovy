package io.bincloud.files.domain.model

import java.time.Instant
import java.time.temporal.ChronoUnit

import io.bincloud.common.domain.model.error.ApplicationException.Severity
import io.bincloud.common.domain.model.io.transfer.CompletionCallback
import io.bincloud.common.domain.model.io.transfer.DestinationPoint
import io.bincloud.common.domain.model.io.transfer.SourcePoint
import io.bincloud.common.domain.model.io.transfer.TransferingScheduler
import io.bincloud.files.domain.model.File
import io.bincloud.files.domain.model.FileDownloadingContext
import io.bincloud.files.domain.model.FileUploadingContext
import io.bincloud.files.domain.model.FilesystemAccessor
import io.bincloud.files.domain.model.errors.FileManagementException
import io.bincloud.files.domain.model.errors.FileNotExistException
import io.bincloud.files.domain.model.states.DraftState
import io.bincloud.files.domain.model.states.FileStatus
import spock.lang.Specification

class DraftFileSpec extends Specification {
	private static final String FILE_ID = "12345"
	private static final Instant TIMESTAMP_INITIAL_POINT = Instant.now()
	private static final Instant TIMESTAMP_NEXT_POINT = TIMESTAMP_INITIAL_POINT.plus(1, ChronoUnit.MILLIS)
	private FilesystemAccessor filesystem;

	def setup() {
		this.filesystem = Mock(FilesystemAccessor)
	}


	def "Scenario: new file successfully created in the filesystem"() {
		given: "The draft file"
		File file = createInitialFile()

		when: "The file creation in the filesystem is requested"
		file.createFile(filesystem)

		then: "The creation request should be delegated to the filesystem"
		1 * filesystem.createFile(FILE_ID)

		and: "The file entity status should be changed to created"
		file.status == FileStatus.CREATED.name()

		and: "The last modification time should be updated"
		file.lastModification != TIMESTAMP_INITIAL_POINT
	}

	def "Scenario: file can not be uploaded in the draft state"() {
		given: "The draft file"
		File file = createInitialFile()

		when: "The file uploading is requested"
		file.uploadFile(createDummyUploadingContext())

		then: "The file not exists error has been thrown"
		FileNotExistException thrownError = thrown(FileNotExistException)
		thrownError.context == FileManagementException.CONTEXT
		thrownError.errorCode == FileNotExistException.ERROR_CODE
		thrownError.severity == Severity.INCIDENT
	}

	def "Scenario: file can not be downloaded in the draft state"() {
		given: "The draft file"
		File file = createInitialFile()

		when: "The file downloading is requested"
		file.downloadFileContent(createDummyDownloadingContext(), 0, 100)

		then: "The file not exists error should be thrown"
		FileNotExistException thrownError = thrown(FileNotExistException)
		thrownError.context == FileManagementException.CONTEXT
		thrownError.errorCode == FileNotExistException.ERROR_CODE
		thrownError.severity == Severity.INCIDENT
	}

	def "Scenario: file distribution can not be started in the draft state"() {
		given: "The draft file"
		File file = createInitialFile()

		when: "The file distribution start is requested"
		file.startDistribution(filesystem)

		then: "The file not exists error should be thrown"
		FileNotExistException thrownError = thrown(FileNotExistException)
		thrownError.context == FileManagementException.CONTEXT
		thrownError.errorCode == FileNotExistException.ERROR_CODE
		thrownError.severity == Severity.INCIDENT
	}

	def createDummyDownloadingContext() {
		return new FileDownloadingContext(Stub(DestinationPoint), Stub(TransferingScheduler), filesystem, Stub(CompletionCallback))
	}

	def createDummyUploadingContext() {
		return new FileUploadingContext(Stub(SourcePoint), Stub(TransferingScheduler), filesystem, Stub(CompletionCallback))
	}

	def createInitialFile() {
		return File.builder()
				.fileId(FILE_ID)
				.creationMoment(TIMESTAMP_INITIAL_POINT)
				.lastModification(TIMESTAMP_INITIAL_POINT)
				.state(new DraftState())
				.size(0L)
				.build()
	}
}
