package io.bcs.domain.model.file;

import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bce.promises.Promise;
import io.bcs.domain.model.ContentLocator;
import io.bcs.domain.model.file.Lifecycle.FileUploadStatistic;

public interface ContentUploader {
    public Promise<FileUploadStatistic> upload(ContentLocator locator, Destination<BinaryChunk> destination);
}
