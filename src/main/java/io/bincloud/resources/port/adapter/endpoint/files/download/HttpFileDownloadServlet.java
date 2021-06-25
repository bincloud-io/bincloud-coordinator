package io.bincloud.resources.port.adapter.endpoint.files.download;

import java.io.IOException;
import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.bincloud.common.port.adapters.web.AsyncServletOperationExecutor;
import io.bincloud.resources.domain.model.contracts.download.FileDownloader;

public class HttpFileDownloadServlet extends HttpServlet {
	private static final long serialVersionUID = -8709972899799498114L;
	
	@Inject
	private FileDownloader fileDownloader;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		AsyncServletOperationExecutor asyncExecutor = new AsyncServletOperationExecutor(request, response);
		asyncExecutor.execute(asyncContext -> {
			// PROCESS DOWNLOAD
		});
	}

	protected DownloadFileRevision extractFileRevision(HttpServletRequest servletRequest) {
		return new DownloadFileRevision(extractResourceId(servletRequest), Optional.empty());
	}

	private Optional<Long> extractResourceId(HttpServletRequest servletRequest) {
		try {
			return Optional.ofNullable("resourceId").map(String::trim).filter(value -> !value.isEmpty())
					.map(Long::valueOf);
		} catch (NumberFormatException error) {
			return Optional.empty();
		}
	}
}
