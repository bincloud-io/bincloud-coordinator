package io.bcs.domain.model.file;

import io.bce.promises.Promise;
import io.bcs.domain.model.ContentLocator;

public interface Lifecycle {
    public LifecycleMethod<FileUploadStatistic> upload(ContentUploader uploader);
    
    public LifecycleMethod<Void> dispose();

    public interface LifecycleMethod<R> {
        public Promise<R> execute();
    }
    
    public interface FileUploadStatistic {
        public ContentLocator getLocator();

        public Long getContentLength();
    }
}