package io.bincloud.resources.port.adapter.endpoint.files.download;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.bincloud.common.domain.model.error.UnexpectedSystemBehaviorException;
import io.bincloud.common.domain.model.io.transfer.DestinationPoint;
import io.bincloud.common.port.adapters.io.transfer.destinations.StreamDestination;
import io.bincloud.resources.domain.model.Constants;
import io.bincloud.resources.domain.model.contracts.RevisionPointer;
import io.bincloud.resources.domain.model.contracts.download.FileDownloader.FileDownloadRequest;
import io.bincloud.resources.domain.model.contracts.download.Range;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpServletFileDownloadRequest implements FileDownloadRequest {

	@Getter
	private final RevisionPointer revision;
	private final HttpRanges ranges;
	private final Supplier<DestinationPoint> destinationPointProvider;
	
	public boolean isMultiRange() {
		return ranges.isRangeRequest();
	}

	@Override
	public Collection<Range> getRanges() {
		return this.ranges.getRanges();
	}

	@Override
	public DestinationPoint getDestinationPoint() {
		return destinationPointProvider.get();
	}

	public static final HttpServletFileDownloadRequest createForLatestUpload(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		return createFor(servletRequest, servletResponse, request -> new ResourceLatestRevisionPointer(request));
	}
	
	public static final HttpServletFileDownloadRequest createForSpecifiedUpload(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		return createFor(servletRequest, servletResponse, request -> new ResourceSpecifiedRevisionPointer(request));
	}
	
	private static final HttpServletFileDownloadRequest createFor(HttpServletRequest servletRequest, HttpServletResponse response, Function<HttpServletRequest, RevisionPointer> revisionPointerProvider) {
		return new HttpServletFileDownloadRequest(revisionPointerProvider.apply(servletRequest), new HttpRanges(servletRequest), () -> {
			try {
				return new StreamDestination(response.getOutputStream());				
			} catch (IOException error) {
				throw new UnexpectedSystemBehaviorException(Constants.CONTEXT, error);
			}
		});
	}
}
