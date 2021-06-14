package io.bincloud.resources.port.adapter.config;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import io.bincloud.common.domain.model.generator.SequentialGenerator;
import io.bincloud.common.domain.model.validation.ValidationService;
import io.bincloud.files.domain.model.contracts.FileStorage;
import io.bincloud.resources.application.FileUploadService;
import io.bincloud.resources.application.FileUploadsHistoryService;
import io.bincloud.resources.application.ResourceManagementService;
import io.bincloud.resources.domain.model.ResourceRepository;
import io.bincloud.resources.domain.model.file.FileUploadsHistory;
import io.bincloud.resources.domain.model.file.FileUploadsRepository;

@ApplicationScoped
public class ServicesConfig {
	@Inject
	private FileStorage fileStorage;

	@Inject
	private ResourceRepository resourceRepository;

	@Inject
	private FileUploadsRepository fileUploadsRepository;

	@Inject
	@Named("resourceIdGenerator")
	private SequentialGenerator<Long> resourceIdGenerator;

	@Inject
	private ValidationService validationService;

	@Produces
	public FileUploadService fileUploadingService(FileUploadsHistory fileUploadsHistory) {
		return new FileUploadService(resourceRepository, fileStorage, fileUploadsHistory);
	}

	@Produces
	public ResourceManagementService resourceManagementService(FileUploadsHistory fileUploadsHistory) {
		return new ResourceManagementService(resourceIdGenerator, validationService, resourceRepository,
				defaultFileNameGenerator(), fileUploadsHistory);
	}
	
	@Produces
	public FileUploadsHistory fileUploadsHistory() {
		return new FileUploadsHistoryService(fileStorage, fileUploadsRepository);
	}

	private SequentialGenerator<String> defaultFileNameGenerator() {
		return () -> UUID.randomUUID().toString();
	}
}
