package io.bincloud.resources.domain.model.contracts.download;

import io.bincloud.resources.domain.model.contracts.Fragment;

public interface MultiRangeDownloadVisitor extends DownloadVisitor {
	public void onFragmentDownloadComplete(FileRevisionDescriptor revisionDescriptor, Fragment fragment);
}
