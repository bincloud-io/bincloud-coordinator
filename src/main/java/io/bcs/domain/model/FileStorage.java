package io.bcs.domain.model;

import java.util.Collection;

import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.binary.BinaryChunk;

public interface FileStorage {
    public ContentLocator create(String mediaType) throws FileStorageException;

    public Destination<BinaryChunk> getAccessOnWrite(ContentLocator contentLocator) throws FileStorageException;
    
    public Source<BinaryChunk> getAccessOnRead(ContentLocator contentLocator, Collection<ContentFragment> fragments) throws FileStorageException;
    
    public void delete(ContentLocator contentLocator) throws FileStorageException;
    
    
}
