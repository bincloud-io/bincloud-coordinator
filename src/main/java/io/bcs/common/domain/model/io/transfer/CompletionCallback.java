package io.bcs.common.domain.model.io.transfer;

public interface CompletionCallback {
	public void onSuccess();
	public void onError(Exception error);
}
