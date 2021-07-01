package io.bincloud.resources.port.adapter.endpoint.files.download.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.bincloud.resources.port.adapter.endpoint.files.download.HttpServletFileDownloadRequest;

public class HttpSpecifiedRevisionDownloadServlet extends HttpFileDownloadServlet {
	private static final long serialVersionUID = -4149088280637759052L;

	@Override
	protected HttpServletFileDownloadRequest createFileDownloadContext(
			HttpServletRequest request, HttpServletResponse response) {
		return HttpServletFileDownloadRequest.createForSpecifiedUpload(request, response);
	}
}
