package io.bcs.storage.domain.model

import java.time.Instant
import java.time.temporal.ChronoUnit

import io.bcs.common.domain.model.generator.SequentialGenerator
import io.bcs.storage.domain.model.FileRevision
import io.bcs.storage.domain.model.FilesystemAccessor
import io.bcs.storage.domain.model.contracts.FileDescriptor
import io.bcs.storage.domain.model.contracts.upload.FileAttributes
import io.bcs.storage.domain.model.states.FileRevisionStatus
import spock.lang.Narrative
import spock.lang.Specification
import spock.lang.Unroll

class FileSpec extends Specification {
	private static final String FILE_REVISION_NAME = "12345"
	private static final String FILE_NAME = "file.txt"
	private static final String FILE_MEDIA_TYPE = "application/media"
	private static final String FILE_DISPOSITION = "inline"
	private static final Instant TIMESTAMP_INITIAL_POINT = Instant.now()
	private static final Instant TIMESTAMP_NEXT_POINT = TIMESTAMP_INITIAL_POINT.plus(1, ChronoUnit.MILLIS)


	private SequentialGenerator<String> filesystemNameGenerator;
	private FilesystemAccessor filesystemAccessor;
	private FileAttributes fileAttributes;
	
	def setup() {
		this.filesystemNameGenerator = Stub(SequentialGenerator)
		this.fileAttributes = Stub(FileAttributes)
		this.fileAttributes.getFileName() >> FILE_NAME
		this.fileAttributes.getMediaType() >> FILE_MEDIA_TYPE
		this.fileAttributes.getContentDisposition() >> FILE_DISPOSITION
		this.filesystemAccessor = Mock(FilesystemAccessor)
	}

	def "Scenario: Create entity by constructor"() {
		given: "The filesystem name generator which generates unique file name on a filesystem"
		this.filesystemNameGenerator.nextValue() >> new FileId(FILE_REVISION_NAME)

		when: "The file entity is created by the constructor"
		FileRevision file = new FileRevision(filesystemNameGenerator, fileAttributes)
		FileDescriptor fileDescriptor = file.getDescriptor()
		
		then: "The file status should be draft"
		fileDescriptor.getStatus() == FileRevisionStatus.DRAFT.name()

		and: "The file size should be zero"
		fileDescriptor.getFileSize() == 0L

		and: "The creation moment should be assigned"
		fileDescriptor.creationMoment != null

		and: "The last modification should be assigned"
		fileDescriptor.lastModification != null

		and: "The creation moment and last modification should be the same"
		fileDescriptor.creationMoment == file.lastModification
				
		and: "The filesystem name should be generated"
		fileDescriptor.getRevisionName() == FILE_REVISION_NAME
		
		and: "The file name should be got from file attributes"
		fileDescriptor.getFileName() == FILE_NAME
		
		and: "The media type should be got from file attributes"
		fileDescriptor.getMediaType() == FILE_MEDIA_TYPE
		
		and: "The content disposition should be got from file attributes"
		fileDescriptor.getContentDisposition() == FILE_DISPOSITION
	}

	@Unroll
	def "Scenario: dispose file"() {
		given: "The #fileStatus.name() file"
		FileRevision file = FileRevision.builder()
				.revisionName(new FileId(FILE_REVISION_NAME))
				.fileName(FILE_NAME)
				.mediaType(FILE_MEDIA_TYPE)
				.contentDisposition(FILE_DISPOSITION)
				.creationMoment(TIMESTAMP_INITIAL_POINT)
				.lastModification(TIMESTAMP_NEXT_POINT)
				.state(fileStatus.getFileState())
				.fileSize(0L)
				.build()
		FileDescriptor fileDescriptor = file.getDescriptor()

		when: "The file is disposed"
		Thread.sleep(10)
		file.dispose(filesystemAccessor)

		then: "The file status should be disposed"
		fileDescriptor.status == FileRevisionStatus.DISPOSED.name()

		and: "The creation moment should be changed"
		fileDescriptor.lastModification != TIMESTAMP_NEXT_POINT
		
		and: "The file should be removed form filesystem"
		1 * filesystemAccessor.removeFile(FILE_REVISION_NAME)

		where:
		fileStatus << [FileRevisionStatus.DRAFT, FileRevisionStatus.CREATED, FileRevisionStatus.DISTRIBUTION]
	}
}
