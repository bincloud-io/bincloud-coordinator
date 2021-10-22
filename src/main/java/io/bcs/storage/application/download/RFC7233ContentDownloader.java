package io.bcs.storage.application.download;

import io.bcs.common.domain.model.io.transfer.CompletionCallback;
import io.bcs.common.domain.model.io.transfer.DestinationPoint;
import io.bcs.common.domain.model.io.transfer.TransferingScheduler;
import io.bcs.storage.domain.model.FileRevision;
import io.bcs.storage.domain.model.FilesystemAccessor;
import io.bcs.storage.domain.model.contexts.FileDownloadingContext;
import io.bcs.storage.domain.model.contracts.download.DownloadListener;
import io.bcs.storage.domain.model.contracts.download.Fragment;
import io.bcs.storage.domain.model.contracts.download.DownloadListener.DownloadProcessType;
import io.bcs.storage.domain.model.contracts.download.FileDownloader.DownloadRequestDetails;
import lombok.RequiredArgsConstructor;

public class RFC7233ContentDownloader {
	private final FileRevision file;
	private final FilesystemAccessor filesystemAccessor;
	private final TransferingScheduler contentTransferScheduler;

	public RFC7233ContentDownloader(FileRevision file, FilesystemAccessor filesystemAccessor,
			TransferingScheduler contentTransferScheduler) {
		super();
		this.file = file;
		this.filesystemAccessor = filesystemAccessor;
		this.contentTransferScheduler = contentTransferScheduler;
	}

	public void downloadContent(DownloadRequestDetails downloadRequest, DestinationPoint destinationPoint,
			DownloadListener downloadListener) {
		try {
			startDownloadProcess(downloadRequest, downloadListener);
			transferFileContent(downloadRequest, destinationPoint, downloadListener);
		} catch (ContentPartTransferringFailedException error) {
			failDownloadProcess(downloadListener, error.getCauseError());
		} catch (Exception error) {
			failDownloadProcess(downloadListener, error);
		}
	}

	private void transferFileContent(DownloadRequestDetails downloadRequest, DestinationPoint destinationPoint,
			DownloadListener downloadListener) {
		FileFragments requestedFragments = createFileFragments(downloadRequest);
		if (requestedFragments.isRequestedMultipleFragments()) {
			transferPartialContent(requestedFragments, destinationPoint, downloadListener);
			completeDownloadProcess(downloadListener, requestedFragments.getTotalSize());
		} else {
			transferSinglePartContent(requestedFragments, destinationPoint, downloadListener);
		}
	}

	private void transferPartialContent(FileFragments requestedFragments, DestinationPoint destinationPoint,
			DownloadListener downloadListener) {
		for (Fragment fragment : requestedFragments.getParts()) {
			startFragmentDownloadProcess(downloadListener, fragment);
			transferFragmentContent(fragment, destinationPoint, downloadListener);
		}
	}

	private void transferSinglePartContent(FileFragments fragments, DestinationPoint destinationPoint,
			DownloadListener downloadListener) {
		CompletionCallback completionCallback = new SinglePartContentDownloadCallback(downloadListener,
				fragments.getTotalSize());
		downloadFileContent(fragments.getSinglePart(), destinationPoint, completionCallback);
	}

	private void transferFragmentContent(Fragment fragment, DestinationPoint destinationPoint,
			DownloadListener downloadListener) {
		CompletionCallback completionCallback = new PartialContentDownloadCallback(fragment, downloadListener);
		downloadFileContent(fragment, destinationPoint, completionCallback);
	}

	private void downloadFileContent(Fragment fragment, DestinationPoint destination,
			CompletionCallback completionCallback) {
		FileDownloadingContext downloadContext = new FileDownloadingContext(destination, contentTransferScheduler,
				filesystemAccessor, completionCallback);
		file.downloadFileContent(downloadContext, fragment.getStart(), fragment.getSize());
	}

	private void startDownloadProcess(DownloadRequestDetails requestDetails, DownloadListener downloadListener) {
		if (createFileFragments(requestDetails).isRequestedMultipleFragments()) {
			downloadListener.onDownloadStart(DownloadProcessType.PARTIAL, file);
		} else {
			downloadListener.onDownloadStart(DownloadProcessType.FULL_SIZE, file);
		}
	}

	private FileFragments createFileFragments(DownloadRequestDetails requestDetails) {
		return new FileFragments(requestDetails.getRanges(), file.getFileSize());
	}

	private void completeDownloadProcess(DownloadListener downloadListener, Long totalSize) {
		downloadListener.onDownloadComplete(file, totalSize);
	}

	private void failDownloadProcess(DownloadListener downloadListener, Exception error) {
		downloadListener.onDownloadError(file, error);
	}

	private void startFragmentDownloadProcess(DownloadListener downloadListener, Fragment fragment) {
		downloadListener.onFragmentDownloadStart(file, fragment);
	}

	private void completeFragmentDownloadProcess(DownloadListener downloadListener, Fragment fragment) {
		downloadListener.onFragmentDownloadComplete(file, fragment);
	}

	@RequiredArgsConstructor
	private class SinglePartContentDownloadCallback implements CompletionCallback {
		private final DownloadListener downloadListener;
		private final Long totalSize;

		@Override
		public void onSuccess() {
			completeDownloadProcess(downloadListener, totalSize);
		}

		@Override
		public void onError(Exception error) {
			failDownloadProcess(downloadListener, error);
		}
	}

	@RequiredArgsConstructor
	private class PartialContentDownloadCallback implements CompletionCallback {
		private final Fragment fragment;
		private final DownloadListener downloadListener;

		@Override
		public void onSuccess() {
			completeFragmentDownloadProcess(downloadListener, fragment);
		}

		@Override
		public void onError(Exception error) {
			throw new ContentPartTransferringFailedException(error);
		}
	}

	private class ContentPartTransferringFailedException extends RuntimeException {
		private static final long serialVersionUID = 5750180811153214306L;

		public ContentPartTransferringFailedException(Exception cause) {
			super(cause);
		}

		public Exception getCauseError() {
			return (Exception) super.getCause();
		}
	}
}
