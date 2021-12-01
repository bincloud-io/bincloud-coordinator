package io.bcs.port.adapters.file;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.bcs.domain.model.file.Range;
import io.bcs.domain.model.file.UnsatisfiableRangeFormatException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor
public final class HttpRanges {
    static final String RANGES_HEADER_PATTERN = "^bytes=\\d*-\\d*(,\\d*-\\d*)*$";

    private final Optional<String> rangesHeaderValue;

    public Collection<Range> getRanges() {
        return rangesHeaderValue.map(value -> parseRanges(value)).orElse(Collections.emptyList());
    }

    private Collection<Range> parseRanges(String value) {
        return Arrays.stream(parseRangesEnumeration(value).split(",")).map(String::trim).map(HttpRange::new)
                .collect(Collectors.toList());
    }

    private String parseRangesEnumeration(String value) {
        checkThatHeaderValueIsWellFormatted(value);
        return value.substring(6);
    }

    private void checkThatHeaderValueIsWellFormatted(String value) {
        if (!value.matches(RANGES_HEADER_PATTERN)) {
            throw new UnsatisfiableRangeFormatException();
        }
    }

    @Getter
    private static final class HttpRange implements Range {
        static final Pattern RANGE_PARSING_PATTERN = Pattern.compile("(\\d*)?-(\\d*)?");
        private Optional<Long> start;
        private Optional<Long> end;

        public HttpRange(String range) {
            super();
            Matcher parser = parseRangeString(range);
            this.start = getParsedValue(parser, 1);
            this.end = getParsedValue(parser, 2);
        }

        private Optional<Long> getParsedValue(Matcher parser, int group) {
            return Optional.ofNullable(parser.group(group)).filter(value -> !value.isEmpty()).map(Long::valueOf);
        }

        private Matcher parseRangeString(String rangeString) {
            Matcher rangeMatcher = RANGE_PARSING_PATTERN.matcher(rangeString);
            rangeMatcher.find();
            return rangeMatcher;
        }
    }
}