package io.bcs.port.adapters.file;

import io.bce.promises.Promise;
import io.bcs.domain.model.file.ContentDownloader;
import io.bcs.domain.model.file.FileContent;

public class HttpFileContentDownloader implements ContentDownloader {
   
    @Override
    public Promise<Void> downloadFullContent(FileContent content) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Promise<Void> downloadContentRange(FileContent content) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Promise<Void> downloadContentRanges(FileContent content) {
        throw new UnsupportedOperationException();
    }

}
