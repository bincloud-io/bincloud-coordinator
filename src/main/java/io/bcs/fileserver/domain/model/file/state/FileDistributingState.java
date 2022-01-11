package io.bcs.fileserver.domain.model.file.state;

import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bcs.fileserver.domain.errors.ContentUploadedException;
import io.bcs.fileserver.domain.model.file.FileFragments;
import io.bcs.fileserver.domain.model.file.Range;
import io.bcs.fileserver.domain.model.file.content.ContentDownloader;
import io.bcs.fileserver.domain.model.file.content.ContentUploader;
import io.bcs.fileserver.domain.model.file.content.FileContent;
import io.bcs.fileserver.domain.model.file.content.FileContent.ContentPart;
import io.bcs.fileserver.domain.model.file.content.FileUploadStatistic;
import io.bcs.fileserver.domain.model.storage.ContentFragment;
import io.bcs.fileserver.domain.model.storage.ContentLocator;
import io.bcs.fileserver.domain.model.storage.FileStorage;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * This class implements the distributing file state.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class FileDistributingState extends FileState {
  private static final ApplicationLogger log =
      Loggers.applicationLogger(FileDistributingState.class);

  public FileDistributingState(FileEntityAccessor fileEntityAccessor) {
    super(fileEntityAccessor);
  }

  @Override
  public Promise<FileUploadStatistic> uploadContent(FileStorage fileStorage,
      ContentUploader contentUploader) {
    log.debug("The file content upload is going to be performed from distributed file");
    return Promises.rejectedBy(new ContentUploadedException());
  }

  @Override
  public Promise<Void> downloadContent(FileStorage fileStorage, ContentDownloader contentDownloader,
      Collection<Range> ranges) {
    return Promises.of(deferred -> {
      FileFragments fragments = new FileFragments(ranges, getTotalLength());
      log.debug("The file content download is going to be performed from distributioning file");
      FileContent content = new StorageFileContent(fileStorage, fragments.getParts());
      contentDownloader.downloadContent(content).delegate(deferred);
    });
  }

  @Getter
  private class StorageFileContent implements FileContent {
    private ContentType type;
    private ContentLocator locator;
    private Collection<ContentPart> parts;

    public StorageFileContent(FileStorage storage, Collection<ContentFragment> fragments) {
      super();
      this.type = recognizeContentType(fragments.size());
      this.locator = getContentLocator();
      this.parts = getContentParts(storage, fragments);
    }

    private Collection<ContentPart> getContentParts(FileStorage storage,
        Collection<ContentFragment> fragments) {
      return normalizeFragments(fragments).stream().collect(Collectors.mapping(
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
      return getTotalLength();
    }
  }

  @Getter
  @AllArgsConstructor
  private class StorageContentPart implements ContentPart {
    private ContentFragment contentFragment;
    private Source<BinaryChunk> contentSource;
  }
}
