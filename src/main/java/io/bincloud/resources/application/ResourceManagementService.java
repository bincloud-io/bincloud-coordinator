package io.bincloud.resources.application;

import java.util.Optional;
import java.util.function.Supplier;

import io.bincloud.common.domain.model.event.LocalEventTransport;
import io.bincloud.common.domain.model.generator.SequentialGenerator;
import io.bincloud.common.domain.model.validation.ValidationService;
import io.bincloud.resources.domain.model.Constants;
import io.bincloud.resources.domain.model.FileReference;
import io.bincloud.resources.domain.model.resource.CreateResource;
import io.bincloud.resources.domain.model.resource.CreateUploadLink;
import io.bincloud.resources.domain.model.resource.RemoveExistingResource;
import io.bincloud.resources.domain.model.resource.Resource;
import io.bincloud.resources.domain.model.resource.ResourceHasBeenDeleted;
import io.bincloud.resources.domain.model.resource.ResourceManager;
import io.bincloud.resources.domain.model.resource.ResourceRepository;
import io.bincloud.resources.domain.model.resource.history.FileHistory;
import io.bincloud.resources.domain.model.resource.history.RegisterFileUpload;
import io.bincloud.resources.domain.model.resource.history.TruncateUploadHistory;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ResourceManagementService implements ResourceManager {
	private final SequentialGenerator<Long> idGenerator;
	private final ValidationService validationService;
	private final SequentialGenerator<String> defaultFileNameGenerator;
	private final ResourceRepository resourceRepository;
	private final FileHistory fileHistory;

	@Override
	public Long createNewResource(CreateResource resourceDetails) {
		validateCommand(resourceDetails, Constants.INVALID_CREATE_NEW_RESOURCE_COMMAND_STATE);
		Resource resource = new Resource(idGenerator, resourceDetails, defaultFileNameGenerator);
		resourceRepository.save(resource);
		return resource.getId();
	}

	@Override
	public void removeExistingResource(RemoveExistingResource command) {
		validateCommand(command, Constants.INVALID_REMOVE_EXISTING_RESOURCE_COMMAND_STATE);
		Resource resource = getExistingResource(command.getResourceId());
		fileHistory.truncateUploadHistory(new ClearResourceHistoryCommand(resource));
		resourceRepository.remove(resource.getId());
		notifyAboutDeletedResource(resource);
	}

	@Override
	public FileReference createUploadLink(CreateUploadLink command) {
		validateCommand(command, Constants.INVALID_CREATE_RESOURCE_FILE_UPLOAD_LINK_COMMAND_STATE);
		Resource resource = getExistingResource(command.getResourceId());
		FileReference fileReference = fileHistory.registerUploadedFile(new RegisterFileUploadCommand(resource));
		return fileReference;
	}

	private Resource getExistingResource(Optional<Long> resourceId) {
		Supplier<Resource> resourceProvider = new ExistingResourceProvider(resourceId, resourceRepository);
		return resourceProvider.get();
	}

	private void notifyAboutDeletedResource(Resource resource) {
		LocalEventTransport.createGlobalEventPublisher().publish(new ResourceHasBeenDeleted(resource.getId()));
	}

	private <C> void validateCommand(C command, Long errorCode) {
		validationService.validate(command).checkValidState(Constants.CONTEXT, errorCode);
	}

	@EqualsAndHashCode
	@RequiredArgsConstructor
	private class RegisterFileUploadCommand implements RegisterFileUpload {
		private final Resource resource;

		@Override
		public Long getResourceId() {
			return resource.getId();
		}

		@Override
		public String getFileName() {
			return resource.getFileName();
		}

		@Override
		public String getMediaType() {
			return resource.getMediaType();
		}

		@Override
		public String getContentDisposition() {
			return resource.getContentDisposition();
		}
	}

	@EqualsAndHashCode
	@RequiredArgsConstructor
	private class ClearResourceHistoryCommand implements TruncateUploadHistory {
		private final Resource resource;

		@Override
		public Long getResourceId() {
			return resource.getId();
		}

		@Override
		public Long getHistoryLength() {
			return 0L;
		}
	}
}
