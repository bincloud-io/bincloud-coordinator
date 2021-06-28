package io.bincloud.resources.domain.model.contracts.download;

import io.bincloud.resources.domain.model.contracts.download.FileDownloader.FileDownloadContext;

public interface DownloadVisitor {
	public void onRequestError(FileDownloadContext request, Exception error);
	
	public void onDownloadStart(FileRevisionDescriptor revisionDescriptor);

	public void onDownloadError(FileRevisionDescriptor revisionDescriptor, Exception error);

	public void onDownloadComplete(FileRevisionDescriptor revisionDescriptor);
	
}
