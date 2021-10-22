package io.bcs.storage.port.adapters.repository

import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.COMPILE
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.RUNTIME
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.TEST

import java.time.Instant
import java.time.temporal.ChronoUnit

import javax.inject.Inject

import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.shrinkwrap.api.Archive
import org.junit.runner.RunWith

import io.bce.domain.errors.ApplicationException
import io.bce.text.TextTemplate
import io.bcs.common.domain.model.generator.SequentialGenerator
import io.bcs.common.domain.model.logging.Loggers
import io.bcs.common.port.adapters.time.JPADateTimeConverter
import io.bcs.storage.domain.model.FileRevisionRepository
import io.bcs.storage.domain.model.FileRevision
import io.bcs.storage.domain.model.FileRevision.FileRevisionState
import io.bcs.storage.domain.model.states.CreatedFileRevisionState
import io.bcs.storage.domain.model.states.DisposedFileRevisionState
import io.bcs.storage.domain.model.states.DistributionFileRevisionState
import io.bcs.storage.domain.model.states.DraftFileRevisionState
import io.bcs.storage.port.adapter.file.repository.JPAFileRepository
import io.bcs.storage.port.adapter.file.repository.JPAFileRevisionStateConverter
import io.bcs.testing.archive.ArchiveBuilder
import io.bcs.testing.database.DatabaseConfigurer
import io.bcs.testing.database.jdbc.cdi.JdbcLiquibase
import spock.lang.Specification

@RunWith(ArquillianSputnik)
class FileRepositoryITSpec extends Specification {
	private static final String FILE_ID = "12345-1--5735b394-05cc-4011-a952-c2f4950fbefa"
	private static final Instant CREATION_MOMENT = Instant.now()
	private static final Instant LAST_MODIFICATION = CREATION_MOMENT.plus(1, ChronoUnit.DAYS)

	@Deployment
	public static Archive "create deployment"() {
		return ArchiveBuilder.jar("file-repository-spec.jar")
				.resolveDependencies("pom.xml")
				.withScopes(COMPILE, RUNTIME, TEST)
				.resolveDependency("org.liquibase", "liquibase-core")
				.apply()
				.appendPackagesRecursively(FileRevision.getPackage().getName())
				.appendPackagesRecursively(DatabaseConfigurer.getPackage().getName())
				.appendPackagesRecursively(ApplicationException.getPackage().getName())
				.appendPackagesRecursively(Loggers.getPackage().getName())
				.appendPackagesRecursively(TextTemplate.getPackage().getName())
				.appendClasses(JPADateTimeConverter)
				.appendClasses(SequentialGenerator,  JPAFileRepository, JPAFileRevisionStateConverter,  FileRepositoryITSpecConfig)
				.appendResource("liquibase")
				.appendManifestResource("META-INF/beans.xml", "beans.xml")
				.appendManifestResource("jpa-test/file-mapping-persistence.xml", "persistence.xml")
				.appendManifestResource("META-INF/orm/file-mapping.xml", "orm/file-mapping.xml")
				.build()
	}

	@Inject
	@JdbcLiquibase
	private DatabaseConfigurer databaseConfigurer;

	@Inject
	private FileRevisionRepository fileRepository;

	def setup() {
		databaseConfigurer.setup("liquibase/master.changelog.xml")
	}

	def cleanup() {
		databaseConfigurer.tearDown()
	}

	def "Scenario: save file entity and obtain by id"() {
		given: "The file entity"
		FileRevision file = createFileForState(fileState, fileSize)

		when: "The file entity has been saved to the repository"
		fileRepository.save(file)

		and: "After that this file has been obtained by id"
		Optional<FileRevision> obtainedOptionalFile = fileRepository.findById(FILE_ID)

		then: "The file should be found into repository"
		obtainedOptionalFile.isPresent() == true

		and: "Their structure should be fully equivalent"
		FileRevision obtainedFile = obtainedOptionalFile.get()
		file == obtainedFile
		truncate(file.getCreationMoment()) == truncate(obtainedFile.getCreationMoment())
		truncate(file.getLastModification()) == truncate(obtainedFile.getLastModification())
		file.getStatus() == obtainedFile.getStatus()
		file.getSize() == obtainedFile.getSize()

		where:
		fileState               | fileSize
		new DraftFileRevisionState()        | 0L
		new CreatedFileRevisionState()      | 0L
		new DistributionFileRevisionState() | 40000000L
		new DisposedFileRevisionState()     | 40000000L
	}
	
	private Instant truncate(Instant source) {
		return source.truncatedTo(ChronoUnit.SECONDS);
	}

	private FileRevision createFileForState(FileRevisionState fileState, Long fileSize) {
		return FileRevision.builder()
				.fileId(FILE_ID)
				.lastModification(LAST_MODIFICATION)
				.creationMoment(CREATION_MOMENT)
				.state(fileState)
				.size(fileSize)
				.build()
	}
}
