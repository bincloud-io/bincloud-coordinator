package io.bincloud.resources.domain.model.contracts;

import java.util.Optional;

import io.bincloud.common.domain.model.io.transfer.DestinationPoint;
import io.bincloud.resources.application.download.DownloadOperation;

public interface FileDownloader {
	public DownloadOperation downloadFile(FileDownloadContext fileDownloadRequest, DownloadCallback downloadCallback);
	
	public DownloadOperation downloadFileRange(FileRangeDownloadContext fileRangeDownloadRequest, DownloadCallback downloadCallback);
	
	public interface FileDownloadContext {
		public Optional<Long> getResourceId();
		public Optional<String> getFileId();
		public DestinationPoint getDestinationPoint();
	}
	
	public interface FileRangeDownloadContext extends FileDownloadContext {
		public Range getRange();
	}
	
	public interface DownloadCallback {
		public void onDownload(DownloadedFile file);
		public void onError(Exception error);
	}
	
	public interface Range {
		public Optional<Long> getStart();
		public Optional<Long> getEnd();
	}
	
	public interface DownloadedFile {
		public String getFileId();
		public String getFileName();
		public Long getTotalLength();
	}
}
