package io.bcs.domain.model.file;

import java.util.Collection;

import io.bce.promises.Promise;
import io.bcs.domain.model.ContentFragment;
import io.bcs.domain.model.ContentLocator;
import io.bcs.domain.model.FileStorage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class FileState {
    @Getter(value = AccessLevel.PROTECTED)
    private final FileEntityAccessor fileEntityAccessor;
        
    public abstract Promise<FileContent> getContentAccess(FileStorage fileStorage, Collection<ContentFragment> contentFragments);
    
    public abstract Lifecycle getLifecycle(FileStorage storage);

    public interface FileEntityAccessor {
        public ContentLocator getLocator();
        
        public FileMetadata getFileMetadata();
        
        public void updateContentLength(Long contentLength);

        public void dispose();
    }

    public interface FileStateFactory {
        public FileState create(FileEntityAccessor fileEntity);
    }
}