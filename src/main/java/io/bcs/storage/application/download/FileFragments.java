package io.bcs.storage.application.download;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

import io.bcs.storage.domain.model.contracts.download.Fragment;
import io.bcs.storage.domain.model.contracts.download.Range;
import io.bcs.storage.domain.model.errors.UnsatisfiableRangeFormatException;
import lombok.EqualsAndHashCode;

public class FileFragments {
	private final Collection<Fragment> fragments;

	public FileFragments(Collection<Range> ranges, Long fileSize) {
		super();
		this.fragments = createFragments(ranges, fileSize);
	}

	public boolean isRequestedMultipleFragments() {
		return fragments.size() > 1;
	}

	public Collection<Fragment> getParts() {
		return fragments;
	}

	public Fragment getSinglePart() {
		return fragments.iterator().next();
	}

	public Long getTotalSize() {
		return fragments.stream().map(Fragment::getSize).reduce(0L, (total, current) -> total + current);
	}

	private Collection<Fragment> createFragments(Collection<Range> ranges, Long fileSize) {
		return Optional.of(createFragmentsFromRanges(ranges, fileSize)).filter(fragments -> !fragments.isEmpty())
				.orElse(createFullSizeFragment(fileSize));
	}

	private Collection<Fragment> createFragmentsFromRanges(Collection<Range> ranges, Long fileSize) {
		return ranges.stream().map(range -> new FileFragment(range, fileSize)).distinct().collect(Collectors.toList());
	}

	private Collection<Fragment> createFullSizeFragment(Long fileSize) {
		return new HashSet<Fragment>(Arrays.asList(new FileFragment(fileSize)));
	}

	@EqualsAndHashCode(onlyExplicitlyIncluded = true)
	private static class FileFragment implements Fragment {
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
		public Long getStart() {
			return this.start;
		}

		@Override
		@EqualsAndHashCode.Include
		public Long getSize() {
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
			return getSize().compareTo(0L) <= 0;
		}

		private boolean isStartValueUnsatisfiable() {
			return start.compareTo(0L) < 0;
		}
	}
}
