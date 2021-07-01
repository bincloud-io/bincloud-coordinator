package io.bincloud.resources.port.adapter.endpoint.files.download.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.bincloud.resources.port.adapter.endpoint.files.download.HttpServletFileDownloadRequest;

public class HttpLatestRevisionDownloadServlet extends HttpFileDownloadServlet {
	private static final long serialVersionUID = 9105779650112273233L;

	@Override
	protected HttpServletFileDownloadRequest createFileDownloadContext(
			HttpServletRequest request, HttpServletResponse response) {
		return HttpServletFileDownloadRequest.createForLatestUpload(request, response);
	}
}
