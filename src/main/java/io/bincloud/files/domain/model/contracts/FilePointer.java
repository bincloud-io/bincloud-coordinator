package io.bincloud.files.domain.model.contracts;

import java.util.Optional;

public interface FilePointer {
	public Optional<String> getFilesystemName();
}
