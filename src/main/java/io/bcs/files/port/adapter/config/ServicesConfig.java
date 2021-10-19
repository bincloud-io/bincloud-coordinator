package io.bcs.files.port.adapter.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.bcs.common.domain.model.generator.SequentialGenerator;
import io.bcs.common.domain.model.io.transfer.TransferingScheduler;
import io.bcs.files.application.download.FileDownloadService;
import io.bcs.files.application.upload.FileUploadService;
import io.bcs.files.domain.model.FileRepository;
import io.bcs.files.domain.model.FilesystemAccessor;
import io.bcs.files.domain.model.contracts.download.FileDownloader;
import io.bcs.files.domain.model.contracts.upload.FileUploader;

@ApplicationScoped
public class ServicesConfig {
	@Inject
	private FilesystemAccessor filesystemAccessor;
	
	@Inject
	private FileRepository fileRepository;
	
	@Inject
	@FileIdGenerator
	private SequentialGenerator<String> fileIdGenerator;

	@Inject
	private TransferingScheduler transferringScheduler;
	
	@Produces
	public FileDownloader fileDownloader() {
		return new FileDownloadService(fileRepository, filesystemAccessor, transferringScheduler);
	}
	
	@Produces
	public FileUploader fileUploader() {
		return new FileUploadService(fileRepository, filesystemAccessor, transferringScheduler);
	}
}
