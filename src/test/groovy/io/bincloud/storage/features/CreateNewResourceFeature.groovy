package io.bincloud.storage.features

import io.bincloud.common.ApplicationException.Severity
import io.bincloud.common.validation.ValidationException
import io.bincloud.common.validation.ValidationService
import io.bincloud.common.validation.ValidationState
import io.bincloud.storage.application.resource.ResourceService
import io.bincloud.storage.domain.model.resource.Resource
import io.bincloud.storage.domain.model.resource.ResourceRepository
import io.bincloud.storage.domain.model.resource.Resource.IdGenerator
import io.bincloud.storage.domain.model.resource.Resource.ResourceDetails
import spock.lang.Narrative
import spock.lang.Specification

@Narrative("""
	In order to identify a stored file in the object storage,
	As a user, I want to create a resource that will represent the corresponding file in the storage
""")
class CreateNewResourceFeature extends Specification {
	private IdGenerator resourceIdGenerator;
	private ValidationService validationService;
	private ResourceRepository resourceRepository;
	private ResourceService serviceUnderTest;
	
	def setup() {
		this.resourceIdGenerator = Stub(IdGenerator)
		this.validationService = Stub(ValidationService)
		this.resourceRepository = Mock(ResourceRepository)
		this.serviceUnderTest = new ResourceService(
			this.resourceIdGenerator,
			this.validationService,
			this.resourceRepository)
	}

	def "Scenario: create resource new resource"() {
		given: "The resource id generator will generate id=1"
		resourceIdGenerator.generateId() >> 1L

		and: "The request parameters are valid"
		ResourceDetails resourceDetails = Stub(ResourceDetails) {
			getFileName() >> "file_name"
		}
		validationService.validate(resourceDetails) >> new ValidationState()

		when: "The create new resource operation has been called"
		Long createdResourceId = this.serviceUnderTest.createNewResource(resourceDetails)
		
		then: "The new resource has been stored"
		Resource storedResource;
		1 *  resourceRepository.save(_) >> {arguments -> storedResource = arguments[0]}
		
		and: "The stored resource id is returned"
		createdResourceId == storedResource.id
		
		and: "The stored resource file name the same like in request parameters"
		storedResource.fileName == "file_name"
	}

	def "Scenario: try to create resource with invalid request parameters"() {
		given: "The resource id generator will generate id=1"
		resourceIdGenerator.generateId() >> 1L
		
		and: "The resource creation command is invalid"
		ResourceDetails resourceDetails = Stub(ResourceDetails) {
			getFileName() >> "file_name"
		}
		validationService.validate(resourceDetails) >> new ValidationState()
			.withGrouped("fileName", "error")
			
		when: "The create new resource operation has been called"
		this.serviceUnderTest.createNewResource(resourceDetails)
		
		then: "The validation error occurred"
		ValidationException error = thrown()
		
		and: "The validation error contains the same error like validation state"
		error.errorState.groupedErrors.get("fileName") == Arrays.asList("error")
		
		and: "The validation error severity is business"
		error.severity == Severity.BUSINESS
	}
}
