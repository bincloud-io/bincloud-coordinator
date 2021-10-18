package io.bincloud.resources.domain.model.resource.history;

import java.util.Optional;
import java.util.stream.Stream;

public interface UploadedFileRepository {
	public Optional<UploadedFile> findById(Long resourceId, String fileId);
	public Optional<UploadedFile> findLatestResourceUpload(Long resourceId);
	public Stream<UploadedFile> findAllResourceUploads(Long resourceId);
	public void save(UploadedFile fileUploading);
	public void remove(Long resourceId, String fileId);
}
