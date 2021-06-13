package io.bincloud.files.domain.model

import java.time.Instant
import java.time.temporal.ChronoUnit

import io.bincloud.common.domain.model.error.ApplicationException
import io.bincloud.common.domain.model.error.ApplicationException.Severity
import io.bincloud.common.domain.model.io.transfer.CompletionCallback
import io.bincloud.common.domain.model.io.transfer.DestinationPoint
import io.bincloud.common.domain.model.io.transfer.SourcePoint
import io.bincloud.common.domain.model.io.transfer.TransferingScheduler
import io.bincloud.files.domain.model.File
import io.bincloud.files.domain.model.FileDownloadingContext
import io.bincloud.files.domain.model.FileUploadingContext
import io.bincloud.files.domain.model.FilesystemAccessor
import io.bincloud.files.domain.model.errors.FileHasAlreadyBeenDisposedException
import io.bincloud.files.domain.model.states.DisposedState
import io.bincloud.files.domain.model.states.FileStatus
import spock.lang.Specification

class DisposedFileSpec extends Specification {
	private static final String FILE_ID = "12345"
	private static final Instant TIMESTAMP_INITIAL_POINT = Instant.now()
	private static final Instant TIMESTAMP_NEXT_POINT = TIMESTAMP_INITIAL_POINT.plus(1, ChronoUnit.MILLIS)
	private FilesystemAccessor filesystem;

	def setup() {
		this.filesystem = Mock(FilesystemAccessor)
	}

	def "Scenario: file can not be created in the disposed state"() {
		given: "The file in disposed state"
		File file =  createDisposedFile()

		when: "The file creation is requested"
		file.createFile(filesystem)

		then: "The file has been disposed should be thrown"
		ApplicationException error = thrown(FileHasAlreadyBeenDisposedException)
		error.context == FileHasAlreadyBeenDisposedException.CONTEXT
		error.errorCode == FileHasAlreadyBeenDisposedException.ERROR_CODE
		error.severity == Severity.BUSINESS

		and: "The file status should not be changed"
		file.status == FileStatus.DISPOSED.name()
	}

	def "Scenario: file can not be uploaded in the disposed state"() {
		given: "The file in disposed state"
		File file =  createDisposedFile()

		when: "The file uploading is requested"
		file.uploadFile(createUploadingContext())

		then: "The file has been disposed should be thrown"
		ApplicationException error = thrown(FileHasAlreadyBeenDisposedException)
		error.context == FileHasAlreadyBeenDisposedException.CONTEXT
		error.errorCode == FileHasAlreadyBeenDisposedException.ERROR_CODE
		error.severity == Severity.BUSINESS

		and: "The file status should not be changed"
		file.status == FileStatus.DISPOSED.name()
	}

	def "Scenario: file can not be downloaded in the disposed state"() {
		given: "The file in disposed state"
		File file =  createDisposedFile()

		when: "The file downloading is requested"
		file.downloadFile(createDownloadingContext())

		then: "The file has been disposed should be thrown"
		ApplicationException error = thrown(FileHasAlreadyBeenDisposedException)
		error.context == FileHasAlreadyBeenDisposedException.CONTEXT
		error.errorCode == FileHasAlreadyBeenDisposedException.ERROR_CODE
		error.severity == Severity.BUSINESS

		and: "The file status should not be changed"
		file.status == FileStatus.DISPOSED.name()
	}

	def "Scenario: file range can not be downloaded in the disposed state"() {
		given: "The file in disposed state"
		File file =  createDisposedFile()

		when: "The file downloading is requested"
		file.downloadFileRange(createDownloadingContext(), 0, 100)

		then: "The file has been disposed should be thrown"
		ApplicationException error = thrown(FileHasAlreadyBeenDisposedException)
		error.context == FileHasAlreadyBeenDisposedException.CONTEXT
		error.errorCode == FileHasAlreadyBeenDisposedException.ERROR_CODE
		error.severity == Severity.BUSINESS

		and: "The file status should not be changed"
		file.status == FileStatus.DISPOSED.name()
	}

	def "Scenario: file distribution can not be started in the disposed state"() {
		given: "The file in disposed state"
		File file =  createDisposedFile()

		when: "The file distribution start is requested"
		file.startDistribution(filesystem)

		then: "The file has been disposed should be thrown"
		ApplicationException error = thrown(FileHasAlreadyBeenDisposedException)
		error.context == FileHasAlreadyBeenDisposedException.CONTEXT
		error.errorCode == FileHasAlreadyBeenDisposedException.ERROR_CODE
		error.severity == Severity.BUSINESS

		and: "The file status should not be changed"
		file.status == FileStatus.DISPOSED.name()
	}

	def createDownloadingContext() {
		return new FileDownloadingContext(Stub(DestinationPoint), Stub(TransferingScheduler), filesystem, Stub(CompletionCallback))
	}

	def createUploadingContext() {
		return new FileUploadingContext(Stub(SourcePoint), Stub(TransferingScheduler), filesystem, Stub(CompletionCallback))
	}

	private def createDisposedFile() {
		return File.builder()
				.fileId(FILE_ID)
				.creationMoment(TIMESTAMP_INITIAL_POINT)
				.lastModification(TIMESTAMP_NEXT_POINT)
				.state(new DisposedState())
				.size(0L)
				.build()
	}
}
