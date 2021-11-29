package io.bcs.port.adapters.file;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.Streamer;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bce.promises.Promise;
import io.bcs.domain.model.file.ContentDownloader;
import io.bcs.domain.model.file.FileContent;
import io.bcs.port.adapters.common.OutputStreamDestination;

public class HttpFileContentDownloader implements ContentDownloader {
    private final Streamer streamer;
    private final Destination<BinaryChunk> destination;
    
    public HttpFileContentDownloader(Streamer streamer, HttpServletResponse servletResponse) throws IOException {
        super();
        this.destination = new OutputStreamDestination(servletResponse.getOutputStream());
        this.streamer = streamer;
    }
    
    @Override
    public Promise<Void> downloadFullContent(FileContent content) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Promise<Void> downloadContentRange(FileContent content) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Promise<Void> downloadContentRanges(FileContent content) {
        throw new UnsupportedOperationException();
    }
}
