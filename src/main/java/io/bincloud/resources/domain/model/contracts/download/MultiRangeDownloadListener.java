package io.bincloud.resources.domain.model.contracts.download;

public interface MultiRangeDownloadListener extends DownloadListener {
	public void onFragmentDownloadComplete(FileRevisionDescriptor revisionDescriptor, Fragment fragment);
}
