package io.bincloud.resources.application.download.operations;

import java.util.function.Supplier;

import io.bincloud.resources.application.download.DownloadOperation;
import io.bincloud.resources.domain.model.contracts.FileDownloader.DownloadCallback;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ErrorSafeDownloadOperation implements DownloadOperation {
	private final Supplier<DownloadOperation> unsafeOperationProvider;
	private final DownloadCallback downloadCallback;
	@Override
	public void downloadFile() {
		try {
			DownloadOperation downloadOperation = unsafeOperationProvider.get();
			downloadOperation.downloadFile();
		} catch (Exception error) {
			downloadCallback.onError(error);
		}
	}
}
