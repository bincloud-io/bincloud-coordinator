package io.bincloud.storage.application.resource

import io.bincloud.common.domain.model.error.ApplicationException.Severity
import io.bincloud.common.domain.model.event.EventPublisher
import io.bincloud.common.domain.model.generator.SequentialGenerator
import io.bincloud.common.domain.model.validation.ValidationService
import io.bincloud.storage.domain.model.resource.Constants
import io.bincloud.storage.domain.model.resource.ResourceRepository
import io.bincloud.storage.domain.model.resource.errors.ResourceDoesNotExistException
import io.bincloud.storage.domain.model.resource.errors.ResourceHasBeenRemoved
import io.bincloud.storage.domain.model.resource.errors.UnspecifiedResourceException
import io.bincloud.storage.domain.model.resource.facades.ResourceManager
import spock.lang.Narrative
import spock.lang.Specification

@Narrative("""
	In order to remove a stored file from the object storage, that became unnecessary,
	As a user, I want to remove a resource representing the file
""")
class RemoveExistingResourceFeature extends Specification {
	private SequentialGenerator<Long> resourceIdGenerator
	private ValidationService validationService
	private ResourceRepository resourceRepository
	private SequentialGenerator<String> defaultResourceNameGenerator
	private ResourceManager removeResourceService
	private EventPublisher<ResourceHasBeenRemoved> eventPublisher

	def setup() {
		this.resourceIdGenerator = Mock(SequentialGenerator)
		this.validationService = Mock(ValidationService)
		this.eventPublisher = Mock(EventPublisher)
		this.resourceRepository = Mock(ResourceRepository)
		this.defaultResourceNameGenerator = Mock(SequentialGenerator)
		this.removeResourceService = new ResourceManagementService(
			resourceIdGenerator, validationService, resourceRepository, defaultResourceNameGenerator, eventPublisher)
	}

	def "Scenario: remove existing resource"() {
		ResourceHasBeenRemoved publishedEvent;
		given: "The removable resource exists in repository"
		resourceRepository.isExists(1L) >> true

		when: "The remove existing resource operation has been called"
		removeResourceService.removeExistingResource(Optional.of(1L))

		then: "The resource has been removed"
		1 * resourceRepository.remove(1L)

		and: "The system notified about resource removing"

		1 * eventPublisher.publish(_) >> {arguments -> publishedEvent = arguments[0]}
		publishedEvent.resourceId == 1L
	}

	def "Scenario: try to remove unknown resource"() {
		ResourceRepository resourceRepository = Mock(ResourceRepository);

		given: "The removable resource doesn't exist in repository"
		resourceRepository.isExists(1L) >> false

		when: "The remove existing resource operation has been called"
		removeResourceService.removeExistingResource(Optional.of(1L))

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
}
