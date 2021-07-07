package io.bincloud.resources.domain.model.contracts.download;

import io.bincloud.resources.domain.model.contracts.download.FileDownloader.FileDownloadRequest;

public interface DownloadListener {
	public void onRequestError(FileDownloadRequest request, Exception error);
	
	public void onDownloadStart(DownloadProcessType type, FileRevisionDescriptor revisionDescriptor);

	public void onDownloadError(FileRevisionDescriptor revisionDescriptor, Exception error);

	public void onDownloadComplete(FileRevisionDescriptor revisionDescriptor);
	
	public void onFragmentDownloadStart(FileRevisionDescriptor revisionDescriptor, Fragment fragment);
	
	public void onFragmentDownloadComplete(FileRevisionDescriptor revisionDescriptor, Fragment fragment);
	
	public enum DownloadProcessType {
		FULL_SIZE,
		PARTIAL;
	}
}
