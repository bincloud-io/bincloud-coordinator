package io.bincloud.files.domain.model

import java.time.Instant
import java.time.temporal.ChronoUnit

import io.bincloud.common.domain.model.error.ApplicationException.Severity
import io.bincloud.common.domain.model.io.transfer.CompletionCallback
import io.bincloud.common.domain.model.io.transfer.DestinationPoint
import io.bincloud.common.domain.model.io.transfer.SourcePoint
import io.bincloud.common.domain.model.io.transfer.TransferingScheduler
import io.bincloud.files.domain.model.contracts.upload.FileAttributes
import io.bincloud.files.domain.model.errors.FileManagementException
import io.bincloud.files.domain.model.errors.FileDoesNotExistException
import io.bincloud.files.domain.model.states.DraftState
import io.bincloud.files.domain.model.states.FileStatus
import spock.lang.Specification

class DraftFileSpec extends Specification {
	private static final String FILESYSTEM_NAME = "12345"
	private static final String FILE_NAME = "file.txt"
	private static final String FILE_MEDIA_TYPE = "application/media"
	private static final String FILE_DISPOSITION = "inline"
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
		1 * filesystem.createFile(FILESYSTEM_NAME)

		and: "The file entity status should be changed to created"
		file.status == FileStatus.CREATED.name()

		and: "The last modification time should be updated"
		file.lastModification != TIMESTAMP_INITIAL_POINT
		
		and: "The filesystem name should be generated"
		file.getFilesystemName() == FILESYSTEM_NAME
		
		and: "The file name should be got from file attributes"
		file.getFileName() == FILE_NAME
		
		and: "The media type should be got from file attributes"
		file.getMediaType() == FILE_MEDIA_TYPE
		
		and: "The content disposition should be got from file attributes"
		file.getContentDisposition() == FILE_DISPOSITION
	}

	def "Scenario: file can not be uploaded in the draft state"() {
		given: "The draft file"
		File file = createInitialFile()

		when: "The file uploading is requested"
		file.uploadFileContent(createDummyUploadContext())

		then: "The file not exists error has been thrown"
		FileDoesNotExistException thrownError = thrown(FileDoesNotExistException)
		thrownError.context == FileManagementException.CONTEXT
		thrownError.errorCode == FileDoesNotExistException.ERROR_CODE
		thrownError.severity == Severity.BUSINESS
	}

	def "Scenario: file can not be downloaded in the draft state"() {
		given: "The draft file"
		File file = createInitialFile()

		when: "The file downloading is requested"
		file.downloadFileContent(createDummyDownloadContext(), 0, 100)

		then: "The file not exists error should be thrown"
		FileDoesNotExistException thrownError = thrown(FileDoesNotExistException)
		thrownError.context == FileManagementException.CONTEXT
		thrownError.errorCode == FileDoesNotExistException.ERROR_CODE
		thrownError.severity == Severity.BUSINESS
	}

	def "Scenario: file distribution can not be started in the draft state"() {
		given: "The draft file"
		File file = createInitialFile()

		when: "The file distribution start is requested"
		file.startDistribution()

		then: "The file not exists error should be thrown"
		FileDoesNotExistException thrownError = thrown(FileDoesNotExistException)
		thrownError.context == FileManagementException.CONTEXT
		thrownError.errorCode == FileDoesNotExistException.ERROR_CODE
		thrownError.severity == Severity.BUSINESS
	}

	def createDummyDownloadContext() {
		return new FileDownloadingContext(Stub(DestinationPoint), Stub(TransferingScheduler), filesystem, Stub(CompletionCallback))
	}

	def createDummyUploadContext() {
		return new FileUploadingContext(1000L, Stub(SourcePoint), Stub(TransferingScheduler), filesystem, Stub(CompletionCallback))
	}

	def createInitialFile() {
		return File.builder()
				.filesystemName(FILESYSTEM_NAME)
				.fileName(FILE_NAME)
				.mediaType(FILE_MEDIA_TYPE)
				.contentDisposition(FILE_DISPOSITION)
				.creationMoment(TIMESTAMP_INITIAL_POINT)
				.lastModification(TIMESTAMP_INITIAL_POINT)
				.state(new DraftState())
				.fileSize(0L)
				.build()
	}
}
