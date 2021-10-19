package io.bcs.files.domain.model.contracts.download;

import java.util.Collection;

import io.bcs.common.domain.model.io.transfer.DestinationPoint;
import io.bcs.files.domain.model.contracts.FilePointer;

public interface FileDownloader {

	public void downloadFile(FileDownloadRequest fileDownloadRequest, DownloadListener downloadCallback);

	public interface FileDownloadRequest {
		public FilePointer getFile();
		public DownloadRequestDetails getRequestDetails();
		public DestinationPoint getDestinationPoint();
	}

	public interface DownloadRequestDetails {
		public Collection<Range> getRanges();
	}
}
