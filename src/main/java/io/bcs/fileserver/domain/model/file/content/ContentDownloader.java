package io.bcs.fileserver.domain.model.file.content;

import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise;
import io.bcs.fileserver.domain.model.file.File;
import io.bcs.fileserver.domain.model.file.FileFragments;
import io.bcs.fileserver.domain.model.file.Range;
import io.bcs.fileserver.domain.model.file.content.FileContent.ContentPart;
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
public class ContentDownloader {
  private static final ApplicationLogger log = Loggers.applicationLogger(File.class);

  private final FileStorage fileStorage;
  private final ContentReceiver contentReceiver;

  /**
   * Download file content.
   *
   * @param file   The file which content is going to be downloaded
   * @param ranges The requested file ranges
   * @return Download operation complete promise
   */
  public Promise<Void> downloadContent(File file, Collection<Range> ranges) {
    FileFragments fragments = new FileFragments(ranges, file.getTotalLength());
    FileContent fileContent = new StorageFileContent(file, fragments.getParts());
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

  @Getter
  private class StorageFileContent implements FileContent {
    private File file;
    private ContentType type;
    private Collection<ContentPart> parts;

    public StorageFileContent(File file, Collection<ContentFragment> fragments) {
      super();
      this.file = file;
      this.type = recognizeContentType(fragments.size());
      this.parts = getContentParts(fileStorage, fragments);
    }

    @Override
    public ContentLocator getLocator() {
      return file.getLocator();
    }

    private Collection<ContentPart> getContentParts(FileStorage storage,
        Collection<ContentFragment> fragments) {
      return normalizeFragments(fragments).stream()
          .collect(
              Collectors
                  .mapping(
                      fragment -> new StorageContentPart(fragment,
                          storage.getAccessOnRead(file.getLocator(), fragment)),
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
