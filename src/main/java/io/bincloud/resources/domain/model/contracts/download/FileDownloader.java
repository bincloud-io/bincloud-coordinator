package io.bincloud.resources.domain.model.contracts.download;

import java.util.Collection;

import io.bincloud.common.domain.model.io.transfer.DestinationPoint;
import io.bincloud.resources.domain.model.contracts.RevisionPointer;

public interface FileDownloader {

	public DownloadOperation downloadFile(FileDownloadRequest fileDownloadRequest, DownloadListener downloadCallback);
	
	public DownloadOperation downloadFileRanges(FileDownloadRequest fileDownloadRequest, MultiRangeDownloadListener downloadCallback);

	public interface FileDownloadRequest {
		public RevisionPointer getRevision();

		public Collection<Range> getRanges();

		public DestinationPoint getDestinationPoint();
	}
}
