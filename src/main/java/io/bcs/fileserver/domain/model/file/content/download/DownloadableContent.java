package io.bcs.fileserver.domain.model.file.content.download;

import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bcs.fileserver.domain.errors.ContentNotUploadedException;
import io.bcs.fileserver.domain.errors.FileDisposedException;
import io.bcs.fileserver.domain.model.file.Disposition;
import io.bcs.fileserver.domain.model.file.FileStatus;
import io.bcs.fileserver.domain.model.file.content.download.FileContent.ContentPart;
import io.bcs.fileserver.domain.model.file.content.download.FileContent.ContentType;
import io.bcs.fileserver.domain.model.storage.ContentFragment;
import io.bcs.fileserver.domain.model.storage.ContentLocator;
import io.bcs.fileserver.domain.model.storage.FileStorage;
import io.bcs.fileserver.domain.model.storage.StorageDescriptor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * This class describes the entity, describing content is allowable for downloading.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@ToString
@SuperBuilder
@NoArgsConstructor
@Getter(value = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DownloadableContent {
  private static final ApplicationLogger log = Loggers.applicationLogger(DownloadableContent.class);

  private static final String DEFAULT_DISTRIBUTION_POINT = "DEFAULT";
  private static final String DEFAULT_STORAGE_FILE_NAME = "UNKNOWN";
  private static final String DEFAULT_FILE_NAME = "UNKNOWN";
  private static final Long DEFAULT_FILE_LENGTH = 0L;

  @Include
  @Default
  private String storageFileName = DEFAULT_STORAGE_FILE_NAME;

  @Include
  @Default
  private String distributionPointName = DEFAULT_DISTRIBUTION_POINT;

  @Default
  private StorageDescriptor storage = StorageDescriptor.unknownStorage();

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
  }

  private class DraftFile implements FileState {
    @Override
    public Promise<Void> downloadContent(Collection<Range> ranges,
        ContentReceiver contentReceiver) {
      log.debug("The file content download is going to be performed from draft file");
      return Promises.rejectedBy(new ContentNotUploadedException());
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
