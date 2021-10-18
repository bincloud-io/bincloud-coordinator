package io.bincloud.resources.application

import io.bincloud.common.domain.model.generator.SequentialGenerator
import io.bincloud.common.domain.model.validation.ValidationService
import io.bincloud.common.domain.model.validation.ValidationState
import io.bincloud.resources.domain.model.FileReference
import io.bincloud.resources.domain.model.resource.CreateUploadLink
import io.bincloud.resources.domain.model.resource.Resource
import io.bincloud.resources.domain.model.resource.ResourceManager
import io.bincloud.resources.domain.model.resource.ResourceRepository
import io.bincloud.resources.domain.model.resource.history.FileHistory
import io.bincloud.resources.domain.model.resource.history.FileStorage
import io.bincloud.resources.domain.model.resource.history.RegisterFileUpload
import io.bincloud.resources.domain.model.resource.history.FileStorage.CreateFileInStorage
import spock.lang.Specification

class CreateFileUploadLinkFeature extends Specification {
	private static final Long RESOURCE_ID = 1L
	private static final String FILE_NAME = "filename.txt"
	private static final String RANDOM_FILE_NAME = "RANDOM.FILE"
	public static final String MEDIA_TYPE = "application/octet-stream";
	public static final String CONTENT_DISPOSITION = "inline";
	private static final String FILESYSTEM_NAME = UUID.randomUUID().toString()

	private SequentialGenerator<Long> resourceIdGenerator
	private ValidationService validationService
	private ResourceRepository resourceRepository
	private SequentialGenerator<String> randomNameGenerator
	private FileHistory fileHistory
	private ResourceManager resourceManager

	def setup() {
		this.resourceIdGenerator = Stub(SequentialGenerator)
		this.validationService = Stub(ValidationService)
		this.resourceRepository = Mock(ResourceRepository)
		this.randomNameGenerator = {RANDOM_FILE_NAME}
		this.fileHistory = Mock(FileHistory)
		this.resourceManager = new ResourceManagementService(
			resourceIdGenerator, 
			validationService, 
			randomNameGenerator, 
			resourceRepository, 
			fileHistory)
	}

	def "Scenario: create upload link successfully"() {
		RegisterFileUpload registerFileUploadCommand

		given: "The create upload link command with specified resource id"
		CreateUploadLink command = Stub(CreateUploadLink) {
			getResourceId() >> Optional.of(RESOURCE_ID)
		}

		and: "The create upload link command state is vaid"
		validationService.validate(command) >> new ValidationState()

		and: "The resource exists into repository"
		resourceRepository.findById(RESOURCE_ID) >> Optional.of(createResource())

		and: "The file history successfully registers file upload into history and creates file in the storage"
		1 * fileHistory.registerUploadedFile(_) >> {
			registerFileUploadCommand = it[0]
			return createFileReference()
		}
		
		when: "The create upload link command happens"
		FileReference fileReference = resourceManager.createUploadLink(command)

		then: "The file upload successfully registers in the file history"
		registerFileUploadCommand.getResourceId() == RESOURCE_ID
		registerFileUploadCommand.getFileName() == FILE_NAME
		registerFileUploadCommand.getMediaType() == MEDIA_TYPE
		registerFileUploadCommand.getContentDisposition() == CONTENT_DISPOSITION

		and: "The returned file reference should has correct state"
		fileReference.getResourceId() == RESOURCE_ID
		fileReference.getFilesystemName() == FILESYSTEM_NAME
	}
	
	private FileReference createFileReference() {
		FileReference reference = Stub(FileReference)
		reference.getResourceId() >> RESOURCE_ID
		reference.getFilesystemName() >> FILESYSTEM_NAME
		return reference
	}

	private Resource createResource() {
		return Resource.builder()
				.id(RESOURCE_ID)
				.fileName(FILE_NAME)
				.build();
	}
}
