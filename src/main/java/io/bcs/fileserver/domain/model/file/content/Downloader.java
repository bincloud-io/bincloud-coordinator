package io.bcs.fileserver.domain.model.file.content;

import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bcs.fileserver.domain.errors.ContentNotUploadedException;
import io.bcs.fileserver.domain.errors.FileDisposedException;
import io.bcs.fileserver.domain.model.file.File;
import io.bcs.fileserver.domain.model.file.FileContentLocator;
import io.bcs.fileserver.domain.model.file.FileFragments;
import io.bcs.fileserver.domain.model.file.FileStatus;
import io.bcs.fileserver.domain.model.file.Range;
import io.bcs.fileserver.domain.model.file.content.FileContent.ContentType;
import io.bcs.fileserver.domain.model.storage.ContentFragment;
import io.bcs.fileserver.domain.model.storage.ContentLocator;
import io.bcs.fileserver.domain.model.storage.FileStorage;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This class performs file content downloading process.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class Downloader {
  private static final ApplicationLogger log = Loggers.applicationLogger(Downloader.class);

  private final File file;
  private final FileStorage fileStorage;

  /**
   * Receive file content.
   *
   * @param ranges          The file content ranges.
   * @param contentReceiver The content receiver.
   * @return The file download complete promise.
   */
  public Promise<Void> receiveContent(Collection<Range> ranges, ContentReceiver contentReceiver) {
    return Promises.of(deferred -> {
      FileFragments fragments = new FileFragments(ranges, file.getTotalLength());
      getFileDownloadState().receiveContent(fragments.getParts(), contentReceiver)
          .delegate(deferred);
    });
  }

  private FileDownloadState getFileDownloadState() {
    FileStatus fileStatus = file.getStatus();
    if (fileStatus == FileStatus.DRAFT) {
      return new DraftDownloadState();
    }

    if (fileStatus == FileStatus.DISTRIBUTING) {
      return new DistributingDownloadState();
    }

    return new DisposedDownloadState();
  }

  /**
   * This interface describes the component, receiving a file content.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public interface ContentReceiver {
    Promise<Void> receiveFullContent(FileContent content);

    Promise<Void> receiveContentRange(FileContent content);

    Promise<Void> receiveContentRanges(FileContent content);
  }

  private interface FileDownloadState {
    Promise<Void> receiveContent(Collection<ContentFragment> fragments,
        ContentReceiver contentReceiver);
  }

  private class DraftDownloadState implements FileDownloadState {
    @Override
    public Promise<Void> receiveContent(Collection<ContentFragment> fragments,
        ContentReceiver contentReceiver) {
      log.debug("The file content download is going to be performed from draft file");
      return Promises.rejectedBy(new ContentNotUploadedException());
    }
  }

  private class DistributingDownloadState implements FileDownloadState {
    @Override
    public Promise<Void> receiveContent(Collection<ContentFragment> fragments,
        ContentReceiver contentReceiver) {
      FileContent fileContent = new StorageFileContent(fragments);
      return receiveContent(fileContent, contentReceiver);
    }

    private Promise<Void> receiveContent(FileContent fileContent, ContentReceiver contentReceiver) {
      if (fileContent.getType() == ContentType.RANGE) {
        log.debug("Download single-range file content");
        return contentReceiver.receiveContentRange(fileContent);
      }

      if (fileContent.getType() == ContentType.MULTIRANGE) {
        log.debug("Download multi-range file content");
        return contentReceiver.receiveContentRanges(fileContent);
      }

      log.debug("Download full-size file content");
      return contentReceiver.receiveFullContent(fileContent);
    }
  }

  private class DisposedDownloadState implements FileDownloadState {
    @Override
    public Promise<Void> receiveContent(Collection<ContentFragment> fragments,
        ContentReceiver contentReceiver) {
      log.debug("The file content download is going to be performed from disposed file");
      return Promises.rejectedBy(new FileDisposedException());
    }
  }

  @Getter
  private class StorageFileContent implements FileContent {
    private ContentType type;
    private Collection<ContentPart> parts;

    public StorageFileContent(Collection<ContentFragment> fragments) {
      super();
      this.type = recognizeContentType(fragments.size());
      this.parts = getContentParts(fileStorage, fragments);
    }

    public File getFile() {
      return file;
    }

    @Override
    public ContentLocator getLocator() {
      return new FileContentLocator(file);
    }

    private Collection<ContentPart> getContentParts(FileStorage storage,
        Collection<ContentFragment> fragments) {
      return normalizeFragments(fragments).stream().collect(Collectors.mapping(
          fragment -> new StorageContentPart(fragment, fileStorage.getAccessOnRead(file, fragment)),
          Collectors.toList()));
    }

    private Collection<ContentFragment> normalizeFragments(Collection<ContentFragment> fragments) {
      return Optional.of(fragments).filter(collection -> !collection.isEmpty())
          .orElse(Arrays.asList(new FullSizeFragment(file.getTotalLength())));
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

    @RequiredArgsConstructor
    private class FullSizeFragment implements ContentFragment {
      private final Long totalLength;

      @Override
      public Long getOffset() {
        return 0L;
      }

      @Override
      public Long getLength() {
        return totalLength;
      }
    }

    @Getter
    @AllArgsConstructor
    private class StorageContentPart implements ContentPart {
      private ContentFragment contentFragment;
      private Source<BinaryChunk> contentSource;
    }
  }
}
