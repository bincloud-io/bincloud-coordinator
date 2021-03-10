package io.bincloud.common.io.transfer;

public interface CompletionCallback {
	public void onSuccess();
	public void onError(Exception error);
}
