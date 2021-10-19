package io.bcs.storage.domain.model.contracts.download;

import io.bcs.storage.domain.model.contracts.FileDescriptor;
import io.bcs.storage.domain.model.contracts.download.FileDownloader.FileDownloadRequest;

public interface DownloadListener {
	public void onRequestError(FileDownloadRequest request, Exception error);
	
	public void onDownloadStart(DownloadProcessType type, FileDescriptor revisionDescriptor);

	public void onDownloadError(FileDescriptor revisionDescriptor, Exception error);

	public void onDownloadComplete(FileDescriptor revisionDescriptor, Long totalSize);
	
	public void onFragmentDownloadStart(FileDescriptor revisionDescriptor, Fragment fragment);
	
	public void onFragmentDownloadComplete(FileDescriptor revisionDescriptor, Fragment fragment);
	
	public enum DownloadProcessType {
		FULL_SIZE,
		PARTIAL;
	}
}
