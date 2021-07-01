package io.bincloud.resources.domain.model.contracts.download;

import io.bincloud.resources.domain.model.contracts.download.FileDownloader.FileDownloadRequest;

public interface DownloadListener {
	public void onRequestError(FileDownloadRequest request, Exception error);
	
	public void onDownloadStart(FileRevisionDescriptor revisionDescriptor);

	public void onDownloadError(FileRevisionDescriptor revisionDescriptor, Exception error);

	public void onDownloadComplete(FileRevisionDescriptor revisionDescriptor);
	
}
