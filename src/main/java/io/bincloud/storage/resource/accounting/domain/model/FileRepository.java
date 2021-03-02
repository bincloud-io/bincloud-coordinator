package io.bincloud.storage.resource.accounting.domain.model;

import java.util.Optional;
import java.util.UUID;

public interface FileRepository {
	public Optional<File> findById(UUID fileId);
	public void save(File file);
}
