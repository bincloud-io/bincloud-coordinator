package io.bcs.files.domain.model;

import java.util.Optional;

public interface FileRepository {
	public Optional<File> findById(String fileId);
//	public Optional<File> findLatestResourceUpload(Long resourceId);
	public void save(File file);
}
