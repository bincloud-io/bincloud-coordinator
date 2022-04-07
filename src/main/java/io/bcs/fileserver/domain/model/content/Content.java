package io.bcs.fileserver.domain.model.content;

import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bcs.fileserver.domain.errors.ContentNotUploadedException;
import io.bcs.fileserver.domain.errors.ContentUploadedException;
import io.bcs.fileserver.domain.errors.FileDisposedException;
import io.bcs.fileserver.domain.model.content.FileContent.ContentPart;
import io.bcs.fileserver.domain.model.content.FileContent.ContentType;
import io.bcs.fileserver.domain.model.file.Disposition;
import io.bcs.fileserver.domain.model.file.FileFragments;
import io.bcs.fileserver.domain.model.file.FileStatus;
import io.bcs.fileserver.domain.model.file.Range;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * This class describes the file content entity.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@ToString
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Content {
  private static final ApplicationLogger log = Loggers.applicationLogger(Content.class);

  private static final String DEFAULT_STORAGE_FILE_NAME = "UNKNOWN";
  private static final String DEFAULT_FILE_NAME = "UNKNOWN";
  private static final Long DEFAULT_FILE_LENGTH = 0L;

  @Include
  @Default
  private StorageDescriptor storage = StorageDescriptor.unknownStorage();

  @Include
  @Default
  private String storageFileName = DEFAULT_STORAGE_FILE_NAME;

  @Default
  private MediaType mediaType = new MediaType();

  @Default
  private FileStatus status = FileStatus.DRAFT;

  @Default
  private String fileName = DEFAULT_FILE_NAME;

  @Default
  private Long totalLength = DEFAULT_FILE_LENGTH;

  /**
   * Download file content.
   *
   * @param ranges          The file content ranges.
   * @param contentReceiver The content receiver.
   * @return The file download complete promise.
   */
  public Promise<Void> downloadContent(Collection<Range> ranges, ContentReceiver contentReceiver) {
    return getFileState().downloadContent(ranges, contentReceiver);
  }

  /**
   * Upload file content.
   *
   * @param contentSource The content source
   * @param contentLength The content length
   * @return The file upload statistic promise
   */
  public Promise<FileUploadStatistic> uploadContent(ContentSource contentSource,
      Long contentLength) {
    return getFileState().uploadContent(contentSource, contentLength);
  }

  private FileState getFileState() {
    if (status == FileStatus.DRAFT) {
      return new DraftFile();
    }

    if (status == FileStatus.DISTRIBUTING) {
      return new DistributioningFile();
    }

    return new DisposedState();
  }

  private FileStorage getStorage() {
    return storage.getStorage();
  }

  private interface FileState {
    Promise<Void> downloadContent(Collection<Range> ranges, ContentReceiver contentReceiver);

    Promise<FileUploadStatistic> uploadContent(ContentSource contentSource, Long contentLength);
  }

  private class DraftFile implements FileState {
    @Override
    public Promise<Void> downloadContent(Collection<Range> ranges,
        ContentReceiver contentReceiver) {
      log.debug("The file content download is going to be performed from draft file");
      return Promises.rejectedBy(new ContentNotUploadedException());
    }

    @Override
    public Promise<FileUploadStatistic> uploadContent(ContentSource contentSource,
        Long contentLength) {
      return Promises.of(deferred -> {
        ContentLocator contentLocator =
            new DefaultContentLocator(storageFileName, storage.getStorageName());
        Destination<BinaryChunk> destination = getStorage().getAccessOnWrite(contentLocator);
        contentSource.sendContent(contentLocator, destination).delegate(deferred);
      });
    }
  }

  private class DistributioningFile implements FileState {
    @Override
    public Promise<Void> downloadContent(Collection<Range> ranges,
        ContentReceiver contentReceiver) {
      return Promises.of(deferred -> {
        FileFragments fragments = new FileFragments(ranges, totalLength);
        FileContent fileContent = new FileContentInfo(fragments.getParts());
        receiveContent(fileContent, contentReceiver).delegate(deferred);
      });
    }

    @Override
    public Promise<FileUploadStatistic> uploadContent(ContentSource contentSource,
        Long contentLength) {
      log.debug("The file content upload is going to be performed from distributed file");
      return Promises.rejectedBy(new ContentUploadedException());
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

  private class DisposedState implements FileState {
    @Override
    public Promise<Void> downloadContent(Collection<Range> ranges,
        ContentReceiver contentReceiver) {
      log.debug("The file content download is going to be performed from disposed file");
      return Promises.rejectedBy(new FileDisposedException());
    }

    @Override
    public Promise<FileUploadStatistic> uploadContent(ContentSource contentSource,
        Long contentLength) {
      log.debug("The file content upload is going to be performed from disposed file");
      return Promises.rejectedBy(new FileDisposedException());
    }
  }

  @Getter
  private class FileContentInfo implements FileContent {
    private ContentType type;
    private Collection<ContentPart> parts;

    public FileContentInfo(Collection<ContentFragment> fragments) {
      super();
      this.type = recognizeContentType(fragments.size());
      this.parts = buildContentParts(fragments);
    }

    @Override
    public ContentLocator getLocator() {
      return new Locator();
    }

    @Override
    public FileMetadata getFileMetadata() {
      return new FileSummaryInfo();
    }

    private Collection<ContentPart> buildContentParts(Collection<ContentFragment> fragments) {
      return normalizeFragments(fragments).stream()
          .collect(Collectors.mapping(ContentPartInfo::new, Collectors.toList()));
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

  @EqualsAndHashCode
  @RequiredArgsConstructor
  private class FullSizeFragment implements ContentFragment {
    @Include
    @Override
    public Long getOffset() {
      return 0L;
    }

    @Include
    @Override
    public Long getLength() {
      return totalLength;
    }
  }

  @Getter
  @EqualsAndHashCode
  @RequiredArgsConstructor
  private class ContentPartInfo implements ContentPart {
    private final ContentFragment contentFragment;

    @Override
    public Source<BinaryChunk> getContentSource() {
      return getStorage().getAccessOnRead(new Locator(), contentFragment);
    }
  }

  private class Locator implements ContentLocator {
    @Override
    public String getStorageName() {
      return storage.getStorageName();
    }

    @Override
    public String getStorageFileName() {
      return storageFileName;
    }
  }

  private class FileSummaryInfo implements FileMetadata {
    @Override
    public String getMediaType() {
      return mediaType.getMediaType();
    }

    @Override
    public String getFileName() {
      return fileName;
    }

    @Override
    public Long getTotalLength() {
      return totalLength;
    }

    @Override
    public Disposition getContentDisposition() {
      return mediaType.getDisposition();
    }
  }
}
