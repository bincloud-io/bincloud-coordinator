package io.bcs.storage.domain.model

import java.time.Instant
import java.time.temporal.ChronoUnit

import io.bce.domain.errors.ApplicationException
import io.bce.domain.errors.ErrorDescriptor.ErrorSeverity
import io.bcs.common.domain.model.io.transfer.CompletionCallback
import io.bcs.common.domain.model.io.transfer.DestinationPoint
import io.bcs.common.domain.model.io.transfer.SourcePoint
import io.bcs.common.domain.model.io.transfer.TransferingScheduler
import io.bcs.storage.domain.model.FileRevision
import io.bcs.storage.domain.model.FilesystemAccessor
import io.bcs.storage.domain.model.contexts.FileDownloadingContext
import io.bcs.storage.domain.model.contexts.FileUploadingContext
import io.bcs.storage.domain.model.states.DisposedFileRevisionState
import io.bcs.storage.domain.model.states.FileHasAlreadyBeenDisposedException
import io.bcs.storage.domain.model.states.FileRevisionStatus
import spock.lang.Specification

class DisposedFileSpec extends Specification {
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

	def "Scenario: file can not be created in the disposed state"() {
		given: "The file in disposed state"
		FileRevision file =  createDisposedFile()

		when: "The file creation is requested"
		file.createFile(filesystem)

		then: "The file has been disposed should be thrown"
		ApplicationException error = thrown(FileHasAlreadyBeenDisposedException)
		error.getContextId() == FileHasAlreadyBeenDisposedException.CONTEXT
		error.getErrorCode() == FileHasAlreadyBeenDisposedException.ERROR_CODE
		error.getErrorSeverity() == ErrorSeverity.BUSINESS

		and: "The file status should not be changed"
		file.status == FileRevisionStatus.DISPOSED.name()
	}

	def "Scenario: file can not be uploaded in the disposed state"() {
		given: "The file in disposed state"
		FileRevision file =  createDisposedFile()

		when: "The file uploading is requested"
		file.uploadFileContent(createUploadingContext())

		then: "The file has been disposed should be thrown"
		ApplicationException error = thrown(FileHasAlreadyBeenDisposedException)
		error.getContextId() == FileHasAlreadyBeenDisposedException.CONTEXT
		error.getErrorCode() == FileHasAlreadyBeenDisposedException.ERROR_CODE
		error.getErrorSeverity() == ErrorSeverity.BUSINESS

		and: "The file status should not be changed"
		file.status == FileRevisionStatus.DISPOSED.name()
	}

	def "Scenario: file content can not be downloaded in the disposed state"() {
		given: "The file in disposed state"
		FileRevision file =  createDisposedFile()

		when: "The file downloading is requested"
		file.downloadFileContent(createDownloadingContext(), 0, 100)

		then: "The file has been disposed should be thrown"
		ApplicationException error = thrown(FileHasAlreadyBeenDisposedException)
		error.getContextId() == FileHasAlreadyBeenDisposedException.CONTEXT
		error.getErrorCode() == FileHasAlreadyBeenDisposedException.ERROR_CODE
		error.getErrorSeverity() == ErrorSeverity.BUSINESS

		and: "The file status should not be changed"
		file.status == FileRevisionStatus.DISPOSED.name()
	}

	def "Scenario: file distribution can not be started in the disposed state"() {
		given: "The file in disposed state"
		FileRevision file =  createDisposedFile()

		when: "The file distribution start is requested"
		file.startDistribution()

		then: "The file has been disposed should be thrown"
		ApplicationException error = thrown(FileHasAlreadyBeenDisposedException)
		error.getContextId() == FileHasAlreadyBeenDisposedException.CONTEXT
		error.getErrorCode() == FileHasAlreadyBeenDisposedException.ERROR_CODE
		error.getErrorSeverity() == ErrorSeverity.BUSINESS

		and: "The file status should not be changed"
		file.status == FileRevisionStatus.DISPOSED.name()
	}

	def createDownloadingContext() {
		return new FileDownloadingContext(Stub(DestinationPoint), Stub(TransferingScheduler), filesystem, Stub(CompletionCallback))
	}

	def createUploadingContext() {
		return new FileUploadingContext(1000L, Stub(SourcePoint), Stub(TransferingScheduler), filesystem, Stub(CompletionCallback))
	}

	def createDisposedFile() {
		return FileRevision.builder()
				.filesystemName(FILESYSTEM_NAME)
				.fileName(FILE_NAME)
				.mediaType(FILE_MEDIA_TYPE)
				.contentDisposition(FILE_DISPOSITION)
				.creationMoment(TIMESTAMP_INITIAL_POINT)
				.lastModification(TIMESTAMP_NEXT_POINT)
				.state(new DisposedFileRevisionState())
				.fileSize(0L)
				.build()
	}
}
