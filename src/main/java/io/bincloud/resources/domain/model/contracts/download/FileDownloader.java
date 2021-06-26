package io.bincloud.resources.domain.model.contracts.download;

import java.util.Collection;

import io.bincloud.common.domain.model.io.transfer.DestinationPoint;
import io.bincloud.resources.domain.model.contracts.Range;

public interface FileDownloader {

	public DownloadOperation downloadFile(FileDownloadContext fileDownloadRequest, MultiRangeDownloadVisitor downloadCallback);

	public interface FileDownloadContext {
		public RevisionPointer getRevision();

		public Collection<Range> getRanges();

		public DestinationPoint getDestinationPoint();
	}
}
