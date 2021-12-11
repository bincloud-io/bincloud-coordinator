package io.bcs.domain.model.file;

import java.util.Collection;

import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.binary.BinaryChunk;

public interface FileContent {
  public ContentType getType();

  public ContentLocator getLocator();

  public Collection<ContentPart> getParts();

  public interface ContentPart {
    public ContentFragment getContentFragment();

    public Source<BinaryChunk> getContentSource();
  }

  public enum ContentType {
    FULL,
    RANGE,
    MULTIRANGE;
  }
}
