package io.bincloud.resources.application.download.operations;

import java.util.Optional;

import io.bincloud.resources.domain.model.contracts.FileDownloader.Range;

public class DownloadRange {
	private final Optional<Long> startOfRange;
	private final Optional<Long> endOfRange;
	private final Long fileSize;

	public DownloadRange(Range range, Long fileSize) {
		super();
		this.startOfRange = range.getStart();
		this.endOfRange = range.getEnd();
		this.fileSize = fileSize;
	}

	public Long getStartPosition() {
		return startOfRange.orElseGet(() -> endOfRange.map(value -> fileSize - value).orElse(0L));
	}

	public Long getSize() {
		Long startPosition = getStartPosition();
		Long endPosition = getEndPosition();
		return endPosition - startPosition;
	}
	
	private Long getEndPosition() {
		return endOfRange.map(endValue -> {
			if (!startOfRange.isPresent()) {
				return fileSize;
			} else {				
				return endValue;
			}
		}).orElse(fileSize);
	}
}
