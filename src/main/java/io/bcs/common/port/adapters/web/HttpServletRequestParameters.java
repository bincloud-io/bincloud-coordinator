package io.bcs.common.port.adapters.web;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import io.bcs.common.domain.model.web.KeyValueParameters;

public class HttpServletRequestParameters extends KeyValueParameters {
	public HttpServletRequestParameters(HttpServletRequest request) {
		super(extractFromServletRequest(request));
	}
	
	private static final Map<String, Collection<String>> extractFromServletRequest(HttpServletRequest servletRequest) {
		return Collections.list(servletRequest.getParameterNames()).stream()
				.collect(Collectors.toMap(key -> key, key -> Arrays.asList(servletRequest.getParameterValues(key))));
	}
}
