package io.bcs.fileserver.domain.model.file.content;

import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bcs.fileserver.domain.model.file.File;
import io.bcs.fileserver.domain.model.storage.ContentFragment;
import io.bcs.fileserver.domain.model.storage.ContentLocator;
import java.util.Collection;

/**
 * This interface describes the retrieved file content accessor.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface FileContent {
  File getFile();
  
  ContentType getType();
  
  ContentLocator getLocator();
  

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
