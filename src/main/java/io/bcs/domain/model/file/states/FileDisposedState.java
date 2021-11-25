package io.bcs.domain.model.file.states;

import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.Streamer;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bcs.domain.model.FileStorage;
import io.bcs.domain.model.file.FileState;
import io.bcs.domain.model.file.Lifecycle;
import io.bcs.domain.model.file.states.lifecycle.InacceptableLifecycleMethod;

public class FileDisposedState extends FileState {
    public FileDisposedState(FileEntityAccessor fileEntityAccessor) {
        super(fileEntityAccessor);
    }

    @Override
    public Lifecycle getLifecycle(FileStorage storage) {
        return new Lifecycle() {
            @Override
            public LifecycleMethod<Void> dispose() {
                return new InacceptableLifecycleMethod<>(FileHasBeenDisposedException::new);
            }

            @Override
            public LifecycleMethod<FileUploadStatistic> upload(Streamer streamer, Source<BinaryChunk> contentSource) {
                return new InacceptableLifecycleMethod<>(FileHasBeenDisposedException::new);
            }
        };
    }
}
