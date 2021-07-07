package io.bincloud.resources.application.download;

import java.util.Collection;

import io.bincloud.common.domain.model.io.transfer.CompletionCallback;
import io.bincloud.common.domain.model.io.transfer.DestinationPoint;
import io.bincloud.files.domain.model.contracts.FileStorage;
import io.bincloud.resources.domain.model.ResourceRepository;
import io.bincloud.resources.domain.model.contracts.RevisionPointer;
import io.bincloud.resources.domain.model.contracts.download.DownloadListener;
import io.bincloud.resources.domain.model.contracts.download.FileDownloader.DownloadRequestDetails;
import io.bincloud.resources.domain.model.contracts.download.FileRevisionDescriptor;
import io.bincloud.resources.domain.model.contracts.download.Fragment;
import io.bincloud.resources.domain.model.file.FileUploadsHistory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class FileRevisionAccessor {
	private final FileStorage fileStorage;
	@Getter
	private final FileRevisionDescriptor revisionDescriptor;

	public FileRevisionAccessor(RevisionPointer revisionPointer, FileStorage fileStorage,
			FileUploadsHistory fileUploadHistory, ResourceRepository resourceRepository) {
		super();
		this.fileStorage = fileStorage;
		this.revisionDescriptor = new StoredFileRevisionDescriptor(revisionPointer, fileStorage, fileUploadHistory,
				resourceRepository);
	}

	public void download(DownloadRequestDetails downloadRequest, DestinationPoint destinationPoint,
			DownloadListener downloadListener) {
		try {
			startDownloadProcess(downloadListener);
			transferFileContent(downloadRequest, destinationPoint, downloadListener);
		} catch (ContentPartTransferringFailedException error) {
			failDownloadProcess(downloadListener, error.getCauseError());
		} catch (Exception error) {
			failDownloadProcess(downloadListener, error);
		}
	}

	private void transferFileContent(DownloadRequestDetails downloadRequest, DestinationPoint destinationPoint,
			DownloadListener downloadListener) {
		FileFragments requestedFragments = new FileFragments(downloadRequest.getRanges(),
				revisionDescriptor.getFileSize());
		if (requestedFragments.isRequestedMultipleFragments()) {
			transferPartialContent(requestedFragments.getParts(), destinationPoint, downloadListener);
			completeDownloadProcess(downloadListener);
		} else {
			transferSizedContent(requestedFragments.getSinglePart(), destinationPoint, downloadListener);
		}
	}

	private void transferPartialContent(Collection<Fragment> requestedFragments, DestinationPoint destinationPoint,
			DownloadListener downloadListener) {
		for (Fragment fragment : requestedFragments) {
			startFragmentDownloadProcess(downloadListener, fragment);
			transferFragmentContent(fragment, destinationPoint, downloadListener);
		}
	}

	private void transferSizedContent(Fragment fragment, DestinationPoint destinationPoint,
			DownloadListener downloadListener) {
		CompletionCallback completionCallback = new SizedContentDownloadCallback(downloadListener);
		fileStorage.downloadFileContent(getFileId(), destinationPoint, completionCallback, fragment.getStart(),
				fragment.getSize());
	}

	private void transferFragmentContent(Fragment fragment, DestinationPoint destinationPoint,
			DownloadListener downloadListener) {
		CompletionCallback completionCallback = new PartialContentDownloadCallback(fragment, downloadListener);
		fileStorage.downloadFileContent(getFileId(), destinationPoint, completionCallback, fragment.getStart(),
				fragment.getSize());
	}

	private String getFileId() {
		return revisionDescriptor.getFileId();
	}

	private void startDownloadProcess(DownloadListener downloadListener) {
		downloadListener.onDownloadStart(revisionDescriptor);
	}

	private void completeDownloadProcess(DownloadListener downloadListener) {
		downloadListener.onDownloadComplete(revisionDescriptor);
	}

	private void failDownloadProcess(DownloadListener downloadListener, Exception error) {
		downloadListener.onDownloadError(revisionDescriptor, error);
	}

	private void startFragmentDownloadProcess(DownloadListener downloadListener, Fragment fragment) {
		downloadListener.onFragmentDownloadStart(revisionDescriptor, fragment);
	}

	private void completeFragmentDownloadProcess(DownloadListener downloadListener, Fragment fragment) {
		downloadListener.onFragmentDownloadComplete(revisionDescriptor, fragment);
	}

	@RequiredArgsConstructor
	private class SizedContentDownloadCallback implements CompletionCallback {
		private final DownloadListener downloadListener;

		@Override
		public void onSuccess() {
			completeDownloadProcess(downloadListener);
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
