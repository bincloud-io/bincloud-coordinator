package io.bincloud.resources.application

import io.bincloud.common.domain.model.error.ApplicationException
import io.bincloud.common.domain.model.error.ApplicationException.Severity
import io.bincloud.common.domain.model.generator.SequentialGenerator
import io.bincloud.common.domain.model.validation.ValidationException
import io.bincloud.common.domain.model.validation.ValidationService
import io.bincloud.common.domain.model.validation.ValidationState
import io.bincloud.files.domain.model.contracts.upload.FileUploader
import io.bincloud.resources.application.ResourceManagementService
import io.bincloud.resources.domain.model.Constants
import io.bincloud.resources.domain.model.resource.RemoveExistingResource
import io.bincloud.resources.domain.model.resource.Resource
import io.bincloud.resources.domain.model.resource.ResourceDoesNotExistException
import io.bincloud.resources.domain.model.resource.ResourceRepository
import io.bincloud.resources.domain.model.resource.UnspecifiedResourceException
import io.bincloud.resources.domain.model.resource.history.FileHistory
import io.bincloud.resources.domain.model.resource.history.FileStorage
import io.bincloud.resources.domain.model.resource.history.TruncateUploadHistory
import spock.lang.Narrative
import spock.lang.Specification

class RemoveExistingResourceFeature extends Specification {
	private static final Long RESOURCE_ID = 1L
	private static final String FILE_NAME = "filename.txt"
	private static final String RANDOM_FILE_NAME = "RANDOM.FILE"
	
	private SequentialGenerator<Long> resourceIdGenerator
	private ValidationService validationService
	private ResourceRepository resourceRepository
	private SequentialGenerator<String> randomNameGenerator
	private FileHistory historyManager
	private ResourceManagementService resourceManagementService
	
	def setup() {
		this.resourceIdGenerator = Stub(SequentialGenerator)
		this.validationService = Stub(ValidationService)
		this.resourceRepository = Mock(ResourceRepository)
		this.randomNameGenerator = {RANDOM_FILE_NAME}
		this.historyManager = Mock(FileHistory)
		this.resourceManagementService = new ResourceManagementService(
			this.resourceIdGenerator,
			this.validationService,
			this.randomNameGenerator,
			this.resourceRepository,
			this.historyManager)
	}

	def "Scenario: remove existing resource"() {
		TruncateUploadHistory truncateHistoryCommand;
		given: "The remove existing resource command with specified id"
		RemoveExistingResource command = Stub(RemoveExistingResource) {
			getResourceId() >> Optional.of(RESOURCE_ID)
		}
		 
		and: "The remove existing resource command state is valid"
		validationService.validate(command) >> new ValidationState()
				
		and: "The removable resource exists in repository"
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())

		when: "The remove existing resource happens"
		resourceManagementService.removeExistingResource(command)

		then: "The file uploads history should be clear"
		1 * historyManager.truncateUploadHistory(_) >> {truncateHistoryCommand = it[0]}
		truncateHistoryCommand.getResourceId() == RESOURCE_ID
		truncateHistoryCommand.getHistoryLength() == 0L
		
		and: "The resource has been removed"
		1 * resourceRepository.remove(RESOURCE_ID)
	}
	
	def "Scenario: remove resource for wrong command state"() {
		
		given: "The remove existing resource command with specified id"
		RemoveExistingResource command = Stub(RemoveExistingResource) {
			getResourceId() >> Optional.of(RESOURCE_ID)
		}
		 
		and: "The remove existing resource command state is wrong"
		validationService.validate(command) >> new ValidationState()
			.withGrouped("resourceId", "error")

		when: "The remove existing resource happens"
		resourceManagementService.removeExistingResource(command)

		then: "The validation exception error should be happened"
		ValidationException error = thrown()
		error.severity == Severity.BUSINESS
		error.getContext() == Constants.CONTEXT
		error.errorState.groupedErrors.get("resourceId") == Arrays.asList("error")
		error.getErrorCode() == Constants.INVALID_REMOVE_EXISTING_RESOURCE_COMMAND_STATE	
	}

	def "Scenario: remove resource with unspecified id"() {
		given: "The remove existing resource command with specified id"
		RemoveExistingResource command = Stub(RemoveExistingResource) {
			getResourceId() >> Optional.empty()
		}
		 
		and: "The remove existing resource command state is valid"
		validationService.validate(command) >> new ValidationState()

		when: "The remove existing resource happens"
		resourceManagementService.removeExistingResource(command)

		then: "The resource does not exists error occurred"
		UnspecifiedResourceException error = thrown()
		error.severity == Severity.BUSINESS
		error.context == Constants.CONTEXT
		error.errorCode == UnspecifiedResourceException.ERROR_CODE
	}
	
	def "Scenario: remove unknown resource"() {
		given: "The remove existing resource command with specified id"
		RemoveExistingResource command = Stub(RemoveExistingResource) {
			getResourceId() >> Optional.of(RESOURCE_ID)
		}
		 
		and: "The remove existing resource command state is valid"
		validationService.validate(command) >> new ValidationState()
				
		and: "The removable resource exists in repository"
		resourceRepository.findById(RESOURCE_ID) >> Optional.empty()

		when: "The remove existing resource happens"
		resourceManagementService.removeExistingResource(command)

		then: "The resource does not exists error occurred"
		ResourceDoesNotExistException error = thrown()
		error.severity == Severity.BUSINESS
		error.context == Constants.CONTEXT
		error.errorCode == ResourceDoesNotExistException.ERROR_CODE
	}
	
	private Resource createResource() {
		return Resource.builder()
				.id(RESOURCE_ID)
				.fileName(FILE_NAME)
				.build();
	}
}
