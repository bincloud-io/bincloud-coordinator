package io.bincloud.files.domain.model

import java.time.Instant
import java.time.temporal.ChronoUnit

import io.bincloud.common.domain.model.generator.SequentialGenerator
import io.bincloud.files.domain.model.File
import io.bincloud.files.domain.model.states.FileStatus
import spock.lang.Narrative
import spock.lang.Specification
import spock.lang.Unroll

@Narrative("""
	To represent business object \"File\" in the source code, 
	as a developer I'm needed in the component, which will do
	this. According to domain, I want a domain object which will 
	encapsulate domain object state and logic which works with this
	state.
""")
class FileSpec extends Specification {
	private static final String FILE_ID = "12345"
	private static final Instant TIMESTAMP_INITIAL_POINT = Instant.now()
	private static final Instant TIMESTAMP_NEXT_POINT = TIMESTAMP_INITIAL_POINT.plus(1, ChronoUnit.MILLIS)


	private SequentialGenerator<String> idGenerator;

	def setup() {
		this.idGenerator = Stub(SequentialGenerator)
	}

	def "Scenario: Create entity by constructor"() {
		given: "The id generator which generates unique file identifier"
		this.idGenerator.nextValue() >> FILE_ID

		when: "The file entity is created by the constructor"
		File file = new File(idGenerator)

		then: "The file status should be draft"
		file.status == FileStatus.DRAFT.name()

		and: "The file size should be zero"
		file.size == 0

		and: "The creation moment should be assigned"
		file.creationMoment != null

		and: "The last modification should be assigned"
		file.lastModification != null

		and: "The creation moment and last modification should be the same"
		file.creationMoment == file.lastModification
	}

	@Unroll
	def "Scenario: dispose file"() {
		given: "The #fileStatus.name() file"
		File file = File.builder()
				.fileId(FILE_ID)
				.creationMoment(TIMESTAMP_INITIAL_POINT)
				.lastModification(TIMESTAMP_NEXT_POINT)
				.state(fileStatus.getFileState())
				.size(0L)
				.build()

		when: "The file is disposed"
		Thread.sleep(10)
		file.dispose()

		then: "The file status should be disposed"
		file.status == FileStatus.DISPOSED.name()

		and: "The creation moment should be changed"
		file.lastModification != TIMESTAMP_NEXT_POINT

		where:
		fileStatus << [FileStatus.DRAFT, FileStatus.CREATED, FileStatus.DISTRIBUTION]
	}
}