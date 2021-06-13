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
import io.bincloud.files.domain.model.errors.FileHasAlreadyBeenUploadedException
import io.bincloud.files.domain.model.states.DistributionState
import io.bincloud.files.domain.model.states.FileStatus
import spock.lang.Specification

class DistributionFileSpec extends Specification {
	private static final String FILE_ID = "12345"
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

	def "Scenario: file can not be created in the distribution state"() {
		given: "The file in distribution state"
		File file =  createDistributionFile()

		when: "The file creation is requested"
		file.createFile(filesystem)

		then: "The file has been disposed should be thrown"
		ApplicationException error = thrown(FileAlreadyExistsException)
		error.context == FileAlreadyExistsException.CONTEXT
		error.errorCode == FileAlreadyExistsException.ERROR_CODE
		error.severity == Severity.BUSINESS

		and: "The file status should not be changed"
		file.status == FileStatus.DISTRIBUTION.name()
	}

	def "Scenario: file can not be uploaded in the distribution state"() {
		given: "The file in distribution state"
		File file =  createDistributionFile()

		when: "The file uploading is requested"
		file.uploadFile(createUploadingContext())

		then: "The file has been disposed should be thrown"
		ApplicationException error = thrown(FileHasAlreadyBeenUploadedException)
		error.context == FileHasAlreadyBeenUploadedException.CONTEXT
		error.errorCode == FileHasAlreadyBeenUploadedException.ERROR_CODE
		error.severity == Severity.BUSINESS

		and: "The file status should not be changed"
		file.status == FileStatus.DISTRIBUTION.name()
	}

	def "Scenario: whole file successfully downloaded"() {
		given: "The file in distribution state"
		File file =  createDistributionFile()

		and: "There is access on read to the file on a file system"
		1 * filesystem.getAccessOnRead(file.fileId, 0, file.size) >> sourcePoint

		and: "There is access on write to the file on a filesystem"
		filesystem.getAccessOnWrite(file.fileId) >> destinationPoint

		and: "The file transfering can be scheduled"
		scheduler.schedule(sourcePoint, destinationPoint, completionCallback) >> transmitter

		when: "The whole file downloading is requested"
		FileDownloadingContext downloadingContext = createDownloadingContext()
		file.downloadFile(downloadingContext)

		then: "The transmission should be started"
		1 * transmitter.start()

		and: "The modification time should be changed"
		file.lastModification == TIMESTAMP_NEXT_POINT

		and: "The status should not be changed"
		file.status == FileStatus.DISTRIBUTION.name()
	}

	def "Scenario: file range can not be downloaded in the disposed state"() {
		given: "The file in distribution state"
		File file =  createDistributionFile()

		and: "There is access on read to the file on a file system for range 10..20"
		1 * filesystem.getAccessOnRead(file.fileId, 10, 20) >> sourcePoint

		and: "There is access on write to the file on a filesystem"
		filesystem.getAccessOnWrite(file.fileId) >> destinationPoint

		and: "The file transfering can be scheduled"
		scheduler.schedule(sourcePoint, destinationPoint, completionCallback) >> transmitter

		when: "The file file range 10..20 downloading is requested"
		FileDownloadingContext downloadingContext = createDownloadingContext()
		file.downloadFileRange(downloadingContext, 10, 20)

		then: "The transmission should be started"
		1 * transmitter.start()

		and: "The modification time should be changed"
		file.lastModification == TIMESTAMP_NEXT_POINT

		and: "The status should not be changed"
		file.status == FileStatus.DISTRIBUTION.name()
	}

	def "Scenario: file distribution can not be started in the distribution state"() {
		given: "The disposed file"
		File file =  createDistributionFile()

		when: "The file distribution start is requested"
		file.startDistribution(filesystem)

		then: "The file has been disposed should be thrown"
		ApplicationException error = thrown(FileHasAlreadyBeenUploadedException)
		error.context == FileHasAlreadyBeenUploadedException.CONTEXT
		error.errorCode == FileHasAlreadyBeenUploadedException.ERROR_CODE
		error.severity == Severity.BUSINESS

		and: "The file status should not be changed"
		file.status == FileStatus.DISTRIBUTION.name()
	}

	private def createUploadingContext() {
		return new FileUploadingContext(sourcePoint, scheduler, filesystem, completionCallback)
	}

	private def createDownloadingContext() {
		return new FileDownloadingContext(destinationPoint, scheduler, filesystem, completionCallback)
	}

	private def createDistributionFile() {
		return File.builder()
				.fileId(FILE_ID)
				.creationMoment(TIMESTAMP_INITIAL_POINT)
				.lastModification(TIMESTAMP_NEXT_POINT)
				.state(new DistributionState())
				.size(0L)
				.build()
	}
}
