package io.bincloud.storage.port.adapter.file.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.bincloud.common.domain.model.generator.SequentialGenerator;
import io.bincloud.common.port.adapters.io.transfer.transmitter.DirectTransferingScheduler;
import io.bincloud.storage.domain.model.file.FileRepository;
import io.bincloud.storage.domain.model.file.FileStorage;
import io.bincloud.storage.domain.model.file.FilesystemAccessor;
import io.bincloud.storage.domain.model.resource.FileManagementService;

@ApplicationScoped
public class ServicesConfig {
	@Inject
	private FilesystemAccessor filesystemAccessor;
	
	@Inject
	private FileRepository fileRepository;
	
	@Inject
	@FileIdGenerator
	private SequentialGenerator<String> fileIdGenerator;
	
	@Produces
	public FileStorage fileStorage() {
		return new FileManagementService(fileIdGenerator, fileRepository, filesystemAccessor, new DirectTransferingScheduler());
	}
}
