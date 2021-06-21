package io.bincloud.resources.port.adapter.endpoint.files.download;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.bincloud.resources.domain.model.contracts.FileDownloader.DownloadCallback;
import io.bincloud.resources.domain.model.contracts.FileDownloader.DownloadedFile;
import io.bincloud.resources.port.adapter.endpoint.files.ServletResponseHandler;

public class HttpFileDownloadServlet extends HttpServlet {
	private static final long serialVersionUID = -8709972899799498114L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
	}
	

	
	private class HttpFileDownloadCallback implements DownloadCallback {
		private ServletResponseHandler servletResponseHandler;
		
		@Override
		public void onDownload(DownloadedFile file) {
			
		}

		@Override
		public void onError(Exception error) {
			
		}
		
	}
}
