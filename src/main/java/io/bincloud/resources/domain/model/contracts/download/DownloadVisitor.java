package io.bincloud.resources.domain.model.contracts.download;

import io.bincloud.resources.domain.model.contracts.Fragment;

public interface DownloadVisitor {
	public void onStart(FileRevisionDescriptor revisionDescriptor);
	
	public void onFragmentComplete(FileRevisionDescriptor revisionDescriptor, Fragment fragment);
	
	public void onError(FileRevisionDescriptor revisionDescriptor, Exception error);
	
	public void onEnd(FileRevisionDescriptor revisionDescriptor);
}
