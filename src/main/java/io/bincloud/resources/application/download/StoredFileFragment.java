package io.bincloud.resources.application.download;

import java.util.Optional;

import io.bincloud.resources.domain.model.contracts.Fragment;
import io.bincloud.resources.domain.model.contracts.Range;
import io.bincloud.resources.domain.model.errors.UnsatisfiableRangeFormatException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class StoredFileFragment implements Fragment {
	private Long start;
	private Long end;

	public StoredFileFragment(Range range, Long fileSize) {
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
		return endOfRange
				.filter(value -> value.compareTo(fileSize) <= 0)
				.map(endValue -> {
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
