package io.bincloud.resources.application;

import io.bincloud.files.domain.model.contracts.FileStorage;
import io.bincloud.resources.application.download.DownloadOperation;
import io.bincloud.resources.application.download.operations.ErrorSafeDownloadOperation;
import io.bincloud.resources.application.download.operations.FileDownloadOperation;
import io.bincloud.resources.domain.model.ResourceRepository;
import io.bincloud.resources.domain.model.contracts.FileDownloader;
import io.bincloud.resources.domain.model.file.FileUploadsHistory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileDownloadService implements FileDownloader {
	private final ResourceRepository resourceRepository;
	private final FileUploadsHistory fileUploadsHistory;
	private final FileStorage fileStorage;

	@Override
	public DownloadOperation downloadFile(FileDownloadContext fileDownloadRequest, DownloadCallback downloadCallback) {
		return new ErrorSafeDownloadOperation(() -> new FileDownloadOperation(fileDownloadRequest, downloadCallback,
				fileStorage, fileUploadsHistory, resourceRepository), downloadCallback);
	}

	@Override
	public DownloadOperation downloadFileRange(FileRangeDownloadContext fileRangeDownloadRequest,
			DownloadCallback downloadCallback) {
//		DownloadOperation downloadOperation = createFileRangeDownloader(fileRangeDownloadRequest, downloadCallback);
//		downloadOperation = createErrorSafeDownloadFileOperation(downloadOperation, downloadCallback);
//		downloadOperation.downloadFile();
		throw new UnsupportedOperationException();
	}

}