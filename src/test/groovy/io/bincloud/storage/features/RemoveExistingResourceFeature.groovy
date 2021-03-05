package io.bincloud.storage.features

import io.bincloud.common.ApplicationException.Severity
import io.bincloud.storage.application.RemoveResourceService
import io.bincloud.storage.application.ResourceDoesNotExistsException
import io.bincloud.storage.application.UnspecifiedResourceException
import io.bincloud.storage.domain.model.resource.Resource
import io.bincloud.storage.domain.model.resource.ResourceManagementException
import io.bincloud.storage.domain.model.resource.ResourceRepository
import spock.lang.Narrative
import spock.lang.Specification

@Narrative("""
	In order to remove a stored file from the object storage, that became unnecessary,
	As a user, I want to remove a resource representing the file
""")
class RemoveExistingResourceFeature extends Specification {
	ResourceRepository resourceRepository;
	RemoveResourceService removeResourceService;
	
	def setup() {
		this.resourceRepository = Mock(ResourceRepository)
		this.removeResourceService = new RemoveResourceService(resourceRepository);
	}
	
	def "Scenario: remove existing resource"() {
		given: "The removable resource exists in repository"
		resourceRepository.findById(1L) >> Optional.of(Resource.builder()
			.id(1L)
		.build())

		and: "The removable resource id has been received"
		Long removableResourceId = 1L;
		
		
		when: "The remove existing resource operation has been called"
		removeResourceService.removeExistingResource(removableResourceId)
		
		then: "The resource has been removed"
			1 * resourceRepository.findById(1L)
			1 * resourceRepository.remove(1L)
	}

	def "Scenario: try to remove unknown resource"() {
		ResourceRepository resourceRepository = Mock(ResourceRepository);
		
		given: "The removable resource doesn't exist in repository"
		resourceRepository.findById(1L) >> Optional.empty()
		
		and: "The removable resource id has been received"
		Long removableResourceId = 1L
		
		when: "The remove existing resource operation has been called"
		removeResourceService.removeExistingResource(removableResourceId)
		
		then: "The resource does not exists error occurred"
		ResourceDoesNotExistsException error = thrown()
		error.severity == Severity.BUSINESS
		error.context == ResourceManagementException.CONTEXT
		error.errorCode == ResourceDoesNotExistsException.ERROR_CODE
		
	}

	def "Scenario: try to remove resource without received id"() {
		given: "The removable resource id hasn't been received"
			
			
		when: "The remove existing resource operation has been called"
			removeResourceService.removeExistingResource(null)
			
		then: "The uspecified resource error occurred"
		ResourceDoesNotExistsException error = thrown()
		error.severity == Severity.BUSINESS
		error.context == ResourceManagementException.CONTEXT
		error.errorCode == UnspecifiedResourceException.ERROR_CODE
	}
}
