package io.bcs.domain.model.file;

import io.bcs.domain.model.ContentLocator;
import io.bcs.domain.model.FileStorage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class FileState {
    @Getter(value = AccessLevel.PROTECTED)
    private final FileEntityAccessor fileEntityAccessor;

    public abstract Lifecycle getLifecycle(FileStorage storage);

    public interface FileEntityAccessor {
        public ContentLocator getLocator();

        public Long getContentLength();
        
        public void updateContentLength(Long contentLength);

        public void dispose();
    }

    public interface FileStateFactory {
        public FileState create(FileEntityAccessor fileEntity);
    }
}
