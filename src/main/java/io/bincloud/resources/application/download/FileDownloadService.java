package io.bincloud.resources.application.download;

import java.util.Collection;

import io.bincloud.common.domain.model.io.transfer.DestinationPoint;
import io.bincloud.files.domain.model.contracts.FileStorage;
import io.bincloud.resources.domain.model.ResourceRepository;
import io.bincloud.resources.domain.model.contracts.RevisionPointer;
import io.bincloud.resources.domain.model.contracts.download.DownloadOperation;
import io.bincloud.resources.domain.model.contracts.download.DownloadListener;
import io.bincloud.resources.domain.model.contracts.download.FileDownloader;
import io.bincloud.resources.domain.model.contracts.download.MultiRangeDownloadListener;
import io.bincloud.resources.domain.model.contracts.download.Range;
import io.bincloud.resources.domain.model.file.FileUploadsHistory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileDownloadService implements FileDownloader {
	private final ResourceRepository resourceRepository;
	private final FileUploadsHistory fileUploadsHistory;
	private final FileStorage fileStorage;

	@Override
	public DownloadOperation downloadFile(FileDownloadRequest fileDownloadRequest, DownloadListener downloadCallback) {
		return () -> {
			try {
				DestinationPoint destinationPoint = fileDownloadRequest.getDestinationPoint();
				FileRevisionAccessor revisionAccessor = createFileRevisionAccessor(fileDownloadRequest);
				revisionAccessor.download(destinationPoint, downloadCallback).downloadFile();
			} catch (Exception error) {
				downloadCallback.onRequestError(fileDownloadRequest, error);
			}
		};
	}

	@Override
	public DownloadOperation downloadFileRanges(FileDownloadRequest fileDownloadRequest, MultiRangeDownloadListener downloadCallback) {
		return () -> {
			try {
				Collection<Range> ranges = fileDownloadRequest.getRanges();
				DestinationPoint destinationPoint = fileDownloadRequest.getDestinationPoint();
				FileRevisionAccessor revisionAccessor = createFileRevisionAccessor(fileDownloadRequest);
				revisionAccessor.download(ranges, destinationPoint, downloadCallback).downloadFile();
			} catch (Exception error) {
				downloadCallback.onRequestError(fileDownloadRequest, error);
			}
		};
	}
	
	private FileRevisionAccessor createFileRevisionAccessor(FileDownloadRequest fileDownloadRequest) {
		RevisionPointer revisionPointer = fileDownloadRequest.getRevision();
		return new FileRevisionAccessor(revisionPointer, fileStorage, fileUploadsHistory, resourceRepository);
	}
}