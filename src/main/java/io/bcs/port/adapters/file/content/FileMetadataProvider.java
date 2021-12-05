package io.bcs.port.adapters.file.content;

import io.bcs.domain.model.file.ContentLocator;
import io.bcs.domain.model.file.FileMetadata;

public interface FileMetadataProvider {
    public FileMetadata getMetadataFor(ContentLocator contentLocator);
}
