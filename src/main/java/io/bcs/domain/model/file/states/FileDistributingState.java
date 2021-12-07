package io.bcs.domain.model.file.states;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bcs.domain.model.file.ContentFragment;
import io.bcs.domain.model.file.ContentLocator;
import io.bcs.domain.model.file.ContentUploader;
import io.bcs.domain.model.file.FileContent;
import io.bcs.domain.model.file.FileContent.ContentPart;
import io.bcs.domain.model.file.FileState;
import io.bcs.domain.model.file.FileStorage;
import io.bcs.domain.model.file.Lifecycle;
import io.bcs.domain.model.file.states.lifecycle.InacceptableLifecycleMethod;
import io.bcs.domain.model.file.states.lifecycle.LifecycleDisposeFileMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class FileDistributingState extends FileState {
    private static final ApplicationLogger log = Loggers.applicationLogger(FileDistributingState.class);
    public FileDistributingState(FileEntityAccessor fileEntityAccessor) {
        super(fileEntityAccessor);
    }

    @Override
    public Promise<FileContent> getContentAccess(FileStorage fileStorage,
            Collection<ContentFragment> contentFragments) {
        return Promises.of(deferred -> {
            log.debug("The file content download is going to be performed from distributioning file");
            deferred.resolve(new StorageFileContent(fileStorage, contentFragments));
        });
    }

    @Override
    public Lifecycle getLifecycle(FileStorage storage) {
        return new Lifecycle() {
            @Override
            public LifecycleMethod<Void> dispose() {
                log.debug("The file dispose is going to be performed for distributioning file");
                return new LifecycleDisposeFileMethod(getFileEntityAccessor(), storage);
            }

            @Override
            public LifecycleMethod<FileUploadStatistic> upload(ContentUploader uploader) {
                log.debug("The file content upload is going to be performed for distributioning file");
                return new InacceptableLifecycleMethod<>(ContentUploadedException::new);
            }
        };
    }

    @Getter
    private class StorageFileContent implements FileContent {
        private ContentType type;
        private ContentLocator locator;
        private Collection<ContentPart> parts;

        public StorageFileContent(FileStorage storage, Collection<ContentFragment> fragments) {
            super();
            this.type = recognizeContentType(fragments.size());
            this.locator = getFileEntityAccessor().getLocator();
            this.parts = getContentParts(storage, fragments);
        }

        private Collection<ContentPart> getContentParts(FileStorage storage, Collection<ContentFragment> fragments) {
            return normalizeFragments(fragments).stream()
                    .collect(Collectors.mapping(
                            fragment -> new StorageContentPart(fragment, storage.getAccessOnRead(locator, fragment)),
                            Collectors.toList()));
        }

        private Collection<ContentFragment> normalizeFragments(Collection<ContentFragment> fragments) {
            return Optional.of(fragments).filter(collection -> !collection.isEmpty())
                    .orElse(Arrays.asList(new FullSizeFragment()));
        }

        private ContentType recognizeContentType(int fragmentsCount) {
            if (fragmentsCount == 0) {
                return ContentType.FULL;
            }

            if (fragmentsCount == 1L) {
                return ContentType.RANGE;
            }

            return ContentType.MULTIRANGE;
        }
    }

    private class FullSizeFragment implements ContentFragment {
        @Override
        public Long getOffset() {
            return 0L;
        }

        @Override
        public Long getLength() {
            return getFileEntityAccessor().getTotalLength();
        }
    }

    @Getter
    @AllArgsConstructor
    private class StorageContentPart implements ContentPart {
        private ContentFragment contentFragment;
        private Source<BinaryChunk> contentSource;
    }
}
