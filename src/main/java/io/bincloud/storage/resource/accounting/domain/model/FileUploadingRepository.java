package io.bincloud.storage.resource.accounting.domain.model;

import java.util.Optional;
import java.util.UUID;

public interface FileUploadingRepository {
	public Optional<FileUploading> findById(Long resourceId, UUID fileId);
	public void save(FileUploading fileUploading);
}
