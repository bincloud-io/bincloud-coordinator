package io.bcs.domain.model.file;

public interface ContentDownloader {
    public void downloadFullContent(FileContent content);
    
    public void downloadContentRange(FileContent content);
    
    public void downloadContentRanges(FileContent content);
}
