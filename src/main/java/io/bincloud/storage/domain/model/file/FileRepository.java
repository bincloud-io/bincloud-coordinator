package io.bincloud.storage.domain.model.file;

import java.util.Optional;

public interface FileRepository {
	public Optional<File> findById(String fileId);
	public void save(File file);
}
