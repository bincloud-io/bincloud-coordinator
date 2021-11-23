package io.bcs.domain.model.file;

import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bcs.domain.model.ContentLocator;
import io.bcs.domain.model.FileStorage;
import io.bcs.domain.model.file.FileState.FileEntityAccessor;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@ToString
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class File {
    static final String DEFAULT_STORAGE_NAME = "unknown";
    static final String DEFAULT_STORAGE_FILE_NAME = "unknown";
    static final String DEFAULT_MEDIA_TYPE = "application/octet-stream";
    static final String DEFAULT_FILE_NAME = "unknown";

    @Include
    @Default
    private String storageFileName = DEFAULT_STORAGE_FILE_NAME;
    @Default
    private String storageName = DEFAULT_STORAGE_NAME;
    @Default
    private FileStatus status = FileStatus.DRAFT;
    @Default
    private String mediaType = DEFAULT_MEDIA_TYPE;
    @Default
    private String fileName = DEFAULT_FILE_NAME;
    @Default
    private Long contentLength = 0L;

    private File(ContentLocator contentLocator, CreateFile createFileCommand) {
        super();
        this.storageName = contentLocator.getStorageName();
        this.storageFileName = contentLocator.getStorageFileName();
        this.mediaType = createFileCommand.getMediaType();
        this.fileName = createFileCommand.getFileName();
        this.status = FileStatus.DRAFT;
        this.contentLength = 0L;
    }

    public static final Promise<File> create(FileStorage fileStorage, CreateFile command) {
        return Promises.of(deferred -> {
           deferred.resolve(new File(fileStorage.create(command.getMediaType()), command)); 
        });
    }

    public ContentLocator getLocator() {
        return new ContentLocator() {
            @Override
            public String getStorageName() {
                return storageName;
            }

            @Override
            public String getStorageFileName() {
                return storageFileName;
            }
        };
    }

    public FileMetadata getFileMetadata() {
        return new FileMetadata() {
            @Override
            public FileStatus getStatus() {
                return status;
            }

            @Override
            public String getMediaType() {
                return mediaType;
            }

            @Override
            public String getFileName() {
                return fileName;
            }

            @Override
            public Long getContentLength() {
                return contentLength;
            }
        };
    }

    public Lifecycle getLifecycle(FileStorage storage) {
        return getFileState().getLifecycle(storage);
    }

    private FileState getFileState() {
        return status.createState(createEntityAccessor());
    }

    private FileEntityAccessor createEntityAccessor() {
        return new FileEntityAccessor() {
            @Override
            public ContentLocator getLocator() {
                return File.this.getLocator();
            }

            @Override
            public void dispose() {
                File.this.dispose();
            }

            @Override
            public Long getContentLength() {
                return File.this.contentLength;
            }

            @Override
            public void updateContentLength(Long contentLength) {
                File.this.updateContentLength(contentLength);
            }
        };
    }
    
    private void updateContentLength(Long contentLength) {
        this.contentLength = contentLength;
    }

    private void dispose() {
        this.status = FileStatus.DISPOSED;
        this.contentLength = 0L;
    }

    public interface CreateFile {
        public String getMediaType();

        public String getFileName();
    }
}
