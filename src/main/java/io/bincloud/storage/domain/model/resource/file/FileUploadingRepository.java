package io.bincloud.storage.domain.model.resource.file;

import java.util.Optional;

public interface FileUploadingRepository {
	public Optional<FileUploading> findById(Long resourceId, String fileId);
	public Optional<FileUploading> findLatestResourceUploading(Long resourceId);
	public void save(FileUploading fileUploading);
	public void remove(FileUploadingId id);
}
