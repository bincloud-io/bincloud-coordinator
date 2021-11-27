package io.bcs.domain.model.file.states.lifecycle;

import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bcs.domain.model.FileStorage;
import io.bcs.domain.model.file.ContentUploader;
import io.bcs.domain.model.file.FileState.FileEntityAccessor;
import io.bcs.domain.model.file.Lifecycle.FileUploadStatistic;
import io.bcs.domain.model.file.Lifecycle.LifecycleMethod;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LifecycleUploadFileMethod implements LifecycleMethod<FileUploadStatistic> {
    private final FileEntityAccessor entityAccessor;
    private final FileStorage storage;
    private final ContentUploader uploader;

    @Override
    public Promise<FileUploadStatistic> execute() {
        return Promises.of(deferred -> {
            Destination<BinaryChunk> destination = storage.getAccessOnWrite(entityAccessor.getLocator());
            uploader.upload(entityAccessor.getLocator(), destination).then(statistic -> {
                entityAccessor.updateContentLength(statistic.getContentLength());
            }).delegate(deferred);
        });
    }
}
