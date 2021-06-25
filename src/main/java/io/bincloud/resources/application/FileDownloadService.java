package io.bincloud.resources.application;

import io.bincloud.files.domain.model.contracts.FileStorage;
import io.bincloud.resources.application.download.DownloadOperation;
import io.bincloud.resources.domain.model.ResourceRepository;
import io.bincloud.resources.domain.model.contracts.DownloadVisitor;
import io.bincloud.resources.domain.model.contracts.FileDownloader;
import io.bincloud.resources.domain.model.file.FileUploadsHistory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileDownloadService implements FileDownloader {
	private final ResourceRepository resourceRepository;
	private final FileUploadsHistory fileUploadsHistory;
	private final FileStorage fileStorage;

	@Override
	public DownloadOperation downloadFile(FileDownloadContext fileDownloadRequest, DownloadVisitor downloadCallback) {
		return null;
	}
}