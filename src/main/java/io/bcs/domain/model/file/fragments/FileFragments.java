package io.bcs.domain.model.file.fragments;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

import io.bcs.domain.model.ContentFragment;
import lombok.EqualsAndHashCode;

public class FileFragments {
    private final Collection<ContentFragment> fragments;

    public FileFragments(Collection<Range> ranges, Long fileSize) {
        super();
        this.fragments = createFragments(ranges, fileSize);
    }

    public boolean isRequestedMultipleFragments() {
        return fragments.size() > 1;
    }

    public Collection<ContentFragment> getParts() {
        return fragments;
    }

    public ContentFragment getSinglePart() {
        return fragments.iterator().next();
    }

    private Collection<ContentFragment> createFragments(Collection<Range> ranges, Long fileSize) {
        return Optional.of(createFragmentsFromRanges(ranges, fileSize)).filter(fragments -> !fragments.isEmpty())
                .orElse(createFullSizeFragment(fileSize));
    }

    private Collection<ContentFragment> createFragmentsFromRanges(Collection<Range> ranges, Long fileSize) {
        return ranges.stream().map(range -> new FileFragment(range, fileSize)).distinct().collect(Collectors.toList());
    }

    private Collection<ContentFragment> createFullSizeFragment(Long fileSize) {
        return new HashSet<ContentFragment>(Arrays.asList(new FileFragment(fileSize)));
    }

    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    private static class FileFragment implements ContentFragment {
        private Long start;
        private Long end;

        public FileFragment(Long fileSize) {
            super();
            this.start = 0L;
            this.end = fileSize - 1;
        }

        public FileFragment(Range range, Long fileSize) {
            super();
            this.start = calculateStartPosition(range.getStart(), range.getEnd(), fileSize);
            this.end = calculateEndPosition(range.getStart(), range.getEnd(), fileSize);
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

        private Long calculateStartPosition(Optional<Long> startOfRange, Optional<Long> endOfRange, Long fileSize) {
            return startOfRange.orElseGet(() -> endOfRange.map(value -> fileSize - value).orElse(0L));
        }

        private Long calculateEndPosition(Optional<Long> startOfRange, Optional<Long> endOfRange, Long fileSize) {
            return endOfRange.filter(value -> value.compareTo(fileSize) <= 0).map(endValue -> {
                if (!startOfRange.isPresent()) {
                    return fileSize - 1;
                } else {
                    return endValue;
                }
            }).orElse(fileSize - 1);
        }

        private void checkRangeFormat() {
            if (isStartValueUnsatisfiable() || isFragmentSizeUnsatisfiable()) {
				throw new UnsatisfiableRangeFormatException();
            }
        }

        private boolean isFragmentSizeUnsatisfiable() {
            return getLength().compareTo(0L) <= 0;
        }

        private boolean isStartValueUnsatisfiable() {
            return start.compareTo(0L) < 0;
        }
    }
    
    public static class UnsatisfiableRangeFormatException extends RuntimeException {
        private static final long serialVersionUID = -5482382949706358492L;

        public UnsatisfiableRangeFormatException() {
            super("Range start value should be greater than range end value and range start value should be positive!");
        }
    }
}
