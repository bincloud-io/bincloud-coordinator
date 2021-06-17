package io.bincloud.resources.application;

import java.util.function.Supplier;

import io.bincloud.common.domain.model.io.transfer.CompletionCallback;
import io.bincloud.common.domain.model.io.transfer.DestinationPoint;
import io.bincloud.files.domain.model.FileDescriptor;
import io.bincloud.files.domain.model.contracts.FileStorage;
import io.bincloud.resources.application.providers.ExistingFileDescriptorProvider;
import io.bincloud.resources.application.providers.ExistingResourceProvider;
import io.bincloud.resources.application.providers.SpecifiedFileIdentifierProvider;
import io.bincloud.resources.application.providers.UnspecifiedFileIdentifierProvider;
import io.bincloud.resources.domain.model.Resource;
import io.bincloud.resources.domain.model.ResourceRepository;
import io.bincloud.resources.domain.model.contracts.FileDownloader;
import io.bincloud.resources.domain.model.file.FileUploadsHistory;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileDownloadService implements FileDownloader {
	private final ResourceRepository resourceRepository;
	private final FileUploadsHistory fileUploadsHistory;
	private final FileStorage fileStorage;

	@Override
	public void downloadFile(FileDownloadContext fileDownloadRequest, DownloadCallback downloadCallback) {
		DownloadOperation downloadOperation = createWholeFileDownloader(fileDownloadRequest, downloadCallback);
		downloadOperation = createErrorSafeDownloadFileOperation(downloadOperation, downloadCallback);
		downloadOperation.downloadFile();
	}

	private DownloadOperation createWholeFileDownloader(FileDownloadContext fileDownloadRequest, DownloadCallback downloadCallback) {
		return () -> {
			DestinationPoint destinationPoint = fileDownloadRequest.getDestinationPoint();
			DownloadedFile downloadFileDescriptor = new DownloadFileDescriptor(fileDownloadRequest);
			CompletionCallback completionCallback = new DownloadCompletionCallback(downloadFileDescriptor,downloadCallback);
			fileStorage.downloadFile(downloadFileDescriptor.getFileId(), destinationPoint, completionCallback);
		};
	}

	private DownloadOperation createErrorSafeDownloadFileOperation(DownloadOperation downloadOperation,
			DownloadCallback downloadCallback) {
		return () -> {
			try {
				downloadOperation.downloadFile();
			} catch (Exception error) {
				downloadCallback.onError(error);
			}
		};
	}

	@FunctionalInterface
	private interface DownloadOperation {
		public void downloadFile();
	}

	@RequiredArgsConstructor
	private class DownloadCompletionCallback implements CompletionCallback {
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

	@EqualsAndHashCode(onlyExplicitlyIncluded = true)
	private class DownloadFileDescriptor implements FileDownloader.DownloadedFile {
		@Getter
		@EqualsAndHashCode.Include
		private final String fileId;
		private final FileDescriptor fileDescriptor;
		private final Resource resource;

		public DownloadFileDescriptor(FileDownloadContext fileDownloadContext) {
			super();
			
			this.resource = createResourceProvider(fileDownloadContext).get();
			this.fileId = createFileIdentifierProvider(fileDownloadContext).get();
			Supplier<FileDescriptor> fileDescriptorProvider = new ExistingFileDescriptorProvider(fileId, fileStorage);
			this.fileDescriptor = fileDescriptorProvider.get();
		}

		@Override
		public Long getFileSize() {
			return fileDescriptor.getSize();
		}
		
		@Override
		public String getFileName() {
			return resource.getFileName();
		}

		private Supplier<Resource> createResourceProvider(FileDownloadContext fileDownloadContext) {
			return new ExistingResourceProvider(fileDownloadContext.getResourceId(), resourceRepository);
		}

		private Supplier<String> createFileIdentifierProvider(FileDownloadContext fileDownloadContext) {
			return fileDownloadContext.getFileId().<Supplier<String>>map(
					fileId -> new SpecifiedFileIdentifierProvider(() -> resource.getId(), fileId, fileUploadsHistory))
					.orElse(new UnspecifiedFileIdentifierProvider(() -> resource.getId(), fileUploadsHistory));
		}		
	}
}