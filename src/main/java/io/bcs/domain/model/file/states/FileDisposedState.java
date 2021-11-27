package io.bcs.domain.model.file.states;

import java.util.Collection;

import io.bce.promises.Promise;
import io.bcs.domain.model.ContentFragment;
import io.bcs.domain.model.FileStorage;
import io.bcs.domain.model.file.ContentUploader;
import io.bcs.domain.model.file.FileContent;
import io.bcs.domain.model.file.FileState;
import io.bcs.domain.model.file.Lifecycle;
import io.bcs.domain.model.file.states.lifecycle.InacceptableLifecycleMethod;

public class FileDisposedState extends FileState {
    public FileDisposedState(FileEntityAccessor fileEntityAccessor) {
        super(fileEntityAccessor);
    }

    @Override
    public Promise<FileContent> getContentAccess(FileStorage fileStorage,
            Collection<ContentFragment> contentFragments) {
        throw new FileDisposedException();
    }

    @Override
    public Lifecycle getLifecycle(FileStorage storage) {
        return new Lifecycle() {
            @Override
            public LifecycleMethod<Void> dispose() {
                return new InacceptableLifecycleMethod<>(FileDisposedException::new);
            }

            @Override
            public LifecycleMethod<FileUploadStatistic> upload(ContentUploader uploader) {
                return new InacceptableLifecycleMethod<>(FileDisposedException::new);
            }
        };
    }
}
