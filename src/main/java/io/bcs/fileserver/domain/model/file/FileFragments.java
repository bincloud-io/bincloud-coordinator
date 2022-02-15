package io.bcs.fileserver.domain.model.file;

import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.text.TextTemplates;
import io.bcs.fileserver.domain.errors.UnsatisfiableRangeFormatException;
import io.bcs.fileserver.domain.model.storage.ContentFragment;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;

/**
 * This class transforms parsed file ranges to the normalized file fragments, according to
 * multi-range content download specification.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class FileFragments {
  private static final ApplicationLogger log = Loggers.applicationLogger(FileFragments.class);

  private final Collection<ContentFragment> fragments;

  public FileFragments(Collection<Range> ranges, Long fileSize) {
    super();
    this.fragments = createFragments(ranges, fileSize);
  }

  public Collection<ContentFragment> getParts() {
    return fragments;
  }

  private Collection<ContentFragment> createFragments(Collection<Range> ranges, Long fileSize) {
    return createFragmentsFromRanges(ranges, fileSize);
  }

  private Collection<ContentFragment> createFragmentsFromRanges(Collection<Range> ranges,
      Long fileSize) {
    return ranges.stream().map(range -> new FileFragment(range, fileSize)).distinct()
        .collect(Collectors.toList());
  }

  @EqualsAndHashCode(onlyExplicitlyIncluded = true)
  private static class FileFragment implements ContentFragment {
    private Long start;
    private Long end;

    public FileFragment(Range range, Long fileSize) {
      super();
      this.start = normalizeStartPosition(range.getStart(), range.getEnd(), fileSize);
      this.end = normalizeEndPosition(range.getStart(), range.getEnd(), fileSize);
      checkRangeFormat();
    }

    @Override
    @EqualsAndHashCode.Include
    public Long getOffset() {
      return this.start;
    }

    @Override
    @EqualsAndHashCode.Include
    public Long getLength() {
      return end - start + 1;
    }

    private Long normalizeStartPosition(Optional<Long> startOfRange, Optional<Long> endOfRange,
        Long fileSize) {
      log.debug(TextTemplates
          .createBy("Normalize start of range fragment position for [start: {{startOfRange}}, "
              + "end: {{endOfRange}}, fileSize: {{fileSize}}]")
          .withParameter("startOfRange", startOfRange).withParameter("endOfRange", endOfRange)
          .withParameter("fileSize", fileSize));
      return startOfRange.orElseGet(() -> {
        log.trace("The start of range is missing. The start range is tried to be calcilated as: "
            + "fileSize - endOfRange");
        return endOfRange.map(value -> fileSize - value).orElseGet(() -> {
          log.trace("The end of range is missing. Zero value will be returned by default");
          return 0L;
        });
      });
    }

    private Long normalizeEndPosition(Optional<Long> startOfRange, Optional<Long> endOfRange,
        Long fileSize) {
      log.debug(TextTemplates
          .createBy("Normalize end of range fragment position for [start: {{startOfRange}}, "
              + "end: {{endOfRange}}, fileSize: {{fileSize}}]")
          .withParameter("startOfRange", startOfRange).withParameter("endOfRange", endOfRange)
          .withParameter("fileSize", fileSize));
      return endOfRange.filter(value -> value.compareTo(fileSize) <= 0).map(endValue -> {
        if (!startOfRange.isPresent()) {
          log.trace("Start of range value is missing. The full size value will be returned.");
          return fileSize - 1;
        } else {
          log.trace("End of range has acceptable value. The end of range value will be returned.");
          return endValue;
        }
      }).orElseGet(() -> {
        log.trace("End of range is out of the file size. The full size value will be returned.");
        return fileSize - 1;
      });
    }

    private void checkRangeFormat() {
      if (isStartValueUnsatisfiable() || isFragmentSizeUnsatisfiable()) {
        throw new UnsatisfiableRangeFormatException();
      }
    }

    private boolean isFragmentSizeUnsatisfiable() {
      log.debug("The range format is unsatisfiable. Fragment size is negative.");
      return getLength().compareTo(0L) <= 0;
    }

    private boolean isStartValueUnsatisfiable() {
      log.debug("The range format is unsatisfiable. Start of range value is negative "
          + "or greater than end of range.");
      return start.compareTo(0L) < 0;
    }
  }
}
