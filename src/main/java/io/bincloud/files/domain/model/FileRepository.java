package io.bincloud.files.domain.model;

import java.util.Optional;

public interface FileRepository {
	public Optional<File> findById(String fileId);
	public void save(File file);
}
