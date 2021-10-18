package io.bincloud.resources.port.adapter.config;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import io.bincloud.common.domain.model.generator.SequentialGenerator;
import io.bincloud.common.domain.model.validation.ValidationService;
import io.bincloud.resources.application.ResourceManagementService;
import io.bincloud.resources.domain.model.resource.ResourceRepository;
import io.bincloud.resources.domain.model.resource.history.FileHistory;
import io.bincloud.resources.domain.model.resource.history.FileHistoryService;
import io.bincloud.resources.domain.model.resource.history.FileStorage;
import io.bincloud.resources.domain.model.resource.history.UploadedFileRepository;

@ApplicationScoped
public class ServicesConfig {
	@Inject
	private FileStorage fileStorage;

	@Inject
	private ResourceRepository resourceRepository;

	@Inject
	private UploadedFileRepository fileUploadsRepository;

	@Inject
	@Named("resourceIdGenerator")
	private SequentialGenerator<Long> resourceIdGenerator;

	@Inject
	private ValidationService validationService;

	@Produces
	public FileHistory fileHistory() {
		return new FileHistoryService(fileUploadsRepository, fileStorage);
	}
	
	@Produces
	public ResourceManagementService resourceManagementService(FileHistory fileHistory) {
		return new ResourceManagementService(resourceIdGenerator, validationService, defaultFileNameGenerator(),
				resourceRepository, fileHistory);
	}

	private SequentialGenerator<String> defaultFileNameGenerator() {
		return () -> UUID.randomUUID().toString();
	}
}
