package io.bcs.domain.model;

import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.binary.BinaryChunk;

public interface FileStorage {
    public ContentLocator create(String mediaType) throws FileStorageException;

    public Destination<BinaryChunk> getAccessOnWrite(ContentLocator contentLocator) throws FileStorageException;

    public void delete(ContentLocator contentLocator) throws FileStorageException;
}
