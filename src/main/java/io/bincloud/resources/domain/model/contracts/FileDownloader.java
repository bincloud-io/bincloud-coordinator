package io.bincloud.resources.domain.model.contracts;

import java.util.Optional;

import io.bincloud.common.domain.model.io.transfer.DestinationPoint;

public interface FileDownloader {
	public void downloadFile(FileDownloadContext fileDownloadRequest, DownloadCallback downloadCallback);
	
	public interface FileDownloadContext {
		public Optional<Long> getResourceId();
		public Optional<String> getFileId();
		public DestinationPoint getDestinationPoint();
	}
	
	
	public interface DownloadCallback {
		public void onDownload(DownloadedFile file);
		public void onError(Exception error);
	}
	
	public interface DownloadedFile {
		public String getFileId();
		public Long getFileSize();
	}
}
