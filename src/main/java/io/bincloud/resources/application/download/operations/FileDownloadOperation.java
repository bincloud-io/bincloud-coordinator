package io.bincloud.resources.application.download.operations;

import io.bincloud.files.domain.model.contracts.FileStorage;
import io.bincloud.resources.application.download.DownloadOperation;
import io.bincloud.resources.domain.model.ResourceRepository;
import io.bincloud.resources.domain.model.contracts.FileDownloader.DownloadCallback;
import io.bincloud.resources.domain.model.contracts.FileDownloader.FileDownloadContext;
import io.bincloud.resources.domain.model.file.FileUploadsHistory;

public class FileDownloadOperation implements DownloadOperation {
	private final DownoadOperationContext context;
	private final FileStorage fileStorage;

	public FileDownloadOperation(FileDownloadContext fileDownloadContext, DownloadCallback downloadCallback,
			FileStorage fileStorage, FileUploadsHistory fileUploadHistory, ResourceRepository resourceRepository) {
		super();
		this.context = new DownoadOperationContext(fileDownloadContext, downloadCallback, fileStorage,
				fileUploadHistory, resourceRepository);
		this.fileStorage = fileStorage;
	}

	@Override
	public void downloadFile() {
		fileStorage.downloadFile(context.getFileId(), context.getDestinationPoint(), context.getDownloadCompletionCallback());
	}
}
