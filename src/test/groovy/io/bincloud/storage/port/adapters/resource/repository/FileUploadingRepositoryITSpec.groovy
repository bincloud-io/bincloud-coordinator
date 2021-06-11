package io.bincloud.storage.port.adapters.resource.repository

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
import io.bincloud.common.port.adapters.time.JPADateTimeConverter
import io.bincloud.storage.domain.model.resource.file.FileUploading
import io.bincloud.storage.domain.model.resource.file.FileUploadingId
import io.bincloud.storage.domain.model.resource.file.FileUploadingRepository
import io.bincloud.storage.port.adapter.resource.repository.JPAFileUploadingRepository
import io.bincloud.storage.port.adapter.resource.repository.JPAResourceRepository
import io.bincloud.testing.archive.ArchiveBuilder
import io.bincloud.testing.database.DatabaseConfigurer
import io.bincloud.testing.database.jdbc.cdi.JdbcLiquibase
import spock.lang.Specification

@RunWith(ArquillianSputnik)
class FileUploadingRepositoryITSpec extends Specification {
	private static final Long RESOURCE_ID = 1L
	private static final String FILE_ID_1 = "file_1"
	private static final String FILE_ID_2 = "file_2"
	private static final DateTime UPLOADING_MOMENT_1 = new DateTime();
	private static final DateTime UPLOADING_MOMENT_2 = UPLOADING_MOMENT_1.plus(1, ChronoUnit.SECONDS);

	@Deployment
	public static final Archive "create deployment"() {
		return ArchiveBuilder.jar("file-repository-spec.jar")
		.resolveDependencies("pom.xml")
		.withScopes(COMPILE, RUNTIME, TEST)
		.resolveDependency("org.liquibase", "liquibase-core")
		.apply()
		.appendPackagesRecursively(FileUploading.getPackage().getName())
		.appendPackagesRecursively(DatabaseConfigurer.getPackage().getName())
		.appendPackagesRecursively(ApplicationException.getPackage().getName())
		.appendPackagesRecursively(DateTime.getPackage().getName())
		.appendPackagesRecursively(JPADateTimeConverter.getPackage().getName())
		.appendPackagesRecursively(Loggers.getPackage().getName())
		.appendPackagesRecursively(MessageTemplate.getPackage().getName())
		.appendClasses(SequentialGenerator,  JPAFileUploadingRepository, FileUploadingRepositoryITSpecConfig)
		.appendResource("liquibase")
		.appendResource("liquibase-test")
		.appendManifestResource("META-INF/beans.xml", "beans.xml")
		.appendManifestResource("jpa-test/file-upoading-mapping-persistence.xml", "persistence.xml")
		.appendManifestResource("META-INF/orm/file-uploading-mapping.xml", "orm/file-uploading-mapping.xml")
		.build()
	}
	
	
	@Inject
	@JdbcLiquibase
	private DatabaseConfigurer databaseConfigurer;

	@Inject
	private FileUploadingRepository fileUploadingRepository;

	def setup() {
		databaseConfigurer.setup("liquibase/master.changelog.xml")
		databaseConfigurer.setup("liquibase-test/storage/FileUploadingRepositoryITSpec.changelog.xml")
	}

	def cleanup() {
		databaseConfigurer.tearDown()
	}

	def "Scenario: save file uploading and get by id"() {
		given: "The stored resources and files"
		databaseConfigurer.setup("liquibase-test/storage/FileUploadingRepositoryITSpec.changelog.xml")

		and: "The file uploading"
		FileUploading fileUploading = createFileUploading(FILE_ID_1, UPLOADING_MOMENT_1)

		when: "The file uploading has been stored to the database"
		fileUploadingRepository.save(fileUploading)

		and: "The file uploading has been requested by id"
		fileUploading = fileUploadingRepository.findById(fileUploading.getResourceId(), fileUploading.getFileId()).get()

		then: "The file uploading should be obtained"
		fileUploading.getFileId() == FILE_ID_1
		fileUploading.getResourceId() == RESOURCE_ID
		fileUploading.getUploadingMoment() == UPLOADING_MOMENT_1
	}

	def "Scenario: get unexistent file uploading by id"() {
		given: "The stored resources and files"
		databaseConfigurer.setup("liquibase-test/storage/FileUploadingRepositoryITSpec.changelog.xml")
		and: "There are no file uploadings"
		
		when: "The file uploading has been requested by id"
		Optional<FileUploading> fileUploading = fileUploadingRepository.findById(RESOURCE_ID, FILE_ID_1)
		
		then: "The file uploading shouldn't be returned"
		fileUploading.isPresent() == false
	}

	def "Scenario: save history and get latest"() {
		given: "The stored resources and files"
		databaseConfigurer.setup("liquibase-test/storage/FileUploadingRepositoryITSpec.changelog.xml")
		
		and: "The two file uploadings for the same resource, but with different uploading moments"
		FileUploading fileUploadingFirst = createFileUploading(FILE_ID_1, UPLOADING_MOMENT_1)
		FileUploading fileUploadingSecond = createFileUploading(FILE_ID_2, UPLOADING_MOMENT_2)
		
		when: "The file uploadings has been stored to the database"
		fileUploadingRepository.save(fileUploadingFirst)
		fileUploadingRepository.save(fileUploadingSecond)
		
		and: "The latest uploading has been requested"
		FileUploading obtainedFileUploading = fileUploadingRepository.findLatestResourceUploading(RESOURCE_ID).get()
		
		then: "The latest file uploading should be obtained"
		obtainedFileUploading.getFileId() == FILE_ID_2
		obtainedFileUploading.getResourceId() == RESOURCE_ID
		obtainedFileUploading.getUploadingMoment() == UPLOADING_MOMENT_2
	}
	
	def "Scenario: get latest for unexsistent history"() {
		given: "The stored resources and files"
		databaseConfigurer.setup("liquibase-test/storage/FileUploadingRepositoryITSpec.changelog.xml")
		and: "There are no file uploadings"
		
		when: "The latest file uploading has been requested for resource"
		Optional<FileUploading> fileUploading = fileUploadingRepository.findLatestResourceUploading(RESOURCE_ID)
		
		then: "The file uploading shouldn't be returned"
		fileUploading.isPresent() == false
	}
	
	def "Scenario: remove existing file uploading"() {
		given: "The stored resources and files"
		databaseConfigurer.setup("liquibase-test/storage/FileUploadingRepositoryITSpec.changelog.xml")
		
		and: "There is file uploading"
		fileUploadingRepository.save(createFileUploading(FILE_ID_1, UPLOADING_MOMENT_1))
		
		when: "The existing resource remove command has been requested"
		fileUploadingRepository.remove(new FileUploadingId(RESOURCE_ID, FILE_ID_1))
		
		then: "The removed uploading shouldn't be found"
		fileUploadingRepository.findById(RESOURCE_ID, FILE_ID_1).isPresent() == false
	}
	
	def "Scenario: remove unknown file uploading"() {
		given: "The stored resources and files"
		databaseConfigurer.setup("liquibase-test/storage/FileUploadingRepositoryITSpec.changelog.xml")
		
		when: "The not existing resource remove command has been requested"
		fileUploadingRepository.remove(new FileUploadingId("UNKNOWN", "FILE"))
		
		then: "The nothing errors should be occurred"
		noExceptionThrown()
	}

	private FileUploading createFileUploading(String fileId, DateTime uploadingMoment) {
		return FileUploading.builder()
				.resourceId(RESOURCE_ID)
				.fileId(fileId)
				.uploadingMoment(uploadingMoment)
				.build()
	}
}
