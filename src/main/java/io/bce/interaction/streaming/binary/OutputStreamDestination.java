package io.bce.interaction.streaming.binary;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import io.bce.domain.errors.UnexpectedErrorException;
import io.bce.interaction.streaming.binary.BinaryChunk.BinaryChunkWriter;
import lombok.NonNull;

public class OutputStreamDestination extends BinaryDestination implements Closeable {
    private final Closeable streamCloser;

    public OutputStreamDestination(@NonNull OutputStream outputStream) {
        super(chunkWriter(outputStream));
        this.streamCloser = outputStream;
    }

    @Override
    public void release() {        
    }
    
    @Override
    public void close() throws IOException {
        streamCloser.close();
    }

    private static BinaryChunkWriter chunkWriter(@NonNull OutputStream outputStream) {
        return chunk -> {
            try {
                byte[] buffer = Arrays.copyOfRange(chunk.getBody(), 0, chunk.getSize());
                outputStream.write(buffer);
            } catch (IOException error) {
                throw new UnexpectedErrorException(error);
            }
        };
    }
}
