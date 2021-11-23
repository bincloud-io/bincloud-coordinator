package io.bcs.domain.model.file;


import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.Stream;
import io.bce.interaction.streaming.Streamer;
import io.bce.interaction.streaming.Stream.Stat;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bcs.domain.model.ContentLocator;
import io.bcs.domain.model.FileStorage;
import io.bcs.domain.model.file.FileState.FileEntityAccessor;
import io.bcs.domain.model.file.Lifecycle.FileUploadStatistic;
import io.bcs.domain.model.file.Lifecycle.LifecycleMethod;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LifecycleUploadFileMethod implements LifecycleMethod<FileUploadStatistic> {
    private final FileEntityAccessor entityAccessor;
    private final FileStorage storage;
    private final Source<BinaryChunk> contentSource;
    private final Streamer streamer;
    
    @Override
    public Promise<FileUploadStatistic> execute() {
        return createUploadStream().chain(result -> {
            entityAccessor.updateContentLength(result.getSize());
            return Promises.resolvedBy(new UploadStatistic(entityAccessor)); 
        });
    }
    
    private Promise<Stat> createUploadStream() {
        return Promises.of(deferred -> {
            Destination<BinaryChunk> destination = storage.getAccessOnWrite(entityAccessor.getLocator());
            Stream<BinaryChunk> stream = streamer.createStream(contentSource, destination);
            stream.start().delegate(deferred);        
        });
    }
    
    
    @Getter
    @EqualsAndHashCode
    private class UploadStatistic implements FileUploadStatistic {
        private final ContentLocator locator;
        private final Long contentLength;
        
        public UploadStatistic(FileEntityAccessor entityAccessor) {
            this.locator = new UploadedContentLocator(entityAccessor.getLocator());
            this.contentLength = entityAccessor.getContentLength();
        }
    }

    @Getter
    @EqualsAndHashCode
    private class UploadedContentLocator implements ContentLocator {
        private final String storageFileName;
        private final String storageName;
        
        public UploadedContentLocator(ContentLocator locator) {
            super();
            this.storageFileName = locator.getStorageFileName();
            this.storageName = locator.getStorageName();
        }
        
        
    }
}
