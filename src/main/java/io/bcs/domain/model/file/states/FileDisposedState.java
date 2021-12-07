package io.bcs.domain.model.file.states;

import java.util.Collection;

import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise;
import io.bcs.domain.model.file.ContentFragment;
import io.bcs.domain.model.file.ContentUploader;
import io.bcs.domain.model.file.FileContent;
import io.bcs.domain.model.file.FileState;
import io.bcs.domain.model.file.FileStorage;
import io.bcs.domain.model.file.Lifecycle;
import io.bcs.domain.model.file.states.lifecycle.InacceptableLifecycleMethod;

public class FileDisposedState extends FileState {
    private static final ApplicationLogger log = Loggers.applicationLogger(FileDisposedState.class);
    
    public FileDisposedState(FileEntityAccessor fileEntityAccessor) {
        super(fileEntityAccessor);
    }

    @Override
    public Promise<FileContent> getContentAccess(FileStorage fileStorage,
            Collection<ContentFragment> contentFragments) {
        log.debug("The file content download is going to be performed from disposed file");
        throw new FileDisposedException();
    }

    @Override
    public Lifecycle getLifecycle(FileStorage storage) {
        return new Lifecycle() {
            @Override
            public LifecycleMethod<Void> dispose() {
                log.debug("The file dispose is going to be performed for disposed file");
                return new InacceptableLifecycleMethod<>(FileDisposedException::new);
            }

            @Override
            public LifecycleMethod<FileUploadStatistic> upload(ContentUploader uploader) {
                log.debug("The file content upload is going to be performed for disposed file");
                return new InacceptableLifecycleMethod<>(FileDisposedException::new);
            }
        };
    }
}
