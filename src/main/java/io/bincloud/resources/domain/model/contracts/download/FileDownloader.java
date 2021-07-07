package io.bincloud.resources.domain.model.contracts.download;

import java.util.Collection;

import io.bincloud.common.domain.model.io.transfer.DestinationPoint;
import io.bincloud.resources.domain.model.contracts.RevisionPointer;

public interface FileDownloader {

	public void downloadFile(FileDownloadRequest fileDownloadRequest, DownloadListener downloadCallback);

	public interface FileDownloadRequest {
		public RevisionPointer getRevision();
		public DownloadRequestDetails getRequestDetails();
		public DestinationPoint getDestinationPoint();
	}

	public interface DownloadRequestDetails {
		public Collection<Range> getRanges();
	}
}
