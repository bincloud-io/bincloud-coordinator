package io.bincloud.resources.application.download;

import io.bincloud.common.domain.model.io.transfer.CompletionCallback;
import io.bincloud.resources.domain.model.contracts.FileDownloader.DownloadCallback;
import io.bincloud.resources.domain.model.contracts.FileDownloader.DownloadedFile;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DownloadCompletionCallback implements CompletionCallback {
	private final DownloadedFile downloadedFile;
	private final DownloadCallback downloadCallback;

	@Override
	public void onSuccess() {
		downloadCallback.onDownload(downloadedFile);
	}

	@Override
	public void onError(Exception error) {
		downloadCallback.onError(error);
	}
}
