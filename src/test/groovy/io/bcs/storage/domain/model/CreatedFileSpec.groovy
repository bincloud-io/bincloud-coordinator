package io.bcs.storage.domain.model

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.CountDownLatch

import io.bce.domain.errors.ApplicationException
import io.bce.domain.errors.ErrorDescriptor.ErrorSeverity
import io.bce.promises.Promise
import io.bce.promises.Promises
import io.bcs.common.domain.model.io.transfer.CompletionCallback
import io.bcs.common.domain.model.io.transfer.DestinationPoint
import io.bcs.common.domain.model.io.transfer.SourcePoint
import io.bcs.common.domain.model.io.transfer.TransferingScheduler
import io.bcs.common.domain.model.io.transfer.Transmitter
import io.bcs.storage.domain.model.FileRevision
import io.bcs.storage.domain.model.FilesystemAccessor
import io.bcs.storage.domain.model.FileRevision.ContentUploader
import io.bcs.storage.domain.model.FileRevision.ContentUploader.UploadedContent
import io.bcs.storage.domain.model.contexts.FileDownloadingContext
import io.bcs.storage.domain.model.contexts.FileUploadingContext
import io.bcs.storage.domain.model.contracts.FileDescriptor
import io.bcs.storage.domain.model.states.CreatedFileRevisionState
import io.bcs.storage.domain.model.states.FileAlreadyExistsException
import io.bcs.storage.domain.model.states.FileHasNotBeenUploadedException
import io.bcs.storage.domain.model.states.FileRevisionStatus
import spock.lang.Specification

class CreatedFileSpec extends Specification {
	private static final String FILE_REVISION_NAME = "12345"
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
		FileDescriptor fileDescriptor
		CountDownLatch latch = new CountDownLatch(1)
		
		given: "The file in created state"
		FileRevision file = createFileInCreatedState(0L)

		and: "The content uploader, transferring ${UPLOADED_FILE_SIZE} bytes"
		ContentUploader contentUploader = Mock(ContentUploader) {
			upload(file.getRevisionName().getFilesystemName()) >> {
				return Promises.of({ deferred ->  
					deferred.resolve(new UploadedContent() {
						@Override
						public Long getSize() {
							return UPLOADED_FILE_SIZE;
						}
					})
				})
			}
		}

		when: "The file uploading is requested"
		file.uploadContent(contentUploader).then({ response -> 
			fileDescriptor = response 
			latch.countDown()
		})
		
		latch.await()

		then: "The data streaming should be completed successfully "				
		and: "The modification time should be changed"
		fileDescriptor.getLastModification() == TIMESTAMP_NEXT_POINT

		and: "The status should not be changed"
		fileDescriptor.getStatus() == FileRevisionStatus.CREATED.name()
		
		and: "The filesystem name should be generated"
		fileDescriptor.getRevisionName() == FILE_REVISION_NAME
		
		and: "The file name should be got from file attributes"
		fileDescriptor.getFileName() == FILE_NAME
		
		and: "The media type should be got from file attributes"
		fileDescriptor.getMediaType() == FILE_MEDIA_TYPE
		
		and: "The content disposition should be got from file attributes"
		fileDescriptor.getContentDisposition() == FILE_DISPOSITION
	}

	def "Scenario: file distribution successfully started"() {
		given: "The file in created state"
		FileRevision file = createFileInCreatedState(UPLOADED_FILE_SIZE)
		
		when: "The file distribion start was requested"
		Thread.sleep(10)
		file.startDistribution()
		FileDescriptor fileDescriptor = file.getDescriptor();

		then: "The file size should be changed to distribution"
		fileDescriptor.getStatus() == FileRevisionStatus.DISTRIBUTION.name()
		
		and: "The modification time should be changed"
		fileDescriptor.getLastModification() != TIMESTAMP_NEXT_POINT
		
		and: "The filesystem name should be generated"
		fileDescriptor.getRevisionName() == FILE_REVISION_NAME
		
		and: "The file name should be got from file attributes"
		fileDescriptor.getFileName() == FILE_NAME
		
		and: "The media type should be got from file attributes"
		fileDescriptor.getMediaType() == FILE_MEDIA_TYPE
		
		and: "The content disposition should be got from file attributes"
		fileDescriptor.getContentDisposition() == FILE_DISPOSITION
	}

	def "Scenario: file can not be created on a filesysten in the created state"() {
		given: "The file in created state"
		FileRevision file = createFileInCreatedState(0L)

		when: "The creation is requested"
		FileDownloadingContext downloadingContext = createDownloadingContext() 
		file.createFile(filesystem)

		then: "The file already exists error should be thrown"
		ApplicationException error = thrown(FileAlreadyExistsException)
		error.getContextId() == FileAlreadyExistsException.CONTEXT
		error.getErrorCode() == FileAlreadyExistsException.ERROR_CODE
		error.getErrorSeverity() == ErrorSeverity.BUSINESS
	}
	
	def "Scenario: file content can not be downloaded in the created state"() {
		given: "The file in created state"
		FileRevision file = createFileInCreatedState(UPLOADED_FILE_SIZE)

		when: "The downloading is requested"
		FileDownloadingContext downloadingContext = new FileDownloadingContext(destinationPoint, scheduler, filesystem, completionCallback)
		file.downloadFileContent(downloadingContext, 0, 10)

		then: "The file has not been uploaded error should be thrown"
		ApplicationException error = thrown(FileHasNotBeenUploadedException)
		error.getContextId() == FileHasNotBeenUploadedException.CONTEXT
		error.getErrorCode() == FileHasNotBeenUploadedException.ERROR_CODE
		error.getErrorSeverity() == ErrorSeverity.BUSINESS
	}

	private def createDownloadingContext() {
		return new FileDownloadingContext(destinationPoint, scheduler, filesystem, completionCallback)
	}

	def createFileInCreatedState(Long fileSize) {
		return FileRevision.builder()
				.revisionName(new FileId(FILE_REVISION_NAME))
				.fileName(FILE_NAME)
				.mediaType(FILE_MEDIA_TYPE)
				.contentDisposition(FILE_DISPOSITION)
				.creationMoment(TIMESTAMP_INITIAL_POINT)
				.lastModification(TIMESTAMP_NEXT_POINT)
				.state(new CreatedFileRevisionState())
				.fileSize(fileSize)
				.build()
	}
}
