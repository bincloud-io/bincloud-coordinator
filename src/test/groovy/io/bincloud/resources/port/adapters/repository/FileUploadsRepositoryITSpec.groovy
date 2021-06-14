package io.bincloud.resources.port.adapters.repository

import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.COMPILE
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.RUNTIME
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.TEST

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.stream.Collectors

import javax.inject.Inject

import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.shrinkwrap.api.Archive
import org.junit.runner.RunWith

import io.bincloud.common.domain.model.error.ApplicationException
import io.bincloud.common.domain.model.generator.SequentialGenerator
import io.bincloud.common.domain.model.logging.Loggers
import io.bincloud.common.domain.model.message.MessageTemplate
import io.bincloud.common.port.adapters.time.JPADateTimeConverter
import io.bincloud.resources.domain.model.file.FileUpload
import io.bincloud.resources.domain.model.file.FileUploadsRepository
import io.bincloud.resources.port.adapter.repository.JPAFileUploadsRepository
import io.bincloud.testing.archive.ArchiveBuilder
import io.bincloud.testing.database.DatabaseConfigurer
import io.bincloud.testing.database.jdbc.cdi.JdbcLiquibase
import spock.lang.Specification

@RunWith(ArquillianSputnik)
class FileUploadsRepositoryITSpec extends Specification {
	private static final Long RESOURCE_ID = 1L
	private static final String FILE_ID_1 = "file_1"
	private static final String FILE_ID_2 = "file_2"
	private static final Instant UPLOAD_MOMENT_1 = Instant.now()
	private static final Instant UPLOAD_MOMENT_2 = UPLOAD_MOMENT_1.plus(1, ChronoUnit.SECONDS);

	@Deployment
	public static final Archive "create deployment"() {
		return ArchiveBuilder.jar("file-repository-spec.jar")
				.resolveDependencies("pom.xml")
				.withScopes(COMPILE, RUNTIME, TEST)
				.resolveDependency("org.liquibase", "liquibase-core")
				.apply()
				.appendPackagesRecursively(FileUpload.getPackage().getName())
				.appendPackagesRecursively(DatabaseConfigurer.getPackage().getName())
				.appendPackagesRecursively(ApplicationException.getPackage().getName())
				.appendPackagesRecursively(JPADateTimeConverter.getPackage().getName())
				.appendPackagesRecursively(Loggers.getPackage().getName())
				.appendPackagesRecursively(MessageTemplate.getPackage().getName())
				.appendClasses(SequentialGenerator,  JPAFileUploadsRepository, FileUploadsRepositoryITSpecConfig)
				.appendResource("liquibase")
				.appendResource("liquibase-test")
				.appendManifestResource("META-INF/beans.xml", "beans.xml")
				.appendManifestResource("jpa-test/file-uploads-mapping-persistence.xml", "persistence.xml")
				.appendManifestResource("META-INF/orm/file-uploads-mapping.xml", "orm/file-uploads-mapping.xml")
				.build()
	}


	@Inject
	@JdbcLiquibase
	private DatabaseConfigurer databaseConfigurer;

	@Inject
	private FileUploadsRepository fileUploadRepository;

	def setup() {
		databaseConfigurer.setup("liquibase/master.changelog.xml")
	}

	def cleanup() {
		databaseConfigurer.tearDown()
	}

	def "Scenario: save file upload and get by id"() {
		given: "The stored resources and files"
		databaseConfigurer.setup("liquibase-test/storage/FileUploadsRepositoryITSpec.changelog.xml")

		and: "The file upload"
		FileUpload fileupload = createFileUpload(FILE_ID_1, UPLOAD_MOMENT_1)

		when: "The file upload has been stored to the database"
		fileUploadRepository.save(fileupload)

		and: "The file upload has been requested by id"
		fileupload = fileUploadRepository.findById(fileupload.getResourceId(), fileupload.getFileId()).get()

		then: "The file upload should be obtained"
		fileupload.getFileId() == FILE_ID_1
		fileupload.getResourceId() == RESOURCE_ID
		truncate(fileupload.getUploadMoment()) == truncate(UPLOAD_MOMENT_1)
	}

	def "Scenario: get unexistent file upload by id"() {
		given: "The stored resources and files"
		databaseConfigurer.setup("liquibase-test/storage/FileUploadsRepositoryITSpec.changelog.xml")
		and: "There are no file uploads"

		when: "The file upload has been requested by id"
		Optional<FileUpload> fileupload = fileUploadRepository.findById(RESOURCE_ID, FILE_ID_1)

		then: "The file upload shouldn't be returned"
		fileupload.isPresent() == false
	}

	def "Scenario: save history and get latest"() {
		given: "The stored resources and files"
		databaseConfigurer.setup("liquibase-test/storage/FileUploadsRepositoryITSpec.changelog.xml")

		and: "The two file uploads for the same resource, but with different upload moments"
		FileUpload fileUploadFirst = createFileUpload(FILE_ID_1, UPLOAD_MOMENT_1)
		FileUpload fileUploadSecond = createFileUpload(FILE_ID_2, UPLOAD_MOMENT_2)

		when: "The file uploads has been stored to the database"
		fileUploadRepository.save(fileUploadFirst)
		fileUploadRepository.save(fileUploadSecond)

		and: "The latest upload has been requested"
		FileUpload obtainedFileupload = fileUploadRepository.findLatestResourceUpload(RESOURCE_ID).get()

		then: "The latest file upload should be obtained"
		obtainedFileupload.getFileId() == FILE_ID_2
		obtainedFileupload.getResourceId() == RESOURCE_ID
		truncate(obtainedFileupload.getUploadMoment()) == truncate(UPLOAD_MOMENT_2)
	}

	def "Scenario: get latest for unexsistent history"() {
		given: "The stored resources and files"
		databaseConfigurer.setup("liquibase-test/storage/FileUploadsRepositoryITSpec.changelog.xml")
		and: "There are no file uploads"

		when: "The latest file upload has been requested for resource"
		Optional<FileUpload> fileupload = fileUploadRepository.findLatestResourceUpload(RESOURCE_ID)

		then: "The file upload shouldn't be returned"
		fileupload.isPresent() == false
	}
	
	def "Scenario: get all file uploads history for existed resource"() {
		given: "The stored resources and files"
		databaseConfigurer.setup("liquibase-test/storage/FileUploadsRepositoryITSpec.changelog.xml")

		and: "The two file uploads for the same resource, but with different upload moments"
		FileUpload fileUploadFirst = createFileUpload(FILE_ID_1, UPLOAD_MOMENT_1)
		FileUpload fileUploadSecond = createFileUpload(FILE_ID_2, UPLOAD_MOMENT_2)

		when: "The file uploads has been stored to the database"
		fileUploadRepository.save(fileUploadFirst)
		fileUploadRepository.save(fileUploadSecond)
		
		then: "The file uploads history should be returned ordered by upload moments"
		fileUploadRepository.findAllResourceUploads(RESOURCE_ID)
			.map({upload -> 
				return String.format("%s-%s", upload.getResourceId(), upload.getFileId())
			})
			.collect(Collectors.toList()) == [
				"${RESOURCE_ID}-${FILE_ID_2}",
				"${RESOURCE_ID}-${FILE_ID_1}"
			] 
	}
	
	def "Scenario: get all file uploads history for unexistent history"() {
		given: "The stored resources and files"
		databaseConfigurer.setup("liquibase-test/storage/FileUploadsRepositoryITSpec.changelog.xml")
		and: "There are no file uploads"
		
		expect: "The empty history will returned"
		fileUploadRepository.findAllResourceUploads(RESOURCE_ID).count() == 0
	}

	def "Scenario: remove existing file upload"() {
		given: "The stored resources and files"
		databaseConfigurer.setup("liquibase-test/storage/FileUploadsRepositoryITSpec.changelog.xml")

		and: "There is file upload"
		fileUploadRepository.save(createFileUpload(FILE_ID_1, UPLOAD_MOMENT_1))

		when: "The existing resource remove command has been requested"
		fileUploadRepository.remove(RESOURCE_ID, FILE_ID_1)

		then: "The removed upload shouldn't be found"
		fileUploadRepository.findById(RESOURCE_ID, FILE_ID_1).isPresent() == false
	}

	def "Scenario: remove unknown file upload"() {
		given: "The stored resources and files"
		databaseConfigurer.setup("liquibase-test/storage/FileUploadsRepositoryITSpec.changelog.xml")

		when: "The not existing resource remove command has been requested"
		fileUploadRepository.remove(-1L, "UNKNOWN_FILE")

		then: "The nothing errors should be occurred"
		noExceptionThrown()
	}
	
	private Instant truncate(Instant source) {
		return source.truncatedTo(ChronoUnit.SECONDS);
	}

	private FileUpload createFileUpload(String fileId, Instant uploadMoment) {
		return FileUpload.builder()
				.resourceId(RESOURCE_ID)
				.fileId(fileId)
				.uploadMoment(uploadMoment)
				.build()
	}
}
