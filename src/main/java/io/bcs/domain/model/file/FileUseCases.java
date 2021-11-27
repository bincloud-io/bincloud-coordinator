package io.bcs.domain.model.file;

import io.bce.promises.Promise;
import io.bcs.domain.model.file.File.CreateFile;

public interface FileUseCases {
    public Promise<String> createFile(CreateFile command);

    public Promise<Void> disposeFile(String storageName);
}
