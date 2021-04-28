package io.bincloud.storage.port.adapter.file;

import java.util.Optional;

import io.bincloud.storage.domain.model.file.File;
import io.bincloud.storage.domain.model.file.FileRepository;

public class JPAFileRepository implements FileRepository {

	@Override
	public Optional<File> findById(String fileId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void save(File file) {
		throw new UnsupportedOperationException();
	}
}
