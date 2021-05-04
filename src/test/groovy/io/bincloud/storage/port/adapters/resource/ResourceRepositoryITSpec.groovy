package io.bincloud.storage.port.adapters.resource

import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.COMPILE
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.RUNTIME
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.TEST

import javax.inject.Inject

import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.shrinkwrap.api.Archive
import org.junit.runner.RunWith

import io.bincloud.common.domain.model.error.ApplicationException
import io.bincloud.common.domain.model.generator.SequentialGenerator
import io.bincloud.storage.domain.model.file.FileRepository
import io.bincloud.storage.domain.model.resource.Resource
import io.bincloud.storage.domain.model.resource.ResourceRepository
import io.bincloud.storage.port.adapter.resource.JPAResourceRepository
import io.bincloud.testing.archive.ArchiveBuilder
import io.bincloud.testing.database.DatabaseConfigurer
import io.bincloud.testing.database.jdbc.cdi.JdbcLiquibase
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

@RunWith(ArquillianSputnik)
class ResourceRepositoryITSpec extends Specification {
	@Deployment
	public static final Archive "create deployment"() {
		return ArchiveBuilder.jar("file-repository-spec.jar")
		.resolveDependencies("pom.xml")
		.withScopes(COMPILE, RUNTIME, TEST)
		.resolveDependency("org.liquibase", "liquibase-core")
		.apply()
		.appendPackagesRecursively(Resource.getPackage().getName())
		.appendPackagesRecursively(DatabaseConfigurer.getPackage().getName())
		.appendPackagesRecursively(ApplicationException.getPackage().getName())
		.appendClasses(SequentialGenerator,  JPAResourceRepository, ResourceRepositoryITSpecConfig)
		.appendResource("liquibase")
		.appendResource("liquibase-test")
		.appendManifestResource("META-INF/beans.xml", "beans.xml")
		.appendManifestResource("jpa-test/resource-mapping-persistence.xml", "persistence.xml")
		.appendManifestResource("META-INF/orm/resource-mapping.xml", "orm/resource-mapping.xml")
		.build()
	}
	
	@Inject
	@JdbcLiquibase
	private DatabaseConfigurer databaseConfigurer;

	@Inject
	private ResourceRepository resourceRepository;

	def setup() {
		databaseConfigurer.setup("liquibase/master.changelog.xml")
	}

	def cleanup() {
		databaseConfigurer.tearDown()
	}
	
	def "Scenario: check resource existence"() {
		setup: "The resources are stored into database"
		databaseConfigurer.setup("liquibase-test/storage/ResourceRepositoryITSpec.changelog.xml")
		
		expect: "The resource existence check result should return #checkResult for resource #resourceId"
		resourceRepository.isExists(resourceId) == checkResult
		
		where:
		resourceId        | checkResult
		1L                | false
		2L                | false
		3L                | true
	}
	
	def "Scenario: the resource not found by id"() {
		given: "The resources are stored into database"
		databaseConfigurer.setup("liquibase-test/storage/ResourceRepositoryITSpec.changelog.xml")
		
		when: "The not existing resource with id=1 has been requested"
		Optional<Resource> resourceOptional = resourceRepository.findById(1L);
		
		then: "The resource optional result should be empty"
		resourceOptional.isPresent() == false
	}
	
	def "Scenario: the resource found by id"() {
		given: "The resources are stored into database"
		databaseConfigurer.setup("liquibase-test/storage/ResourceRepositoryITSpec.changelog.xml")
		
		when: "The existing resource with id=3 has been requested"
		Optional<Resource> resourceOptional = resourceRepository.findById(3L);
		
		then: "The resource optional result shouldn't be empty"
		resourceOptional.isPresent() == true
		Resource resource = resourceOptional.get()
		
		and: "The resource id should be 1"
		resource.getId() == 3L
		
		and: "The resource name should be \"filename.txt\""
		resource.getFileName() == "filename.txt"
	}
	
	def "Scenario: save resource to the database"() {
		given: "The resource to save"
		Resource sourceResource = createResource()
		
		when: "The resource has been stored to repository"
		resourceRepository.save(sourceResource)
		
		and: "Then the resource has been requested by id"
		Resource storedResource = resourceRepository.findById(sourceResource.getId()).orElse(null)
		
		then: "Both resources should have the same structural equivalency"
		storedResource == sourceResource
		storedResource.getFileName() == sourceResource.getFileName()
	}
	
	def "Scenario: remove resource from the database"() {
		given: "The resources stored into database"
		databaseConfigurer.setup("liquibase-test/storage/ResourceRepositoryITSpec.changelog.xml")
		
		when: "The existing resource has been removed by id"
		resourceRepository.remove(3L)
		
		then: "The existence check shouldn't be passed"
		resourceRepository.isExists(3L) == false
	}
	
	private Resource createResource() {
		return Resource.builder()
			.id(1L)
			.fileName("filename.txt")			
		.build()
	}
}
