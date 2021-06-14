package io.bincloud.resources.domain.model.file;

import java.util.Optional;
import java.util.stream.Stream;

public interface FileUploadsRepository {
	public Optional<FileUpload> findById(Long resourceId, String fileId);
	public Optional<FileUpload> findLatestResourceUpload(Long resourceId);
	public Stream<FileUpload> findAllResourceUploads(Long resourceId);
	public void save(FileUpload fileUploading);
	public void remove(Long resourceId, String fileId);
}
