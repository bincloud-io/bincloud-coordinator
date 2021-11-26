package io.bcs.domain.model.file;

import io.bce.promises.Promise;

public interface ContentDownloader {
    public Promise<Void> downloadFullContent(FileContent content);
    
    public Promise<Void> downloadContentRange(FileContent content);
    
    public Promise<Void> downloadContentRanges(FileContent content);
}
