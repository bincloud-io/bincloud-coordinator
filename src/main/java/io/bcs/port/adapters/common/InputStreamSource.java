package io.bcs.port.adapters.common;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import io.bce.domain.errors.UnexpectedErrorException;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bce.interaction.streaming.binary.BinaryChunk.BinaryChunkReader;
import io.bce.interaction.streaming.binary.BinarySource;
import lombok.NonNull;

public class InputStreamSource extends BinarySource implements Closeable {
    private final Closeable streamCloser;

    public InputStreamSource(@NonNull InputStream inputStream, int bufferSize) {
        super(chunkReader(inputStream, bufferSize));
        this.streamCloser = inputStream;
    }

    @Override
    public void release() {
    }

    @Override
    public void close() throws IOException {
        streamCloser.close();
    }

    private static BinaryChunkReader chunkReader(InputStream inputStream, int bufferSize) {
        return () -> {
            try {
                byte[] buffer = new byte[bufferSize];
                int readCount = inputStream.read(buffer);
                if (readCount != -1) {
                    return new BinaryChunk(Arrays.copyOfRange(buffer, 0, readCount));
                }
                return BinaryChunk.EMPTY;
            } catch (IOException error) {
                throw new UnexpectedErrorException(error);
            }
        };
    }
}
