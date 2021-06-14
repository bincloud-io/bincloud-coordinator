package io.bincloud.files.domain.model

import java.time.Instant
import java.time.temporal.ChronoUnit

import io.bincloud.common.domain.model.error.ApplicationException
import io.bincloud.common.domain.model.error.ApplicationException.Severity
import io.bincloud.common.domain.model.io.transfer.CompletionCallback
import io.bincloud.common.domain.model.io.transfer.DestinationPoint
import io.bincloud.common.domain.model.io.transfer.SourcePoint
import io.bincloud.common.domain.model.io.transfer.TransferingScheduler
import io.bincloud.common.domain.model.io.transfer.Transmitter
import io.bincloud.files.domain.model.File
import io.bincloud.files.domain.model.FileDownloadingContext
import io.bincloud.files.domain.model.FileUploadingContext
import io.bincloud.files.domain.model.FilesystemAccessor
import io.bincloud.files.domain.model.errors.FileAlreadyExistsException
import io.bincloud.files.domain.model.errors.FileHasNotBeenUploadedException
import io.bincloud.files.domain.model.states.CreatedState
import io.bincloud.files.domain.model.states.FileStatus
import spock.lang.Specification

class CreatedFileSpec extends Specification {
	private static final String FILE_ID = "12345"
	private static final Long UPLOADED_FILE_SIZE = 1000000L
	private static final Instant TIMESTAMP_INITIAL_POINT = Instant.now()
	private static final Instant TIMESTAMP_NEXT_POINT = TIMESTAMP_INITIAL_POINT.plus(1, ChronoUnit.MILLIS)

	private SourcePoint sourcePoint
	private DestinationPoint destinationPoint
	private FilesystemAccessor filesystem
	private TransferingScheduler scheduler
	private CompletionCallback completionCallback;
	private Transmitter transmitter;

	def setup() {
		this.sourcePoint = Stub(SourcePoint)
		this.destinationPoint = Stub(DestinationPoint)
		this.filesystem = Mock(FilesystemAccessor)
		this.scheduler = Mock(TransferingScheduler)
		this.completionCallback = Stub(CompletionCallback)
		this.transmitter = Mock(Transmitter)
	}

	def "Scenario: file successfully uploaded"() {
		given: "The file in created state"
		File file = createFileInCreatedState()

		and: "There is access on write to the file on a filesystem"
		filesystem.getAccessOnWrite(file.fileId) >> destinationPoint

		and: "The file transfering can be scheduled"
		scheduler.schedule(sourcePoint, destinationPoint, completionCallback) >> transmitter

		when: "The file uploading is requested"
		FileUploadingContext uploadingContext = createUploadingContext()
		file.uploadFile(uploadingContext)

		then: "The transmission should be started"
		1 * transmitter.start()

		and: "The modification time should be changed"
		file.lastModification == TIMESTAMP_NEXT_POINT

		and: "The status should not be changed"
		file.status == FileStatus.CREATED.name()
	}

	def "Scenario: file distribution successfully started"() {
		given: "The file in created state"
		File file = createFileInCreatedState()

		and: "The uploaded file has non zero file size"
		filesystem.getFileSize(file.fileId) >> UPLOADED_FILE_SIZE

		when: "The file distribion start was requested"
		Thread.sleep(10)
		file.startDistribution(filesystem)

		then: "The file size should be changed to distribution"
		file.status == FileStatus.DISTRIBUTION.name()
		
		and: "The modification time should be changed"
		file.lastModification != TIMESTAMP_NEXT_POINT
	}

	def "Scenario: file can not be created in the created state"() {
		given: "The file in created state"
		File file = createFileInCreatedState()

		when: "The creation is requested"
		FileDownloadingContext downloadingContext = createDownloadingContext() 
		file.createFile(filesystem)

		then: "The file already exists error should be thrown"
		ApplicationException error = thrown(FileAlreadyExistsException)
		error.context == FileAlreadyExistsException.CONTEXT
		error.errorCode == FileAlreadyExistsException.ERROR_CODE
		error.severity == Severity.BUSINESS
	}
	
	

	def "Scenario: file can not be downloaded in the created state"() {
		given: "The file in created state"
		File file = createFileInCreatedState()

		when: "The downloading is requested"
		FileDownloadingContext downloadingContext = createDownloadingContext()
		file.downloadFile(downloadingContext)

		then: "The file has not been uploaded error should be thrown"
		ApplicationException error = thrown(FileHasNotBeenUploadedException)
		error.context == FileHasNotBeenUploadedException.CONTEXT
		error.errorCode == FileHasNotBeenUploadedException.ERROR_CODE
		error.severity == Severity.BUSINESS
	}
	
	def "Scenario: file range can not be downloaded in the created state"() {
		given: "The file in created state"
		File file = createFileInCreatedState()

		when: "The downloading is requested"
		FileDownloadingContext downloadingContext = new FileDownloadingContext(destinationPoint, scheduler, filesystem, completionCallback)
		file.downloadFileRange(downloadingContext, 0, 10)

		then: "The file has not been uploaded error should be thrown"
		ApplicationException error = thrown(FileHasNotBeenUploadedException)
		error.context == FileHasNotBeenUploadedException.CONTEXT
		error.errorCode == FileHasNotBeenUploadedException.ERROR_CODE
		error.severity == Severity.BUSINESS
	}

	private def createUploadingContext() {
		return new FileUploadingContext(sourcePoint, scheduler, filesystem, completionCallback)
	}
	
	private def createDownloadingContext() {
		return new FileDownloadingContext(destinationPoint, scheduler, filesystem, completionCallback)
	}
	
	private def createFileInCreatedState() {
		return File.builder()
				.fileId(FILE_ID)
				.creationMoment(TIMESTAMP_INITIAL_POINT)
				.lastModification(TIMESTAMP_NEXT_POINT)
				.state(new CreatedState())
				.size(0L)
				.build()
	}
}
