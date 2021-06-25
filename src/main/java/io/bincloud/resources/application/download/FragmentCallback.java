package io.bincloud.resources.application.download;

import io.bincloud.common.domain.model.io.transfer.CompletionCallback;
import io.bincloud.resources.domain.model.contracts.DownloadVisitor;
import io.bincloud.resources.domain.model.contracts.Fragment;
import io.bincloud.resources.domain.model.file.FileRevisionDescriptor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FragmentCallback implements CompletionCallback {
	private final FileRevisionDescriptor fileRevisionDescriptor;
	private final DownloadVisitor downloadVisitor;
	private final Fragment fragment;
	
	@Override
	public void onSuccess() {
		downloadVisitor.onFragmentComplete(fileRevisionDescriptor, fragment);
	}

	@Override
	public void onError(Exception error) {
		downloadVisitor.onError(fileRevisionDescriptor, error);
	}
}
