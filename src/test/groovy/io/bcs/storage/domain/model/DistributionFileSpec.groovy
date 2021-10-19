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
import io.bcs.storage.domain.model.errors.FileHasAlreadyBeenUploadedException
import io.bcs.storage.domain.model.states.CreatedState
import io.bcs.storage.domain.model.states.DistributionState
import io.bcs.storage.domain.model.states.FileStatus
import spock.lang.Specification

class DistributionFileSpec extends Specification {
	private static final String FILESYSTEM_NAME = "12345"
	private static final String FILE_NAME = "file.txt"
	private static final String FILE_MEDIA_TYPE = "application/media"
	private static final String FILE_DISPOSITION = "inline"
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
		file.uploadFileContent(createUploadingContext())

		then: "The file has been disposed should be thrown"
		ApplicationException error = thrown(FileHasAlreadyBeenUploadedException)
		error.context == FileHasAlreadyBeenUploadedException.CONTEXT
		error.errorCode == FileHasAlreadyBeenUploadedException.ERROR_CODE
		error.severity == Severity.BUSINESS

		and: "The file status should not be changed"
		file.status == FileStatus.DISTRIBUTION.name()
	}

	def "Scenario: file content can not be downloaded in the disposed state"() {
		given: "The file in distribution state"
		File file =  createDistributionFile()

		and: "There is access on read to the file on a file system for range 10..20"
		1 * filesystem.getAccessOnRead(file.getFilesystemName(), 10, 20) >> sourcePoint

		and: "The file transfering can be scheduled"
		scheduler.schedule(sourcePoint, destinationPoint, _) >> {
			CompletionCallback callback = it[2]
			return Stub(Transmitter) {
				start() >> {
					callback.onSuccess()
				}
			}
		}

		when: "The file content 10..20 downloading is requested"
		FileDownloadingContext downloadingContext = createDownloadingContext()
		file.downloadFileContent(downloadingContext, 10, 20)

		then: "The transmission should be completed"
		1 * completionCallback.onSuccess()
		
		and: "The modification time should be changed"
		file.lastModification == TIMESTAMP_NEXT_POINT

		and: "The status should not be changed"
		file.status == FileStatus.DISTRIBUTION.name()
		
		and: "The filesystem name should be generated"
		file.getFilesystemName() == FILESYSTEM_NAME
		
		and: "The file name should be got from file attributes"
		file.getFileName() == FILE_NAME
		
		and: "The media type should be got from file attributes"
		file.getMediaType() == FILE_MEDIA_TYPE
		
		and: "The content disposition should be got from file attributes"
		file.getContentDisposition() == FILE_DISPOSITION
	}

	def "Scenario: file distribution can not be started in the distribution state"() {
		given: "The disposed file"
		File file =  createDistributionFile()

		when: "The file distribution start is requested"
		file.startDistribution()

		then: "The file has been disposed should be thrown"
		ApplicationException error = thrown(FileHasAlreadyBeenUploadedException)
		error.context == FileHasAlreadyBeenUploadedException.CONTEXT
		error.errorCode == FileHasAlreadyBeenUploadedException.ERROR_CODE
		error.severity == Severity.BUSINESS

		and: "The file status should not be changed"
		file.status == FileStatus.DISTRIBUTION.name()
	}

	private def createUploadingContext() {
		return new FileUploadingContext(1000L, sourcePoint, scheduler, filesystem, completionCallback)
	}

	private def createDownloadingContext() {
		return new FileDownloadingContext(destinationPoint, scheduler, filesystem, completionCallback)
	}

	def createDistributionFile() {
		return File.builder()
				.filesystemName(FILESYSTEM_NAME)
				.fileName(FILE_NAME)
				.mediaType(FILE_MEDIA_TYPE)
				.contentDisposition(FILE_DISPOSITION)
				.creationMoment(TIMESTAMP_INITIAL_POINT)
				.lastModification(TIMESTAMP_NEXT_POINT)
				.state(new DistributionState())
				.fileSize(0L)
				.build()
	}
}
