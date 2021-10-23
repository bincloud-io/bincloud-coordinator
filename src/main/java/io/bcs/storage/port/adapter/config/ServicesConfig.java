package io.bcs.storage.port.adapter.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.bce.interaction.streaming.Streamer;
import io.bcs.common.domain.model.generator.SequentialGenerator;
import io.bcs.common.domain.model.io.transfer.TransferingScheduler;
import io.bcs.storage.application.download.FileDownloadService;
import io.bcs.storage.application.upload.FileUploadService;
import io.bcs.storage.domain.model.FileRevisionRepository;
import io.bcs.storage.domain.model.FilesystemAccessor;
import io.bcs.storage.domain.model.contracts.download.FileDownloader;
import io.bcs.storage.domain.model.contracts.upload.FileUploader;

@ApplicationScoped
public class ServicesConfig {
	@Inject
	private FilesystemAccessor filesystemAccessor;
	
	@Inject
	private FileRevisionRepository fileRepository;
	
	@Inject
	@FileIdGenerator
	private SequentialGenerator<String> fileIdGenerator;

	@Inject
	private TransferingScheduler transferringScheduler;
	
	@Inject
	private Streamer dataStreamer;
	
	@Produces
	public FileDownloader fileDownloader() {
		return new FileDownloadService(fileRepository, filesystemAccessor, transferringScheduler);
	}
	
	@Produces
	public FileUploader fileUploader() {
//		return new FileUploadService(fileRepository, filesystemAccessor, transferringScheduler, dataStreamer);
		return new FileUploadService(fileRepository, filesystemAccessor, dataStreamer);
	}
}
