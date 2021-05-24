package io.bincloud.storage.application

import io.bincloud.common.domain.model.error.ApplicationException.Severity
import io.bincloud.common.domain.model.generator.SequentialGenerator
import io.bincloud.common.domain.model.validation.ValidationException
import io.bincloud.common.domain.model.validation.ValidationService
import io.bincloud.common.domain.model.validation.ValidationState
import io.bincloud.storage.application.resource.ResourceService
import io.bincloud.storage.domain.model.resource.Constants
import io.bincloud.storage.domain.model.resource.Resource
import io.bincloud.storage.domain.model.resource.ResourceRepository
import io.bincloud.storage.domain.model.resource.Resource.ResourceDetails
import spock.lang.Narrative
import spock.lang.Specification

@Narrative("""
	In order to identify a stored file in the object storage,
	As a user, I want to create a resource that will represent the corresponding file in the storage
""")
class CreateNewResourceFeature extends Specification {
	private static final String RANDOM_FILE_NAME = "RANDOM.FILE"
	private SequentialGenerator<Long> resourceIdGenerator;
	private ValidationService validationService;
	private ResourceRepository resourceRepository;
	private ResourceService serviceUnderTest;
	
	def setup() {
		this.resourceIdGenerator = Stub(SequentialGenerator)
		this.validationService = Stub(ValidationService)
		this.resourceRepository = Mock(ResourceRepository)
		this.serviceUnderTest = new ResourceService(
			this.resourceIdGenerator,
			this.validationService,
			this.resourceRepository, 
			{RANDOM_FILE_NAME})
	}

	def "Scenario: create new resource with specified name"() {
		Long createdResourceId;
		given: "The resource id generator will generate id=1"
		resourceIdGenerator.nextValue() >> 1L

		and: "The request parameters are valid"
		ResourceDetails resourceDetails = Stub(ResourceDetails) {
			getFileName() >> Optional.ofNullable("file_name")
		}
		validationService.validate(resourceDetails) >> new ValidationState()

		when: "The create new resource operation has been called"
		createdResourceId = this.serviceUnderTest.createNewResource(resourceDetails)
		
		then: "The new resource has been stored"
		1 *  resourceRepository.save(_) >> {
			Resource storedResource = it[0];
			createdResourceId == storedResource.id
			storedResource.fileName == "file_name"
		}
	}
	
	def "Scenario: create new resource with randomly generated file name"() {
		Long createdResourceId;
		given: "The resource id generator will generate id=1"
		resourceIdGenerator.nextValue() >> 1L

		and: "The request parameters are valid"
		ResourceDetails resourceDetails = Stub(ResourceDetails) {
			getFileName() >> Optional.empty()
		}
		validationService.validate(resourceDetails) >> new ValidationState()

		when: "The create new resource operation has been called"
		createdResourceId = this.serviceUnderTest.createNewResource(resourceDetails)
		
		then: "The new resource has been stored"
		1 *  resourceRepository.save(_) >> {
			Resource storedResource = it[0];
			createdResourceId == storedResource.id
			storedResource.fileName == "file_name"
		}
	}

	def "Scenario: try to create resource with invalid request parameters"() {
		given: "The resource id generator will generate id=1"
		resourceIdGenerator.nextValue() >> 1L
		
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
		
		and: "The validation error code is ${Constants.INVALID_RESOURCE_DETAILS_ERROR}"
		error.getErrorCode() == Constants.INVALID_RESOURCE_DETAILS_ERROR
	}
}
