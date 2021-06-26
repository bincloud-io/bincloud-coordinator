package io.bincloud.resources.application;

import io.bincloud.files.domain.model.contracts.FileStorage;
import io.bincloud.resources.domain.model.ResourceRepository;
import io.bincloud.resources.domain.model.contracts.download.DownloadOperation;
import io.bincloud.resources.domain.model.contracts.download.MultiRangeDownloadVisitor;
import io.bincloud.resources.domain.model.contracts.download.FileDownloader;
import io.bincloud.resources.domain.model.file.FileUploadsHistory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileDownloadService implements FileDownloader {
	private final ResourceRepository resourceRepository;
	private final FileUploadsHistory fileUploadsHistory;
	private final FileStorage fileStorage;

	@Override
	public DownloadOperation downloadFile(FileDownloadContext fileDownloadRequest, MultiRangeDownloadVisitor downloadCallback) {
		return null;
	}
}