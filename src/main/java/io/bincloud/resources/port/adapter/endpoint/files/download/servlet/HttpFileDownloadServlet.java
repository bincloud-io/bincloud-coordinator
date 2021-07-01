package io.bincloud.resources.port.adapter.endpoint.files.download.servlet;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.bincloud.common.domain.model.message.MessageProcessor;
import io.bincloud.common.port.adapters.web.AsyncServletOperationExecutor;
import io.bincloud.resources.domain.model.contracts.download.DownloadOperation;
import io.bincloud.resources.domain.model.contracts.download.FileDownloader;
import io.bincloud.resources.domain.model.contracts.download.FileDownloader.FileDownloadRequest;
import io.bincloud.resources.port.adapter.endpoint.files.download.HttpServletFileDownloadListener;
import io.bincloud.resources.port.adapter.endpoint.files.download.HttpServletFileDownloadRequest;
import io.bincloud.resources.port.adapter.endpoint.files.download.HttpServletFileRangesDownloadListener;

public abstract class HttpFileDownloadServlet extends HttpServlet {
	private static final long serialVersionUID = -8709972899799498114L;
	
	@Inject
	private FileDownloader fileDownloader;
	
	@Inject
	private MessageProcessor messageProcessor;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		AsyncServletOperationExecutor asyncExecutor = new AsyncServletOperationExecutor(request, response);
		asyncExecutor.execute(asyncContext -> {
			DownloadOperation downloadOperation = createDownloadOperation(asyncContext, request, response);
			downloadOperation.downloadFile();
		});
	}
	
	private DownloadOperation createDownloadOperation(AsyncContext asyncContext, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		HttpServletFileDownloadRequest downloadRequest = createFileDownloadContext(servletRequest, servletResponse);
		if (downloadRequest.isMultiRange()) {
			return createFileRangesDownloadOperation(asyncContext, downloadRequest);
		} 
		return createFileDownloadOperation(asyncContext, downloadRequest);
	} 
	
	private DownloadOperation createFileDownloadOperation(AsyncContext asyncContext, FileDownloadRequest downloadRequest) {
		return fileDownloader.downloadFile(downloadRequest, new HttpServletFileDownloadListener(asyncContext, messageProcessor)); 
	}
	
	private DownloadOperation createFileRangesDownloadOperation(AsyncContext asyncContext, FileDownloadRequest downloadRequest) {
		return fileDownloader.downloadFileRanges(downloadRequest, new HttpServletFileRangesDownloadListener(asyncContext, messageProcessor));
	}
	
	
	protected abstract HttpServletFileDownloadRequest createFileDownloadContext(HttpServletRequest request, HttpServletResponse response);
}
