package io.bcs.domain.model.file;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bcs.domain.model.file.FileContent.ContentType;
import io.bcs.domain.model.file.FileState.FileEntityAccessor;
import io.bcs.domain.model.file.FileState.FileStateFactory;
import io.bcs.domain.model.file.states.FileDisposedState;
import io.bcs.domain.model.file.states.FileDistributingState;
import io.bcs.domain.model.file.states.FileDraftState;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.EqualsAndHashCode.Include;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class File {
    static final String DEFAULT_STORAGE_NAME = "unknown";
    static final String DEFAULT_STORAGE_FILE_NAME = "unknown";
    static final String DEFAULT_MEDIA_TYPE = "application/octet-stream";
    static final String DEFAULT_FILE_NAME = "unknown";

    private static FileStateCreator STATE_CREATOR = new FileStateCreator()
            .registerStateFactory(FileStatus.DRAFT, FileDraftState::new)
            .registerStateFactory(FileStatus.DISTRIBUTING, FileDistributingState::new)
            .registerStateFactory(FileStatus.DISPOSED, FileDisposedState::new);

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
    private Long totalLength = 0L;

    private File(ContentLocator contentLocator, CreateFile createFileCommand) {
        super();
        this.storageName = contentLocator.getStorageName();
        this.mediaType = extractMediaType(createFileCommand);
        this.storageFileName = contentLocator.getStorageFileName();
        this.fileName = createFileCommand.getFileName().orElse(storageFileName);
        this.status = FileStatus.DRAFT;
        this.totalLength = 0L;
    }

    public static final Promise<File> create(FileStorage fileStorage, CreateFile command) {
        return Promises.of(deferred -> {
            deferred.resolve(new File(fileStorage.create(extractMediaType(command)), command));
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

    public Promise<Void> downloadContent(FileStorage fileStorage, ContentReceiver contentDownloader,
            Collection<Range> ranges) {
        return Promises.of(deferred -> {
            getFileState().getContentAccess(fileStorage, createFragments(ranges)).chain(fileContent -> {
                if (fileContent.getType() == ContentType.RANGE) {
                    return contentDownloader.receiveContentRange(fileContent);
                }

                if (fileContent.getType() == ContentType.MULTIRANGE) {
                    return contentDownloader.receiveContentRanges(fileContent);
                }

                return contentDownloader.receiveFullContent(fileContent);
            }).delegate(deferred);
        });
    }

    private Collection<ContentFragment> createFragments(Collection<Range> ranges) {
        FileFragments fragments = new FileFragments(ranges, totalLength);
        return fragments.getParts();
    }

    public Lifecycle getLifecycle(FileStorage storage) {
        return getFileState().getLifecycle(storage);
    }

    private static String extractMediaType(CreateFile command) {
        return command.getMediaType().orElse(DEFAULT_MEDIA_TYPE);
    }

    private FileState getFileState() {
        return STATE_CREATOR.createStateFor(status, createEntityAccessor());
    }

    private FileEntityAccessor createEntityAccessor() {
        return new FileEntityAccessor() {
            @Override
            public ContentLocator getLocator() {
                return File.this.getLocator();
            }

            @Override
            public Long getTotalLength() {
                return File.this.totalLength;
            }

            @Override
            public void dispose() {
                File.this.dispose();
            }

            @Override
            public void startFileDistribution(Long contentLength) {
                File.this.startFileDistribution(contentLength);
            }
        };
    }

    private void startFileDistribution(Long contentLength) {
        this.totalLength = contentLength;
        this.status = FileStatus.DISTRIBUTING;
    }

    private void dispose() {
        this.status = FileStatus.DISPOSED;
        this.totalLength = 0L;
    }

    public interface CreateFile {
        public Optional<String> getMediaType();

        public Optional<String> getFileName();
    }

    private static class FileStateCreator {
        private Map<FileStatus, FileStateFactory> STATE_FACTORIES = new HashMap<>();

        public FileState createStateFor(FileStatus status, FileEntityAccessor accessor) {
            return STATE_FACTORIES.get(status).create(accessor);
        }

        public FileStateCreator registerStateFactory(FileStatus status, FileStateFactory stateFactory) {
            STATE_FACTORIES.put(status, stateFactory);
            return this;
        }
    }
}
