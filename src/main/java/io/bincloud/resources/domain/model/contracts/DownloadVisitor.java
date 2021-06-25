package io.bincloud.resources.domain.model.contracts;

import io.bincloud.resources.domain.model.file.FileRevisionDescriptor;

public interface DownloadVisitor {
	public void onStart(FileRevisionDescriptor revisionDescriptor);
	
	public void onFragmentComplete(FileRevisionDescriptor revisionDescriptor, Fragment fragment);
	
	public void onError(FileRevisionDescriptor revisionDescriptor, Exception error);
	
	public void onEnd(FileRevisionDescriptor revisionDescriptor);
}
