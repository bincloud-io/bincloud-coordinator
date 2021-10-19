package io.bcs.storage.domain.model

import java.time.Instant
import java.time.temporal.ChronoUnit

import io.bcs.common.domain.model.error.ApplicationException
import io.bcs.common.domain.model.error.ApplicationException.Severity
import io.bcs.common.domain.model.io.transfer.CompletionCallback
import io.bcs.common.domain.model.io.transfer.DestinationPoint
import io.bcs.common.domain.model.io.transfer.SourcePoint
import io.bcs.common.domain.model.io.transfer.TransferingScheduler
import io.bcs.common.domain.model.io.transfer.Transmitter
import io.bcs.storage.domain.model.File
import io.bcs.storage.domain.model.FileDownloadingContext
import io.bcs.storage.domain.model.FileUploadingContext
import io.bcs.storage.domain.model.FilesystemAccessor
import io.bcs.storage.domain.model.errors.FileAlreadyExistsException
import io.bcs.storage.domain.model.errors.FileHasNotBeenUploadedException
import io.bcs.storage.domain.model.states.CreatedState
import io.bcs.storage.domain.model.states.FileStatus
import spock.lang.Specification

class CreatedFileSpec extends Specification {
	private static final String FILESYSTEM_NAME = "12345"
	private static final String FILE_NAME = "file.txt"
	private static final String FILE_MEDIA_TYPE = "application/media"
	private static final String FILE_DISPOSITION = "inline"
	private static final Long UPLOADED_FILE_SIZE = 1000000L
	private static final Instant TIMESTAMP_INITIAL_POINT = Instant.now()
	private static final Instant TIMESTAMP_NEXT_POINT = TIMESTAMP_INITIAL_POINT.plus(1, ChronoUnit.MILLIS)

	private SourcePoint sourcePoint
	private DestinationPoint destinationPoint
	private FilesystemAccessor filesystem
	private TransferingScheduler scheduler
	private CompletionCallback completionCallback;

	def setup() {
		this.sourcePoint = Stub(SourcePoint)
		this.destinationPoint = Stub(DestinationPoint)
		this.filesystem = Mock(FilesystemAccessor)
		this.scheduler = Mock(TransferingScheduler)
		this.completionCallback = Mock(CompletionCallback)
	}

	def "Scenario: file successfully uploaded"() {
		given: "The file in created state"
		File file = createFileInCreatedState(0L)

		and: "There is access on write to the file on a filesystem"
		filesystem.getAccessOnWrite(file.getFilesystemName(), UPLOADED_FILE_SIZE) >> destinationPoint

		and: "The file transfering can be scheduled"
		scheduler.schedule(sourcePoint, destinationPoint, _) >> {
			CompletionCallback callback = it[2]
			return Stub(Transmitter) {
				start() >> {
					callback.onSuccess()
				}
			}
		}

		when: "The file uploading is requested"
		FileUploadingContext uploadingContext = createUploadingContext(UPLOADED_FILE_SIZE)
		file.uploadFileContent(uploadingContext)

		then: "The transmission should be started"
		1 * completionCallback.onSuccess()
		
		and: "The modification time should be changed"
		file.lastModification == TIMESTAMP_NEXT_POINT

		and: "The status should not be changed"
		file.status == FileStatus.CREATED.name()
		
		and: "The filesystem name should be generated"
		file.getFilesystemName() == FILESYSTEM_NAME
		
		and: "The file name should be got from file attributes"
		file.getFileName() == FILE_NAME
		
		and: "The media type should be got from file attributes"
		file.getMediaType() == FILE_MEDIA_TYPE
		
		and: "The content disposition should be got from file attributes"
		file.getContentDisposition() == FILE_DISPOSITION
	}

	def "Scenario: file distribution successfully started"() {
		given: "The file in created state"
		File file = createFileInCreatedState(UPLOADED_FILE_SIZE)
		
		when: "The file distribion start was requested"
		Thread.sleep(10)
		file.startDistribution()

		then: "The file size should be changed to distribution"
		file.status == FileStatus.DISTRIBUTION.name()
		
		and: "The modification time should be changed"
		file.lastModification != TIMESTAMP_NEXT_POINT
		
		and: "The filesystem name should be generated"
		file.getFilesystemName() == FILESYSTEM_NAME
		
		and: "The file name should be got from file attributes"
		file.getFileName() == FILE_NAME
		
		and: "The media type should be got from file attributes"
		file.getMediaType() == FILE_MEDIA_TYPE
		
		and: "The content disposition should be got from file attributes"
		file.getContentDisposition() == FILE_DISPOSITION
	}

	def "Scenario: file can not be created on a filesysten in the created state"() {
		given: "The file in created state"
		File file = createFileInCreatedState(0L)

		when: "The creation is requested"
		FileDownloadingContext downloadingContext = createDownloadingContext() 
		file.createFile(filesystem)

		then: "The file already exists error should be thrown"
		ApplicationException error = thrown(FileAlreadyExistsException)
		error.context == FileAlreadyExistsException.CONTEXT
		error.errorCode == FileAlreadyExistsException.ERROR_CODE
		error.severity == Severity.BUSINESS
	}
	
	def "Scenario: file content can not be downloaded in the created state"() {
		given: "The file in created state"
		File file = createFileInCreatedState(UPLOADED_FILE_SIZE)

		when: "The downloading is requested"
		FileDownloadingContext downloadingContext = new FileDownloadingContext(destinationPoint, scheduler, filesystem, completionCallback)
		file.downloadFileContent(downloadingContext, 0, 10)

		then: "The file has not been uploaded error should be thrown"
		ApplicationException error = thrown(FileHasNotBeenUploadedException)
		error.context == FileHasNotBeenUploadedException.CONTEXT
		error.errorCode == FileHasNotBeenUploadedException.ERROR_CODE
		error.severity == Severity.BUSINESS
	}

	private def createUploadingContext(Long contentLength) {
		return new FileUploadingContext(contentLength, sourcePoint, scheduler, filesystem, completionCallback)
	}
	
	private def createDownloadingContext() {
		return new FileDownloadingContext(destinationPoint, scheduler, filesystem, completionCallback)
	}

	def createFileInCreatedState(Long fileSize) {
		return File.builder()
				.filesystemName(FILESYSTEM_NAME)
				.fileName(FILE_NAME)
				.mediaType(FILE_MEDIA_TYPE)
				.contentDisposition(FILE_DISPOSITION)
				.creationMoment(TIMESTAMP_INITIAL_POINT)
				.lastModification(TIMESTAMP_NEXT_POINT)
				.state(new CreatedState())
				.fileSize(fileSize)
				.build()
	}
}
