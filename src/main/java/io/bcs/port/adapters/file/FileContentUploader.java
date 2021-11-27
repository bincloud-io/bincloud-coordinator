package io.bcs.port.adapters.file;

import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.Stream;
import io.bce.interaction.streaming.Streamer;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bcs.domain.model.ContentLocator;
import io.bcs.domain.model.file.ContentUploader;
import io.bcs.domain.model.file.Lifecycle.FileUploadStatistic;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class FileContentUploader implements ContentUploader {
    private Streamer streamer;
    private Source<BinaryChunk> source;

    @Override
    public Promise<FileUploadStatistic> upload(ContentLocator locator, Destination<BinaryChunk> destination) {
        return Promises.of(deferred -> {
            uploadContent(locator, streamer.createStream(source, destination)).delegate(deferred);
        });
    }

    private Promise<FileUploadStatistic> uploadContent(ContentLocator locator, Stream<BinaryChunk> stream) {
        return stream.start().chain(stat -> {
            return Promises.resolvedBy(new UploadStatistic(locator, stat.getSize()));
        });
    }

    @Getter
    @EqualsAndHashCode
    @RequiredArgsConstructor
    private static class UploadStatistic implements FileUploadStatistic {
        private final ContentLocator locator;
        private final Long contentLength;
    }
}
