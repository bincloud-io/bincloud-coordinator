package io.bincloud.resources.application.download.operations;

import io.bincloud.files.domain.model.contracts.FileStorage;
import io.bincloud.resources.application.download.DownloadOperation;
import io.bincloud.resources.domain.model.ResourceRepository;
import io.bincloud.resources.domain.model.contracts.FileDownloader.DownloadCallback;
import io.bincloud.resources.domain.model.contracts.FileDownloader.FileRangeDownloadContext;
import io.bincloud.resources.domain.model.file.FileUploadsHistory;

public class FileRangeDownloadOperation implements DownloadOperation {
	private final DownoadOperationContext context;
	private final DownloadRange downloadRange;
	private final FileStorage fileStorage;

	public FileRangeDownloadOperation(FileRangeDownloadContext fileDownloadContext, DownloadCallback downloadCallback,
			FileStorage fileStorage, FileUploadsHistory fileUploadHistory, ResourceRepository resourceRepository) {
		super();
		this.context = new DownoadOperationContext(fileDownloadContext, downloadCallback, fileStorage,
				fileUploadHistory, resourceRepository);
		this.downloadRange = new DownloadRange(fileDownloadContext.getRange(), context.getFileLength());
		this.fileStorage = fileStorage;
	}

	@Override
	public void downloadFile() {
		fileStorage.downloadFileRange(context.getFileId(), context.getDestinationPoint(),
				context.getDownloadCompletionCallback(), downloadRange.getStartPosition(), downloadRange.getSize());
	}

}
