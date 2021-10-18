package io.bincloud.files.application.download;

import io.bincloud.common.domain.model.io.transfer.DestinationPoint;
import io.bincloud.common.domain.model.io.transfer.TransferingScheduler;
import io.bincloud.files.domain.model.File;
import io.bincloud.files.domain.model.FileRepository;
import io.bincloud.files.domain.model.FilesystemAccessor;
import io.bincloud.files.domain.model.contracts.download.DownloadListener;
import io.bincloud.files.domain.model.contracts.download.FileDownloader;
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