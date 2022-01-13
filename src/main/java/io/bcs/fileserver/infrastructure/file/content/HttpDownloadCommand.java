package io.bcs.fileserver.infrastructure.file.content;

import io.bcs.fileserver.domain.model.file.Range;
import io.bcs.fileserver.domain.services.ContentService.DownloadCommand;
import io.bcs.fileserver.infrastructure.file.HttpRanges;
import java.util.Collection;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;

/**
 * This class implements the file download command.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@Getter
public class HttpDownloadCommand implements DownloadCommand {
  private final Optional<String> storageFileName;
  private final Collection<Range> ranges;

  /**
   * Create the file download command.
   *
   * @param storageFileNameParam The requested storage file name
   * @param httpRanges           The requested http ranges
   */
  public HttpDownloadCommand(@NonNull Optional<String> storageFileNameParam,
      @NonNull HttpRanges httpRanges) {
    super();
    this.storageFileName = storageFileNameParam;
    this.ranges = httpRanges.getRanges();
  }
}
