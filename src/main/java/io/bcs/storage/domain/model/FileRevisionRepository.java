package io.bcs.storage.domain.model;

import java.util.Optional;

public interface FileRevisionRepository {
	public Optional<FileRevision> findById(FileId fileId);
	public void save(FileRevision file);
}
