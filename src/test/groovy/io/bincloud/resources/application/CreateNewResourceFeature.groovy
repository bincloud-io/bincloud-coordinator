package io.bincloud.resources.application

import io.bincloud.common.domain.model.error.ApplicationException.Severity
import io.bincloud.common.domain.model.generator.SequentialGenerator
import io.bincloud.common.domain.model.validation.ValidationException
import io.bincloud.common.domain.model.validation.ValidationService
import io.bincloud.common.domain.model.validation.ValidationState
import io.bincloud.resources.application.ResourceManagementService
import io.bincloud.resources.domain.model.Constants
import io.bincloud.resources.domain.model.resource.CreateResource
import io.bincloud.resources.domain.model.resource.Resource
import io.bincloud.resources.domain.model.resource.ResourceRepository
import io.bincloud.resources.domain.model.resource.history.FileHistory
import io.bincloud.resources.domain.model.resource.history.FileStorage
import spock.lang.Specification

class CreateNewResourceFeature extends Specification {
	private static final String RANDOM_FILE_NAME = "RANDOM.FILE"
	private SequentialGenerator<Long> resourceIdGenerator
	private ValidationService validationService
	private ResourceRepository resourceRepository
	private SequentialGenerator<String> randomNameGenerator
	private FileHistory fileHistory
	private ResourceManagementService resourceManagementService
	
	def setup() {
		this.resourceIdGenerator = Stub(SequentialGenerator)
		this.validationService = Stub(ValidationService)
		this.resourceRepository = Mock(ResourceRepository)
		this.randomNameGenerator = {RANDOM_FILE_NAME}
		this.fileHistory = Mock(FileHistory)
		this.resourceManagementService = new ResourceManagementService(
			resourceIdGenerator, 
			validationService, 
			randomNameGenerator, 
			resourceRepository, 
			fileHistory)
	}

	def "Scenario: create new resource with specified name"() {
		Long createdResourceId;
		Resource storedResource;
		
		given: "The resource id generator generates random unique resource id"
		resourceIdGenerator.nextValue() >> 1L

		and: "The create resource command"
		CreateResource command = Stub(CreateResource) {
			getFileName() >> Optional.of("file_name")
		}
		
		and: "The command state is valid"
		validationService.validate(command) >> new ValidationState()

		when: "The create new resource command happens"
		createdResourceId = this.resourceManagementService.createNewResource(command)
		
		then: "The new resource has been stored"
		1 *  resourceRepository.save(_) >> {storedResource = it[0]}
		createdResourceId == storedResource.id
		storedResource.fileName == "file_name"
	}
	
	def "Scenario: create new resource with randomly generated file name"() {
		Long createdResourceId;
		Resource storedResource;
		given: "The resource id generator generates random unique resource id"
		resourceIdGenerator.nextValue() >> 1L

		and: "The create resource command with missing filename"
		CreateResource command = Stub(CreateResource) {
			getFileName() >> Optional.empty()
		}
		
		and: "The command state is valid"
		validationService.validate(command) >> new ValidationState()
		
		when: "The create new resource command happens"
		createdResourceId = this.resourceManagementService.createNewResource(command)
		
		then: "The new resource has been stored"
		1 *  resourceRepository.save(_) >> {storedResource = it[0];}
		
		createdResourceId == storedResource.id
		storedResource.fileName == RANDOM_FILE_NAME
	}

	def "Scenario: try to create resource with invalid request parameters"() {
		given: "The resource id generator generates random unique resource id"
		resourceIdGenerator.nextValue() >> 1L
		
		and: "The resource creation command is invalid"
		CreateResource resourceDetails = Stub(CreateResource) {
			getFileName() >> "file_name"
		}
		validationService.validate(resourceDetails) >> new ValidationState()
			.withGrouped("fileName", "error")
			
		when: "The create new resource command happens"
		this.resourceManagementService.createNewResource(resourceDetails)
		
		then: "The validation error occurred"
		ValidationException error = thrown()
		error.severity == Severity.BUSINESS
		error.getContext() == Constants.CONTEXT
		error.errorState.groupedErrors.get("fileName") == Arrays.asList("error")
		error.getErrorCode() == Constants.INVALID_CREATE_NEW_RESOURCE_COMMAND_STATE
	}
}
