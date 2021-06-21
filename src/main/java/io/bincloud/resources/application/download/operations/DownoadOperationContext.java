package io.bincloud.resources.application.download.operations;

import io.bincloud.common.domain.model.io.transfer.CompletionCallback;
import io.bincloud.common.domain.model.io.transfer.DestinationPoint;
import io.bincloud.files.domain.model.contracts.FileStorage;
import io.bincloud.resources.application.download.DownloadCompletionCallback;
import io.bincloud.resources.application.download.DownloadFileDescriptor;
import io.bincloud.resources.domain.model.ResourceRepository;
import io.bincloud.resources.domain.model.contracts.FileDownloader.DownloadCallback;
import io.bincloud.resources.domain.model.contracts.FileDownloader.DownloadedFile;
import io.bincloud.resources.domain.model.contracts.FileDownloader.FileDownloadContext;
import io.bincloud.resources.domain.model.file.FileUploadsHistory;
import lombok.Getter;

@Getter
public class DownoadOperationContext {
	private final DestinationPoint destinationPoint;
	private final DownloadedFile downloadedFile;
	private final CompletionCallback downloadCompletionCallback;
	
	public DownoadOperationContext(FileDownloadContext fileDownloadContext, DownloadCallback downloadCallback, FileStorage fileStorage,
			FileUploadsHistory fileUploadHistory, ResourceRepository resourceRepository) {
		super();
		this.destinationPoint = fileDownloadContext.getDestinationPoint();
		this.downloadedFile = new DownloadFileDescriptor(fileDownloadContext, fileStorage, fileUploadHistory, resourceRepository);
		this.downloadCompletionCallback = new DownloadCompletionCallback(downloadedFile, downloadCallback);
	}
	
	public String getFileId() {
		return downloadedFile.getFileId();
	}
}
