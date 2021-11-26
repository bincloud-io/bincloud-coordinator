package io.bcs.domain.model.file.states;

import java.util.Collection;

import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.Streamer;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bce.promises.Promise;
import io.bcs.domain.model.ContentFragment;
import io.bcs.domain.model.FileStorage;
import io.bcs.domain.model.file.FileContent;
import io.bcs.domain.model.file.FileState;
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
            public LifecycleMethod<FileUploadStatistic> upload(Streamer streamer, Source<BinaryChunk> contentSource) {
                return new LifecycleUploadFileMethod(getFileEntityAccessor(), storage, contentSource, streamer);
            }
        };
    }
}
