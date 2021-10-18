package io.bincloud.files.domain.model

import java.time.Instant
import java.time.temporal.ChronoUnit

import io.bincloud.common.domain.model.generator.SequentialGenerator
import io.bincloud.files.domain.model.contracts.upload.FileAttributes
import io.bincloud.files.domain.model.states.FileStatus
import spock.lang.Narrative
import spock.lang.Specification
import spock.lang.Unroll

class FileSpec extends Specification {
	private static final String FILESYSTEM_NAME = "12345"
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
		this.filesystemNameGenerator.nextValue() >> FILESYSTEM_NAME

		when: "The file entity is created by the constructor"
		File file = new File(filesystemNameGenerator, fileAttributes)

		then: "The file status should be draft"
		file.status == FileStatus.DRAFT.name()

		and: "The file size should be zero"
		file.getFileSize() == 0L

		and: "The creation moment should be assigned"
		file.creationMoment != null

		and: "The last modification should be assigned"
		file.lastModification != null

		and: "The creation moment and last modification should be the same"
		file.creationMoment == file.lastModification
				
		and: "The filesystem name should be generated"
		file.getFilesystemName() == FILESYSTEM_NAME
		
		and: "The file name should be got from file attributes"
		file.getFileName() == FILE_NAME
		
		and: "The media type should be got from file attributes"
		file.getMediaType() == FILE_MEDIA_TYPE
		
		and: "The content disposition should be got from file attributes"
		file.getContentDisposition() == FILE_DISPOSITION
	}

	@Unroll
	def "Scenario: dispose file"() {
		given: "The #fileStatus.name() file"
		File file = File.builder()
				.filesystemName(FILESYSTEM_NAME)
				.fileName(FILE_NAME)
				.mediaType(FILE_MEDIA_TYPE)
				.contentDisposition(FILE_DISPOSITION)
				.creationMoment(TIMESTAMP_INITIAL_POINT)
				.lastModification(TIMESTAMP_NEXT_POINT)
				.state(fileStatus.getFileState())
				.fileSize(0L)
				.build()

		when: "The file is disposed"
		Thread.sleep(10)
		file.dispose(filesystemAccessor)

		then: "The file status should be disposed"
		file.status == FileStatus.DISPOSED.name()

		and: "The creation moment should be changed"
		file.lastModification != TIMESTAMP_NEXT_POINT
		
		and: "The file should be removed form filesystem"
		1 * filesystemAccessor.removeFile(FILESYSTEM_NAME)

		where:
		fileStatus << [FileStatus.DRAFT, FileStatus.CREATED, FileStatus.DISTRIBUTION]
	}
}
