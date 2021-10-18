package io.bincloud.files.port.adapter.file.web.download;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import io.bincloud.common.domain.model.web.KeyValueParameters;
import io.bincloud.common.port.adapters.web.HttpServletRequestHeaders;
import io.bincloud.files.domain.model.contracts.download.Range;
import io.bincloud.files.domain.model.errors.UnsatisfiableRangeFormatException;
import lombok.Getter;

public class HttpRanges {
	private static final String RANGES_HEADER_NAME = "Ranges";
	private static final String RANGES_HEADER_PATTERN = "^bytes=\\d*-\\d*(,\\d*-\\d*)*$";
	private static final Pattern RANGE_PARSING_PATTERN = Pattern.compile("(\\d*)-(\\d*)");
	
	private Optional<String> rangesHeaderValue;

	public HttpRanges(HttpServletRequest servletRequest) {
		super();
		KeyValueParameters headers = new HttpServletRequestHeaders(servletRequest);
		this.rangesHeaderValue = headers.getStringValue(RANGES_HEADER_NAME);
	}
	
	public boolean isRangeRequest() {
		return rangesHeaderValue.isPresent();
	}
	
	public Collection<Range> getRanges() {
		return Arrays.stream(getRangesEnumeration().split(","))
				.map(ParsedRange::new).collect(Collectors.toList());
	}
	
	private String getRangesEnumeration() {
		return rangesHeaderValue
			.filter(value -> value.matches(RANGES_HEADER_PATTERN))
			.map(value -> value.substring(6)).orElseThrow(() -> new UnsatisfiableRangeFormatException());
	}
	
	@Getter
	private class ParsedRange implements Range {
		private Optional<Long> start;
		private Optional<Long> end;
		
		public ParsedRange(String rangeString) {
			super();
			Matcher parser = parseRangeString(rangeString);
			this.start = Optional.ofNullable(parser.group(1)).map(Long::valueOf);
			this.end = Optional.ofNullable(parser.group(2)).map(Long::valueOf);
		}
		
		private Matcher parseRangeString(String rangeString) {
			Matcher rangeMatcher = RANGE_PARSING_PATTERN.matcher(rangeString);
			checkThatRangeIsSuccessfullyParsed(rangeMatcher);
			return rangeMatcher;
		}

		private void checkThatRangeIsSuccessfullyParsed(Matcher rangeMatcher) {
			if (!rangeMatcher.find()) {
				throw new UnsatisfiableRangeFormatException();
			}
		}
	}
}
