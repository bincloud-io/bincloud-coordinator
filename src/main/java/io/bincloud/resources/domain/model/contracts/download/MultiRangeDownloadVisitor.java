package io.bincloud.resources.domain.model.contracts.download;

public interface MultiRangeDownloadVisitor extends DownloadVisitor {
	public void onFragmentDownloadComplete(FileRevisionDescriptor revisionDescriptor, Fragment fragment);
}
