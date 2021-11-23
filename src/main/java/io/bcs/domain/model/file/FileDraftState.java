package io.bcs.domain.model.file;

import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.Streamer;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bcs.domain.model.FileStorage;

public class FileDraftState extends FileState {
    FileDraftState(FileEntityAccessor fileEntityAccessor) {
        super(fileEntityAccessor);
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
