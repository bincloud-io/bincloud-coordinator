package io.bcs.fileserver.domain.model.content;

import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.binary.BinaryChunk;
import java.util.Collection;

/**
 * This interface describes the retrieved file content accessor.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface FileContent {
  
  ContentType getType();
  
  ContentLocator getLocator();
  
  FileMetadata getFileMetadata();
  
  Collection<ContentPart> getParts();
  

  /**
   * This interface describes an accessor to a part of retrieved file content.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  interface ContentPart {
    ContentFragment getContentFragment();

    Source<BinaryChunk> getContentSource();
  }

  /**
   * This class enumerates possible types of retrieved content types.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  enum ContentType {
    FULL,
    RANGE,
    MULTIRANGE;
  }
}
