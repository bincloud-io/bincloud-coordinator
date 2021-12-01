package io.bcs.port.adapters.file;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import javax.servlet.http.HttpServletResponse;

import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.RechargeableSource;
import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.Stream.Stat;
import io.bce.interaction.streaming.Streamer;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bce.interaction.streaming.binary.InputStreamSource;
import io.bce.interaction.streaming.binary.OutputStreamDestination;
import io.bce.promises.Promise;
import io.bcs.domain.model.file.ContentReceiver;
import io.bcs.domain.model.file.FileContent;
import io.bcs.domain.model.file.FileContent.ContentPart;
import io.bcs.domain.model.file.FileMetadata;

public class HttpFileDataReceiver extends HttpHeadersReceiver implements ContentReceiver {
    private static final String MULTIPART_SEPARATOR = "--" + MULTIPART_BOUNDARY;
    private static final String MULTIPART_ENDING = MULTIPART_SEPARATOR + "--";
    private final Streamer streamer;
    private final Destination<BinaryChunk> destination;

    public HttpFileDataReceiver(Streamer streamer, HttpServletResponse servletResponse) throws IOException {
        super(servletResponse);
        this.destination = new OutputStreamDestination(servletResponse.getOutputStream());
        this.streamer = streamer;
    }

    @Override
    public Promise<Void> receiveFullContent(FileContent content) {
        return transferContent(new SingleRangeContentSource(content)).chain(stat -> {
            return HttpFileDataReceiver.super.receiveFullContent(content);
        });
    }

    @Override
    public Promise<Void> receiveContentRange(FileContent content) {
        return transferContent(new SingleRangeContentSource(content)).chain(stat -> {
            return HttpFileDataReceiver.super.receiveContentRange(content);
        });
    }

    @Override
    public Promise<Void> receiveContentRanges(FileContent content) {
        return transferContent(new MultiRangeContentSource(content)).chain(stat -> {
            return HttpFileDataReceiver.super.receiveContentRanges(content);
        });
    }

    private Promise<Stat> transferContent(Source<BinaryChunk> contentSource) {
        return streamer.createStream(contentSource, destination).start();
    }

    private static class SingleRangeContentSource extends RechargeableSource<BinaryChunk> {
        public SingleRangeContentSource(FileContent content) {
            super(createSourcesQueue(content));
        }

        private static Queue<Source<BinaryChunk>> createSourcesQueue(FileContent content) {
            LinkedList<Source<BinaryChunk>> queue = new LinkedList<Source<BinaryChunk>>();
            queue.add(content.getParts().iterator().next().getContentSource());
            return queue;
        }
    }

    private static class MultiRangeContentSource extends RechargeableSource<BinaryChunk> {
        public MultiRangeContentSource(FileContent content) {
            super(createSourcesQueue(content));
        }

        private static Queue<Source<BinaryChunk>> createSourcesQueue(FileContent content) {
            LinkedList<Source<BinaryChunk>> queue = new LinkedList<Source<BinaryChunk>>();
            FileMetadata metadata = content.getFileMetadata();
            content.getParts().forEach(part -> queue.add(createPartSource(metadata, part)));
            byte[] contentEnding = createContentEnding();
            queue.add(new InputStreamSource(new ByteArrayInputStream(contentEnding), contentEnding.length));
            return queue;
        }

        private static Source<BinaryChunk> createPartSource(FileMetadata metadata, ContentPart part) {
            LinkedList<Source<BinaryChunk>> queue = new LinkedList<Source<BinaryChunk>>();
            byte[] head = createRangeHead(metadata, part);
            queue.add(new InputStreamSource(new ByteArrayInputStream(head), head.length));
            queue.add(part.getContentSource());
            return new RechargeableSource<>(queue);
        }

        private static final byte[] createRangeHead(FileMetadata fileMetadata, ContentPart contentPart) {
            return new StringBuilder().append("\n").append(MULTIPART_SEPARATOR + "\n")
                    .append(String.format("Content-Type: %s\n", fileMetadata.getMediaType()))
                    .append(String.format("Content-Range: %s\n", new ContentRange(fileMetadata, contentPart)))
                    .toString().getBytes();
        }

        private static final byte[] createContentEnding() {
            return new StringBuilder().append("\n").append(MULTIPART_ENDING + "\n").toString().getBytes();
        }
    }
}
