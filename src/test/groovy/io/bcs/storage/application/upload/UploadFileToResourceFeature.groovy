package io.bcs.storage.application.upload

import java.time.Instant
import java.time.temporal.ChronoUnit

import io.bcs.common.domain.model.error.ApplicationException
import io.bcs.common.domain.model.error.ApplicationException.Severity
import io.bcs.common.domain.model.io.transfer.CompletionCallback
import io.bcs.common.domain.model.io.transfer.DestinationPoint
import io.bcs.common.domain.model.io.transfer.SourcePoint
import io.bcs.common.domain.model.io.transfer.TransferingScheduler
import io.bcs.common.domain.model.io.transfer.Transmitter
import io.bcs.storage.application.upload.FileUploadService
import io.bcs.storage.domain.model.File
import io.bcs.storage.domain.model.FileRepository
import io.bcs.storage.domain.model.FileState
import io.bcs.storage.domain.model.FilesystemAccessor
import io.bcs.storage.domain.model.contracts.FileDescriptor
import io.bcs.storage.domain.model.contracts.FilePointer
import io.bcs.storage.domain.model.contracts.upload.FileUploadListener
import io.bcs.storage.domain.model.contracts.upload.FileUploader
import io.bcs.storage.domain.model.errors.FileDoesNotExistException
import io.bcs.storage.domain.model.errors.FileHasAlreadyBeenDisposedException
import io.bcs.storage.domain.model.errors.FileHasAlreadyBeenUploadedException
import io.bcs.storage.domain.model.errors.UnspecifiedFilesystemNameException
import io.bcs.storage.domain.model.states.CreatedState
import io.bcs.storage.domain.model.states.DisposedState
import io.bcs.storage.domain.model.states.DistributionState
import spock.lang.Specification

class UploadFileToResourceFeature extends Specification {
	private static final String FILE_NAME = "file.txt"
	private static final String FILE_MEDIA_TYPE = "application/media"
	private static final String FILE_DISPOSITION = "inline"
	private static final String FILESYSTEM_NAME = "12345"
	private static final Instant CREATION_MOMENT = Instant.now()
	private static final Instant LAST_MODIFICATION = CREATION_MOMENT.plus(1, ChronoUnit.MINUTES)
	private static final Long FILE_SIZE = 100L

	private SourcePoint source
	private DestinationPoint destination
	private FileRepository fileRepository
	private FilesystemAccessor filesystemAccessor
	private TransferingScheduler transferringScheduler
	private FileUploadListener uploadListener
	private FileUploader fileUploader

	def setup() {
		this.source = Stub(SourcePoint)
		this.destination = Stub(DestinationPoint)
		this.filesystemAccessor = Stub(FilesystemAccessor)
		this.fileRepository = Mock(FileRepository)
		this.uploadListener = Mock(FileUploadListener)
		this.transferringScheduler = Mock(TransferingScheduler)
		this.fileUploader = new FileUploadService(fileRepository, filesystemAccessor, transferringScheduler)
	}

//	def "Scenario: upload content to the file with unspecified resource id"() {
//		ApplicationException error;
//		given: "The file pointer"
//		FilePointer filePointer = createFilePointer(Optional.empty())
//
//		when: "The file upload is requested"
//		fileUploader.uploadFileContent(filePointer, FILE_SIZE, source, uploadListener)
//
//		then: "The wrong file pointer format error should be passed to the upload listener"
//		1 * uploadListener.onError(_) >> {error = it[0]}
//		error.getSeverity() == Severity.BUSINESS
//		error.getContext() == Constants.CONTEXT
//		error.getErrorCode() == UnspecifiedResourceException.ERROR_CODE
//	}

	def "Scenario: upload content to the file with unspecified fliesystem name"() {
		ApplicationException error;
		given: "The file pointer"
		FilePointer filePointer = createFilePointer(Optional.empty())

		when: "The file upload is requested"
		fileUploader.uploadFileContent(filePointer, FILE_SIZE, source, uploadListener)

		then: "The wrong file pointer format error should be passed to the upload listener"
		1 * uploadListener.onError(_) >> {error = it[0]}
		error.getSeverity() == Severity.BUSINESS
		error.getContext() == UnspecifiedFilesystemNameException.CONTEXT
		error.getErrorCode() == UnspecifiedFilesystemNameException.ERROR_CODE
	}

	def "Scenario: upload content to the unknown resource id"() {
		ApplicationException error;
		given: "The file pointer"
		FilePointer filePointer = createFilePointer(Optional.of(FILESYSTEM_NAME))

		and: "The file with this id doesn't exist in the repository"
		fileRepository.findById(FILESYSTEM_NAME) >> Optional.empty()

		when: "The file upload is requested"
		fileUploader.uploadFileContent(filePointer, FILE_SIZE, source, uploadListener)

		then: "The wrong file pointer format error should be passed to the upload listener"
		1 * uploadListener.onError(_) >> {error = it[0]}
		error.getSeverity() == Severity.BUSINESS
		error.getContext() == FileDoesNotExistException.CONTEXT
		error.getErrorCode() == FileDoesNotExistException.ERROR_CODE
	}

	def "Scenario: file is successfuly uploaded to the existing resource"() {
		File storedFile;
		FileDescriptor fileDescriptor;
		given: "The file pointer"
		FilePointer filePointer = createFilePointer(Optional.of(FILESYSTEM_NAME))

		and: "The file, containing into repository, has the created state"
		fileRepository.findById(FILESYSTEM_NAME) >> Optional.of(createFile(new CreatedState(), FILE_SIZE))

		and: "There is access on write to the filesystem"
		filesystemAccessor.getAccessOnWrite(FILESYSTEM_NAME, FILE_SIZE) >> destination

		and: "The file content transferring is completed successfully"
		initSuccessfulTransferring();

		when: "The file upload is requested"
		fileUploader.uploadFileContent(filePointer, FILE_SIZE, source, uploadListener)

		then: "Upload listener should be successfully completed with received file descriptor"
		1 * uploadListener.onUpload(_) >> {fileDescriptor = it[0]}

		and: "File should be stored to the repository"
		1 * fileRepository.save(_) >> {storedFile = it[0]}

		and: "The file size should be updated to the requested size"
		storedFile.getFileSize() == FILE_SIZE

		and: "The file descriptor state should be corresponded to file state"
		fileDescriptor.getFilesystemName() == storedFile.getFilesystemName()
		fileDescriptor.getStatus() == storedFile.getStatus()
		fileDescriptor.getFileName() == storedFile.getFileName()
		fileDescriptor.getMediaType() == storedFile.getMediaType()
		fileDescriptor.getContentDisposition() == storedFile.getContentDisposition()
		fileDescriptor.getCreationMoment() == storedFile.getCreationMoment()
		fileDescriptor.getLastModification() == storedFile.getLastModification()
		fileDescriptor.getFileSize() == storedFile.getFileSize()
	}

	def "Scenario: upload content to the not acceptable file state"() {
		ApplicationException error;
		given: "The file pointer"
		FilePointer filePointer = createFilePointer(Optional.of(FILESYSTEM_NAME))

		and: "The file, containing into repository, has the wrong state"
		fileRepository.findById(FILESYSTEM_NAME) >> Optional.of(createFile(fileState, FILE_SIZE))

		and: "There is access on write to the filesystem"
		filesystemAccessor.getAccessOnWrite(FILESYSTEM_NAME, FILE_SIZE) >> destination

		and: "The file content transferring is completed successfully"
		initSuccessfulTransferring();

		when: "The file upload is requested"
		fileUploader.uploadFileContent(filePointer, FILE_SIZE, source, uploadListener)

		then: "Upload listener should be completed with error"
		1 * uploadListener.onError(_) >> {error = it[0]}
		error.getSeverity() == errorSeverity
		error.getContext() == errorContext
		error.getErrorCode() == errorCode

		and: "File shouldn't be stored to the repository"
		0 * fileRepository.save(_)
		
		where:
		fileState                 | errorSeverity      | errorContext                                | errorCode
		new DistributionState()   | Severity.BUSINESS  | FileHasAlreadyBeenUploadedException.CONTEXT | FileHasAlreadyBeenUploadedException.ERROR_CODE
		new DisposedState()       | Severity.BUSINESS  | FileHasAlreadyBeenDisposedException.CONTEXT | FileHasAlreadyBeenDisposedException.ERROR_CODE
		     
	}

	def "Scenario: error during getting access to the filesystem"() {
		Exception error = new RuntimeException("ERROR");
		given: "The file pointer"
		FilePointer filePointer = createFilePointer(Optional.of(FILESYSTEM_NAME))

		and: "The file, containing into repository, has the created state"
		fileRepository.findById(FILESYSTEM_NAME) >> Optional.of(createFile(new CreatedState(), FILE_SIZE))

		and: "There isn't access on write to the filesystem"
		filesystemAccessor.getAccessOnWrite(FILESYSTEM_NAME, FILE_SIZE) >> {throw error}

		when: "The file upload is requested"
		fileUploader.uploadFileContent(filePointer, FILE_SIZE, source, uploadListener)

		then: "Upload listener should be completed with error"
		1 * uploadListener.onError(error)
	}
	
	def "Scenario: error during file content transferring"() {
		Exception error = new Exception("ERROR");
		given: "The file pointer"
		FilePointer filePointer = createFilePointer(Optional.of(FILESYSTEM_NAME))

		and: "The file, containing into repository, has the created state"
		fileRepository.findById(FILESYSTEM_NAME) >> Optional.of(createFile(new CreatedState(), FILE_SIZE))

		and: "There is access on write to the filesystem"
		filesystemAccessor.getAccessOnWrite(FILESYSTEM_NAME, FILE_SIZE) >> destination

		and: "The file content transferring is completed successfully"
		initErrorTransferring(error)

		when: "The file upload is requested"
		fileUploader.uploadFileContent(filePointer, FILE_SIZE, source, uploadListener)

		then: "Upload listener should be completed with error"
		1 * uploadListener.onError(error)
	}

	private void initSuccessfulTransferring() {
		transferringScheduler.schedule(_, _, _) >> { arguments ->
			CompletionCallback callback = arguments[2]
			return Stub(Transmitter) {
				start() >> {
					callback.onSuccess()
				}
			}
		}
	}

	private void initErrorTransferring(Exception error) {
		transferringScheduler.schedule(_, _, _) >> { arguments ->
			CompletionCallback callback = arguments[2]
			return Stub(Transmitter) {
				start() >> {
					callback.onError(error)
				}
			}
		}
	}

	private FilePointer createFilePointer(Optional<String> filesystemName) {
		FilePointer filePointer = Stub(FilePointer)
		filePointer.getFilesystemName() >> filesystemName
		return filePointer
	}

	private File createFile(FileState fileState, Long fileSize) {
		return File.builder()
				.filesystemName(FILESYSTEM_NAME)
				.fileName(FILE_NAME)
				.mediaType(FILE_MEDIA_TYPE)
				.contentDisposition(FILE_DISPOSITION)
				.creationMoment(CREATION_MOMENT)
				.lastModification(LAST_MODIFICATION)
				.state(fileState)
				.fileSize(fileSize)
				.build()
	}
}
