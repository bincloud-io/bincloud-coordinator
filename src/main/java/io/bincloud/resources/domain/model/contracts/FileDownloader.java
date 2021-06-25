package io.bincloud.resources.domain.model.contracts;

import java.util.Collection;
import java.util.Optional;

import io.bincloud.common.domain.model.io.transfer.DestinationPoint;
import io.bincloud.resources.application.download.DownloadOperation;

public interface FileDownloader {

	public DownloadOperation downloadFile(FileDownloadContext fileDownloadRequest, DownloadVisitor downloadCallback);

	public interface FileDownloadContext {
		public RevisionPointer getRevision();

		public Collection<Range> getRanges();

		public DestinationPoint getDestinationPoint();
	}
}
