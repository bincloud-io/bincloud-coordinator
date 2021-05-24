package io.bincloud.storage.port.adapter.resource.config;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.bincloud.common.domain.model.event.LocalEventTransport;
import io.bincloud.storage.application.resource.FileUploadingService;
import io.bincloud.storage.domain.model.file.FileStorage;
import io.bincloud.storage.domain.model.resource.FileHasBeenUploaded;
import io.bincloud.storage.domain.model.resource.ResourceRepository;
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

	@Produces
	public FileUploadingService resourceManagementService() {
		return new FileUploadingService(resourceRepository, fileStorage,
				LocalEventTransport.createGlobalEventPublisher());
	}

	@PostConstruct
	public void configureFileHasBeenUploadedEventListener() {
		LocalEventTransport.registerLocalEventListener(FileHasBeenUploaded.class,
				new FileHasBeenUploadedListener(fileUploadingsRepository));
	}
}
