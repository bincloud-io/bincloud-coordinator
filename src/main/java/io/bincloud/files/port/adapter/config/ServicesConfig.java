package io.bincloud.files.port.adapter.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.bincloud.common.domain.model.generator.SequentialGenerator;
import io.bincloud.common.port.adapters.io.transfer.transmitter.DirectTransferingScheduler;
import io.bincloud.files.domain.model.FileManagementService;
import io.bincloud.files.domain.model.FileRepository;
import io.bincloud.files.domain.model.FilesystemAccessor;
import io.bincloud.files.domain.model.contracts.FileStorage;

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
