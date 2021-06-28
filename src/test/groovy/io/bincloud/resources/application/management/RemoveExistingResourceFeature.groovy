package io.bincloud.resources.application.management

import java.lang.annotation.Retention

import io.bincloud.common.domain.model.error.ApplicationException.Severity
import io.bincloud.common.domain.model.event.EventPublisher
import io.bincloud.common.domain.model.generator.SequentialGenerator
import io.bincloud.common.domain.model.validation.ValidationService
import io.bincloud.resources.application.management.ResourceManagementService
import io.bincloud.resources.domain.model.Constants
import io.bincloud.resources.domain.model.Resource
import io.bincloud.resources.domain.model.ResourceRepository
import io.bincloud.resources.domain.model.contracts.ResourceManager
import io.bincloud.resources.domain.model.errors.ResourceDoesNotExistException
import io.bincloud.resources.domain.model.errors.UnspecifiedResourceException
import io.bincloud.resources.domain.model.file.FileUploadsHistory
import spock.lang.Narrative
import spock.lang.Specification

@Narrative("""
	In order to remove a stored file from the object storage, that became unnecessary,
	As a user, I want to remove a resource representing the file
""")
class RemoveExistingResourceFeature extends Specification {
	private static final Long RESOURCE_ID = 1L
	private static final String FILE_NAME = "filename.txt"
	
	private SequentialGenerator<Long> resourceIdGenerator
	private ValidationService validationService
	private ResourceRepository resourceRepository
	private SequentialGenerator<String> defaultResourceNameGenerator
	private ResourceManager removeResourceService
	private FileUploadsHistory fileUploadsHistory;

	def setup() {
		this.resourceIdGenerator = Mock(SequentialGenerator)
		this.validationService = Mock(ValidationService)
		this.fileUploadsHistory = Mock(FileUploadsHistory)
		this.resourceRepository = Mock(ResourceRepository)
		this.defaultResourceNameGenerator = Mock(SequentialGenerator)
		this.removeResourceService = new ResourceManagementService(
			resourceIdGenerator, validationService, resourceRepository, defaultResourceNameGenerator, fileUploadsHistory)
	}

	def "Scenario: remove existing resource"() {
		given: "The removable resource exists in repository"
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())

		when: "The remove existing resource operation has been called"
		removeResourceService.removeExistingResource(Optional.of(RESOURCE_ID))

		then: "The file uploads history should be clear"
		1 * fileUploadsHistory.clearUploadsHistory(RESOURCE_ID)
		
		and: "The resource has been removed"
		1 * resourceRepository.remove(RESOURCE_ID)


	}

	def "Scenario: try to remove unknown resource"() {
		given: "The removable resource doesn't exist in repository"
		resourceRepository.findById(RESOURCE_ID) >> Optional.empty()

		when: "The remove existing resource operation has been called"
		removeResourceService.removeExistingResource(Optional.of(RESOURCE_ID))

		then: "The resource does not exists error occurred"
		ResourceDoesNotExistException error = thrown()
		error.severity == Severity.BUSINESS
		error.context == Constants.CONTEXT
		error.errorCode == ResourceDoesNotExistException.ERROR_CODE
	}

	def "Scenario: try to remove resource without received id"() {
		given: "The removable resource id hasn't been received"


		when: "The remove existing resource operation has been called"
		removeResourceService.removeExistingResource(Optional.empty())

		then: "The uspecified resource error occurred"
		UnspecifiedResourceException error = thrown()
		error.severity == Severity.BUSINESS
		error.context == Constants.CONTEXT
		error.errorCode == UnspecifiedResourceException.ERROR_CODE
	}
	
	private Resource createResource() {
		return Resource.builder()
				.id(RESOURCE_ID)
				.fileName(FILE_NAME)
				.build();
	}
}
