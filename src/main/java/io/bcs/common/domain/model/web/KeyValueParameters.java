package io.bcs.common.domain.model.web;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.bce.text.TextTemplates;
import io.bcs.common.domain.model.logging.ApplicationLogger;
import io.bcs.common.domain.model.logging.Level;
import io.bcs.common.domain.model.logging.LogRecord;
import io.bcs.common.domain.model.logging.Loggers;

public class KeyValueParameters {
	private static final String HEADER_VALUE_PARSING_ERROR_LOG_MESSAGE_TEMPLATE = "Header value of header "
			+ "[{{$headerName}}:{{$headerValue}}] couldn't be parsed because of error: \"{{$errorMessage}}\". "
			+ "The value will be skipped.";

	private Map<String, Collection<String>> httpHeaders = new HashMap<String, Collection<String>>();
	private ApplicationLogger logger;

	protected KeyValueParameters(Map<String, Collection<String>> headers) {
		super();
		this.httpHeaders.putAll(headers);
		this.logger = Loggers.applicationLogger(KeyValueParameters.class);
	}

	public boolean isContain(String key) {
		return httpHeaders.containsKey(key);
	}

	public Collection<String> getValues(String headerName) {
		return Collections.unmodifiableList(Optional.ofNullable(httpHeaders.get(headerName))
				.orElse(Collections.emptyList()).stream().filter(value -> value != null).map(String::trim)
				.filter(value -> !value.isEmpty()).collect(Collectors.toList()));
	}

	public Optional<String> getStringValue(String headerName) {
		return extractFirstValue(getValues(headerName));
	}

	public Collection<Byte> getByteValues(String headerName) {
		return getValues(headerName, Byte::valueOf);
	}

	public Optional<Byte> getByteValue(String headerName) {
		return extractFirstValue(getByteValues(headerName));
	}

	public Collection<Short> getShortValues(String headerName) {
		return getValues(headerName, Short::valueOf);
	}

	public Optional<Short> getShortValue(String headerName) {
		return extractFirstValue(getShortValues(headerName));
	}

	public Collection<Integer> getIntValues(String headerName) {
		return getValues(headerName, Integer::valueOf);
	}

	public Optional<Integer> getIntValue(String headerName) {
		return extractFirstValue(getIntValues(headerName));
	}

	public Collection<Long> getLongValues(String headerName) {
		return getValues(headerName, Long::valueOf);
	}

	public Optional<Long> getLongValue(String headerName) {
		return extractFirstValue(getLongValues(headerName));
	}

	public Collection<Float> getFloatValues(String headerName) {
		return getValues(headerName, Float::valueOf);
	}

	public Optional<Float> getFloatValue(String headerName) {
		return extractFirstValue(getFloatValues(headerName));
	}

	public Collection<Double> getDoubleValues(String headerName) {
		return getValues(headerName, Double::valueOf);
	}

	public Optional<Double> getDoubleValue(String headerName) {
		return extractFirstValue(getDoubleValues(headerName));
	}

	public <T> Collection<T> getValues(String headerName, Function<String, T> headerValueMapper) {
		return Collections.unmodifiableList(getValues(headerName).stream().<Optional<T>>map(value -> {
			try {
				return Optional.ofNullable(headerValueMapper.apply(value));
			} catch (Exception error) {
				logger.log(new LogRecord(Level.WARN,
						TextTemplates.createBy(HEADER_VALUE_PARSING_ERROR_LOG_MESSAGE_TEMPLATE)
								.withParameter("$headerName", headerName).withParameter("$headerValue", value)
								.withParameter("$errorMessage", error.getMessage())));
				return Optional.empty();
			}
		}).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList()));
	}

	private <T> Optional<T> extractFirstValue(Collection<T> headers) {
		return headers.stream().findFirst();
	}
}
