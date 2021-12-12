package io.bcs.fileserver.infrastructure.file.content;

import io.bcs.fileserver.domain.model.file.content.FileContent.ContentPart;
import io.bcs.fileserver.domain.model.file.metadata.FileMetadata;
import io.bcs.fileserver.domain.model.storage.ContentFragment;

class ContentRange {
  private final Long rangeStart;
  private final Long rangeEnd;
  private final Long totalLength;

  public ContentRange(FileMetadata metadata, ContentPart contentPart) {
    ContentFragment fragment = contentPart.getContentFragment();
    this.rangeStart = fragment.getOffset();
    this.rangeEnd = this.rangeStart + fragment.getLength() - 1;
    this.totalLength = metadata.getTotalLength();
  }

  @Override
  public String toString() {
    return String.format("bytes %s-%s/%s", rangeStart, rangeEnd, totalLength);
  }
}
