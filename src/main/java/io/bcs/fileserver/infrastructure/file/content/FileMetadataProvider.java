package io.bcs.fileserver.infrastructure.file.content;

import io.bcs.fileserver.domain.model.file.metadata.FileMetadata;
import io.bcs.fileserver.domain.model.storage.ContentLocator;

/**
 * This interface describes the component, obtaining file metadata by a content locator.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface FileMetadataProvider {
  public FileMetadata getMetadataFor(ContentLocator contentLocator);
}
