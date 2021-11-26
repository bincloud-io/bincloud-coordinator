package io.bcs.domain.model.file;

import java.util.Optional;

public interface FileRepository {
    public Optional<File> findById(String storageFileName);

    public void save(File file);
}
