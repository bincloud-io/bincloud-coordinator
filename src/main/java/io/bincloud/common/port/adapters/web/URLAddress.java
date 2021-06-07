package io.bincloud.common.port.adapters.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import io.bincloud.common.domain.model.error.MustNeverBeHappenedError;

public class URLAddress {
	private String url;
	
	public URLAddress(String url) {
		super();
		this.url = normalizeRoot(url);
	}

	public String getValue() {
		return this.url;
	}

	public URLAddress escape() {
		try {
			return new URLAddress(URLEncoder.encode(this.url, "UTF-8"));
		} catch (UnsupportedEncodingException error) {
			throw new MustNeverBeHappenedError(error);
		}
	}

	public URLAddress append(String uriPart) {
		return new URLAddress(String.format("%s/%s", url, normalizeURI(uriPart)));
	}

	public URLAddress append(String format, Object... params) {
		return append(String.format(format, params));
	}

	private String normalizeRoot(String rootPart) {
		if (rootPart.endsWith("/")) {
			return rootPart.substring(0, rootPart.length() - 1);
		}
		return rootPart;
	}
	
	private String normalizeURI(String uriPart) {
		if (uriPart.startsWith("/")) {
			return uriPart.substring(1);
		}
		return uriPart;
	}
}
