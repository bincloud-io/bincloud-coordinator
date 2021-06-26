package io.bincloud.resources.domain.model.contracts.download;

public interface DownloadVisitor {
	public void onDownloadStart(FileRevisionDescriptor revisionDescriptor);

	public void onDownloadError(FileRevisionDescriptor revisionDescriptor, Exception error);

	public void onDownloadComplete(FileRevisionDescriptor revisionDescriptor);
}
