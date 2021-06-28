package io.bincloud.resources.application.download;

import io.bincloud.common.domain.model.io.transfer.CompletionCallback;
import io.bincloud.resources.domain.model.contracts.download.MultiRangeDownloadVisitor;
import io.bincloud.resources.domain.model.contracts.download.FileRevisionDescriptor;
import io.bincloud.resources.domain.model.contracts.download.Fragment;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FragmentCallback implements CompletionCallback {
	private final FileRevisionDescriptor fileRevisionDescriptor;
	private final MultiRangeDownloadVisitor downloadVisitor;
	private final Fragment fragment;
	
	@Override
	public void onSuccess() {
		downloadVisitor.onFragmentDownloadComplete(fileRevisionDescriptor, fragment);
	}

	@Override
	public void onError(Exception error) {
		downloadVisitor.onDownloadError(fileRevisionDescriptor, error);
	}
}
