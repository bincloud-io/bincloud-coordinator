package io.bincloud.common.domain.model.io.transfer;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CompletionCallbackWrapper implements CompletionCallback {
	private final CompletionCallback wrapped;
	
	@Override
	public void onSuccess() {
		wrapped.onSuccess();
	}

	@Override
	public void onError(Exception error) {
		wrapped.onError(error);
	}

}
