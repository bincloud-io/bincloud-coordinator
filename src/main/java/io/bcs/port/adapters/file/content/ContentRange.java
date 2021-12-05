package io.bcs.port.adapters.file.content;

import io.bcs.domain.model.file.FileMetadata;
import io.bcs.domain.model.file.ContentFragment;
import io.bcs.domain.model.file.FileContent.ContentPart;

public class ContentRange {
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
