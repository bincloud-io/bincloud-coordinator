package io.bcs.storage.domain.model.contracts;

import java.util.Optional;

import io.bcs.storage.domain.model.FileId;

public interface FilePointer {
	public Optional<FileId> getFilesystemName();
}
