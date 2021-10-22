package io.bcs.storage.application.management

import java.time.Instant
import java.time.temporal.ChronoUnit

import io.bcs.common.domain.model.generator.SequentialGenerator
import io.bcs.storage.application.management.FileManagementService
import io.bcs.storage.domain.model.FileRevision
import io.bcs.storage.domain.model.FileId
import io.bcs.storage.domain.model.FileRevisionRepository
import io.bcs.storage.domain.model.FilesystemAccessor
import io.bcs.storage.domain.model.contracts.FileManager
import io.bcs.storage.domain.model.states.DistributionFileRevisionState
import io.bcs.storage.domain.model.states.FileRevisionStatus
import spock.lang.Specification

class DisposeExistingFileFeatureSpec extends Specification {
	private static final String FILESYSTEM_NAME = "12345"
	private static final String FILE_NAME = "file.txt"
	private static final String FILE_MEDIA_TYPE = "application/media"
	private static final String FILE_DISPOSITION = "inline"
	private static final Long UPLOADED_FILE_SIZE = 1000000L
	private static final Instant TIMESTAMP_INITIAL_POINT = Instant.now()
	private static final Instant TIMESTAMP_NEXT_POINT = TIMESTAMP_INITIAL_POINT.plus(1, ChronoUnit.MILLIS)

	private FileRevisionRepository fileRepository
	private FilesystemAccessor filesystemAccessor;
	private FileManager fileManager

	def setup() {
		this.fileRepository = Mock(FileRevisionRepository)
		this.filesystemAccessor = Mock(FilesystemAccessor)
		this.fileManager = new FileManagementService(Stub(SequentialGenerator), fileRepository, filesystemAccessor)
	}
	
	def "Scenario: file is successfully disposed"() {
		FileRevision storedFile;
		given: "The file is stored in the repository"
		fileRepository.findById(FILESYSTEM_NAME) >> Optional.of(createDistributionFile())

		when: "The file disposition is requested"
		fileManager.disposeFile(new FileId(FILESYSTEM_NAME))

		then: "The file should be stored with disposed state"
		1 * fileRepository.save(_) >> {storedFile = it[0]}
		storedFile.getStatus() == FileRevisionStatus.DISPOSED.name()
		
		and: "The physical file should be removed from the filesystem"
		1 * filesystemAccessor.removeFile(FILESYSTEM_NAME)
	}

	def createDistributionFile() {
		return FileRevision.builder()
				.filesystemName(FILESYSTEM_NAME)
				.fileName(FILE_NAME)
				.mediaType(FILE_MEDIA_TYPE)
				.contentDisposition(FILE_DISPOSITION)
				.creationMoment(TIMESTAMP_INITIAL_POINT)
				.lastModification(TIMESTAMP_NEXT_POINT)
				.state(new DistributionFileRevisionState())
				.fileSize(1000L)
				.build()
	}
}
