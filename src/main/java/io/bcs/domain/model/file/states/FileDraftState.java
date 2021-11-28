package io.bcs.domain.model.file.states;

import java.util.Collection;

import io.bce.promises.Promise;
import io.bcs.domain.model.file.ContentFragment;
import io.bcs.domain.model.file.ContentUploader;
import io.bcs.domain.model.file.FileContent;
import io.bcs.domain.model.file.FileState;
import io.bcs.domain.model.file.FileStorage;
import io.bcs.domain.model.file.Lifecycle;
import io.bcs.domain.model.file.states.lifecycle.LifecycleDisposeFileMethod;
import io.bcs.domain.model.file.states.lifecycle.LifecycleUploadFileMethod;

public class FileDraftState extends FileState {
    public FileDraftState(FileEntityAccessor fileEntityAccessor) {
        super(fileEntityAccessor);
    }

    @Override
    public Promise<FileContent> getContentAccess(FileStorage fileStorage,
            Collection<ContentFragment> contentFragments) {
        throw new ContentNotUploadedException();
    }

    @Override
    public Lifecycle getLifecycle(FileStorage storage) {
        return new Lifecycle() {
            @Override
            public LifecycleMethod<Void> dispose() {
                return new LifecycleDisposeFileMethod(getFileEntityAccessor(), storage);
            }

            @Override
            public LifecycleMethod<FileUploadStatistic> upload(ContentUploader uploader) {
                return new LifecycleUploadFileMethod(getFileEntityAccessor(), storage, uploader);
            }
        };
    }
}
