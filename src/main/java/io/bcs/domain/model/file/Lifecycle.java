package io.bcs.domain.model.file;

import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.Streamer;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bce.promises.Promise;
import io.bcs.domain.model.ContentLocator;

public interface Lifecycle {
    public LifecycleMethod<FileUploadStatistic> upload(Streamer streamer, Source<BinaryChunk> contentSource);
    
    public LifecycleMethod<Void> dispose();

    public interface LifecycleMethod<R> {
        public Promise<R> execute();
    }
    
    public interface FileUploadStatistic {
        public ContentLocator getLocator();

        public Long getContentLength();
    }
}