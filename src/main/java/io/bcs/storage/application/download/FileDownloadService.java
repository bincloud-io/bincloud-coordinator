package io.bcs.storage.application.download;

import io.bcs.common.domain.model.io.transfer.DestinationPoint;
import io.bcs.common.domain.model.io.transfer.TransferingScheduler;
import io.bcs.storage.domain.model.File;
import io.bcs.storage.domain.model.FileRepository;
import io.bcs.storage.domain.model.FilesystemAccessor;
import io.bcs.storage.domain.model.contracts.download.DownloadListener;
import io.bcs.storage.domain.model.contracts.download.FileDownloader;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileDownloadService implements FileDownloader {
	private final FileRepository fileRepository;
	private final FilesystemAccessor filesystemAccessor;
	private final TransferingScheduler transferingScheduler;

	@Override
	public void downloadFile(FileDownloadRequest fileDownloadRequest, DownloadListener downloadListener) {
		try {
			DestinationPoint destinationPoint = fileDownloadRequest.getDestinationPoint();
			DownloadRequestDetails requestDetails = fileDownloadRequest.getRequestDetails();
			File file = new FileForDownloadProvider(fileDownloadRequest.getFile(), fileRepository).get();
			RFC7233ContentDownloader revisionAccessor = new RFC7233ContentDownloader(file, filesystemAccessor, transferingScheduler);
			revisionAccessor.downloadContent(requestDetails, destinationPoint, downloadListener);
		} catch (Exception error) {
			downloadListener.onRequestError(fileDownloadRequest, error);
		}
	}	
}