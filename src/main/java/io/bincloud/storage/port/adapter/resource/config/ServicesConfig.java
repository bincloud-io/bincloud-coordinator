package io.bincloud.storage.port.adapter.resource.config;

import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import io.bincloud.common.domain.model.event.LocalEventTransport;
import io.bincloud.common.domain.model.generator.SequentialGenerator;
import io.bincloud.common.domain.model.validation.ValidationService;
import io.bincloud.storage.application.resource.ResourceManagementService;
import io.bincloud.storage.application.resource.file.FileUploadService;
import io.bincloud.storage.domain.model.file.facades.FileStorage;
import io.bincloud.storage.domain.model.resource.ResourceRepository;
import io.bincloud.storage.domain.model.resource.file.FileHasBeenUploaded;
import io.bincloud.storage.domain.model.resource.file.FileHasBeenUploadedListener;
import io.bincloud.storage.domain.model.resource.file.FileUploadingRepository;

@ApplicationScoped
public class ServicesConfig {
	@Inject
	private FileStorage fileStorage;

	@Inject
	private ResourceRepository resourceRepository;

	@Inject
	private FileUploadingRepository fileUploadingsRepository;

	@Inject
	@Named("resourceIdGenerator")
	private SequentialGenerator<Long> resourceIdGenerator;

	@Inject
	private ValidationService validationService;

	@Produces
	public FileUploadService fileUploadingService() {
		return new FileUploadService(resourceRepository, fileStorage,
				LocalEventTransport.createGlobalEventPublisher());
	}

	@Produces
	public ResourceManagementService resourceManagementService() {
		return new ResourceManagementService(resourceIdGenerator, validationService, resourceRepository,
				defaultFileNameGenerator(), LocalEventTransport.createGlobalEventPublisher());
	}

	@PostConstruct
	public void configureFileHasBeenUploadedEventListener() {
		LocalEventTransport.registerLocalEventListener(FileHasBeenUploaded.class,
				new FileHasBeenUploadedListener(fileUploadingsRepository));
	}

	private SequentialGenerator<String> defaultFileNameGenerator() {
		return () -> UUID.randomUUID().toString();
	}
}
