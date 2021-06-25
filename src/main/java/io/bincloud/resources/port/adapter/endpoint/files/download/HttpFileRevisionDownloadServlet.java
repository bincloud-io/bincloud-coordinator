package io.bincloud.resources.port.adapter.endpoint.files.download;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

public class HttpFileRevisionDownloadServlet extends HttpFileDownloadServlet {
	private static final long serialVersionUID = 1995389169243618144L;

	@Override
	protected DownloadFileRevision extractFileRevision(HttpServletRequest servletRequest) {
		DownloadFileRevision downloadFileRevision = super.extractFileRevision(servletRequest);
		return new DownloadFileRevision(downloadFileRevision.getResourceId(), extractFileId(servletRequest));
	}

	private Optional<String> extractFileId(HttpServletRequest servletRequest) {
		return Optional.ofNullable(servletRequest.getParameter("fileId")).map(String::trim)
				.filter(value -> !value.isEmpty());
	}
}
