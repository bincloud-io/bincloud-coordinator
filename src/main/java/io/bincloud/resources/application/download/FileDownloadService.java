package io.bincloud.resources.application.download;

import io.bincloud.common.domain.model.io.transfer.DestinationPoint;
import io.bincloud.files.domain.model.contracts.FileStorage;
import io.bincloud.resources.domain.model.ResourceRepository;
import io.bincloud.resources.domain.model.contracts.RevisionPointer;
import io.bincloud.resources.domain.model.contracts.download.DownloadListener;
import io.bincloud.resources.domain.model.contracts.download.FileDownloader;
import io.bincloud.resources.domain.model.file.FileUploadsHistory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileDownloadService implements FileDownloader {
	private final ResourceRepository resourceRepository;
	private final FileUploadsHistory fileUploadsHistory;
	private final FileStorage fileStorage;

	@Override
	public void downloadFile(FileDownloadRequest fileDownloadRequest, DownloadListener downloadCallback) {
		try {
			DestinationPoint destinationPoint = fileDownloadRequest.getDestinationPoint();
			DownloadRequestDetails requestDetails = fileDownloadRequest.getRequestDetails();
			FileRevisionAccessor revisionAccessor = createFileRevisionAccessor(fileDownloadRequest);
			revisionAccessor.downloadContent(requestDetails, destinationPoint, downloadCallback);
		} catch (Exception error) {
			downloadCallback.onRequestError(fileDownloadRequest, error);
		}
	}
	
	private FileRevisionAccessor createFileRevisionAccessor(FileDownloadRequest fileDownloadRequest) {
		RevisionPointer revisionPointer = fileDownloadRequest.getRevision();
		return new FileRevisionAccessor(revisionPointer, fileStorage, fileUploadsHistory, resourceRepository);
	}
}