package io.bcs.domain.model.file;

import java.util.Collection;

import io.bce.promises.Promise;
import io.bcs.domain.model.file.Lifecycle.FileUploadStatistic;

public interface ContentUseCases {
    public Promise<Void> download(String storageFileName, Collection<Range> ranges, ContentDownloader downloader);

    public Promise<FileUploadStatistic> upload(String storageFileName, ContentUploader uploader);
}
