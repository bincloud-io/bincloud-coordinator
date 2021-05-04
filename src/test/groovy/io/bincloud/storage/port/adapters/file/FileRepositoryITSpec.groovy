package io.bincloud.storage.port.adapters.file

import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.COMPILE
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.RUNTIME
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.TEST

import java.time.temporal.ChronoUnit

import javax.inject.Inject

import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.shrinkwrap.api.Archive
import org.junit.runner.RunWith

import io.bincloud.common.domain.model.error.ApplicationException
import io.bincloud.common.domain.model.generator.SequentialGenerator
import io.bincloud.common.domain.model.logging.Loggers
import io.bincloud.common.domain.model.message.MessageTemplate
import io.bincloud.common.domain.model.time.DateTime
import io.bincloud.storage.domain.model.file.File
import io.bincloud.storage.domain.model.file.FileRepository
import io.bincloud.storage.domain.model.file.FileState
import io.bincloud.storage.domain.model.file.states.CreatedState
import io.bincloud.storage.domain.model.file.states.DisposedState
import io.bincloud.storage.domain.model.file.states.DistributionState
import io.bincloud.storage.domain.model.file.states.DraftState
import io.bincloud.storage.port.adapter.file.JPADateTimeConverter
import io.bincloud.storage.port.adapter.file.JPAFileRepository
import io.bincloud.storage.port.adapter.file.JPAFileStateConverter
import io.bincloud.testing.archive.ArchiveBuilder
import io.bincloud.testing.database.DatabaseConfigurer
import io.bincloud.testing.database.jdbc.cdi.JdbcLiquibase
import spock.lang.Specification

@RunWith(ArquillianSputnik)
class FileRepositoryITSpec extends Specification {
	private static final String FILE_ID = "12345-1--5735b394-05cc-4011-a952-c2f4950fbefa"
	private static final DateTime CREATION_MOMENT = new DateTime()
	private static final DateTime LAST_MODIFICATION = CREATION_MOMENT.plus(1, ChronoUnit.DAYS)

	@Deployment
	public static Archive "create deployment"() {
		return ArchiveBuilder.jar("file-repository-spec.jar")
				.resolveDependencies("pom.xml")
				.withScopes(COMPILE, RUNTIME, TEST)
				.resolveDependency("org.liquibase", "liquibase-core")
				.apply()
				.appendPackagesRecursively(File.getPackage().getName())
				.appendPackagesRecursively(DatabaseConfigurer.getPackage().getName())
				.appendPackagesRecursively(ApplicationException.getPackage().getName())
				.appendPackagesRecursively(Loggers.getPackage().getName())
				.appendPackagesRecursively(MessageTemplate.getPackage().getName())
				.appendClasses(SequentialGenerator, DateTime, JPAFileRepository, JPADateTimeConverter, JPAFileStateConverter, FileRepositoryITSpecConfig)
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
	private FileRepository fileRepository;

	def setup() {
		databaseConfigurer.setup("liquibase/master.changelog.xml")
	}

	def cleanup() {
		databaseConfigurer.tearDown()
	}

	def "Scenario: save file entity and obtain by id"() {
		given: "The file entity"
		File file = createFileForState(fileState, fileSize)

		when: "The file entity has been saved to the repository"
		fileRepository.save(file)

		and: "After that this file has been obtained by id"
		Optional<File> obtainedOptionalFile = fileRepository.findById(FILE_ID)

		then: "The file should be found into repository"
		obtainedOptionalFile.isPresent() == true

		and: "Their structure should be fully equivalent"
		File obtainedFile = obtainedOptionalFile.get()
		file == obtainedFile
		file.getCreationMoment() == obtainedFile.getCreationMoment()
		file.getLastModification() == obtainedFile.getLastModification()
		file.getStatus() == obtainedFile.getStatus()
		file.getSize() == obtainedFile.getSize()

		where:
		fileState               | fileSize
		new DraftState()        | 0L
		new CreatedState()      | 0L
		new DistributionState() | 40000000L
		new DisposedState()     | 40000000L
	}

	private File createFileForState(FileState fileState, Long fileSize) {
		return File.builder()
				.fileId(FILE_ID)
				.lastModification(LAST_MODIFICATION)
				.creationMoment(CREATION_MOMENT)
				.state(fileState)
				.size(fileSize)
				.build()
	}
}
