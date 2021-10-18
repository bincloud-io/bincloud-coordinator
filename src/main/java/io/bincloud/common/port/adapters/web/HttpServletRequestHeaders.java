package io.bincloud.common.port.adapters.web;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import io.bincloud.common.domain.model.web.KeyValueParameters;

public class HttpServletRequestHeaders extends KeyValueParameters {
	public HttpServletRequestHeaders(HttpServletRequest servletRequest) {
		super(extractFromServletRequest(servletRequest));
	}

	private static final Map<String, Collection<String>> extractFromServletRequest(HttpServletRequest servletRequest) {
		return Collections.list(servletRequest.getHeaderNames()).stream()
				.collect(Collectors.toMap(value -> value, value -> Collections.list(servletRequest.getHeaders(value))));
	}
}
