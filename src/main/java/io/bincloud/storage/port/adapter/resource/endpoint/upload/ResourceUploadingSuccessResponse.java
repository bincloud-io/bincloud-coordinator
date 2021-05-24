package io.bincloud.storage.port.adapter.resource.endpoint.upload;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;

import io.bincloud.common.domain.model.error.MustNeverBeHappenedError;
import io.bincloud.storage.domain.model.resource.FileUploader.UploadedResource;

public class ResourceUploadingSuccessResponse extends Properties {
	private static final long serialVersionUID = 3941244708171978393L;
	private static final String DOWNLOAD_RESOURCE_LINK_PROPERTY = "resource.download.link";
	private static final String DOWNLOAD_RESOURCE_REVISION_LINK_PROPERTY = "revision.download.link";

	public ResourceUploadingSuccessResponse(String rootURL, UploadedResource uploadedResource) {
		super();
		put(DOWNLOAD_RESOURCE_LINK_PROPERTY, escapeURL(createResourceDownloadLink(rootURL, uploadedResource)));
		put(DOWNLOAD_RESOURCE_REVISION_LINK_PROPERTY, escapeURL(createRevisionDownloadLink(rootURL, uploadedResource)));
	}

	private String createResourceDownloadLink(String rootURL, UploadedResource uploadedResource) {
		return String.format("%s/resource/download?resourceId=%s", rootURL, uploadedResource.getResourceId());
	}

	private String createRevisionDownloadLink(String rootURL, UploadedResource uploadedResource) {
		return String.format("%s/resource/revision/download?resourceId=%s&fileId=%s", rootURL,
				uploadedResource.getResourceId(), uploadedResource.getFileId());
	}

	private String escapeURL(String unescapedURL) {
		try {
			return URLEncoder.encode(unescapedURL, "UTF-8");
		} catch (UnsupportedEncodingException error) {
			throw new MustNeverBeHappenedError(error);
		}
	}
}
