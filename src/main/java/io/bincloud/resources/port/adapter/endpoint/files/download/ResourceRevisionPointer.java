package io.bincloud.resources.port.adapter.endpoint.files.download;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import io.bincloud.resources.domain.model.contracts.RevisionPointer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class ResourceRevisionPointer implements RevisionPointer {
	@Getter(value = AccessLevel.PROTECTED)
	private final HttpServletRequest request;

	@Override
	public final Optional<Long> getResourceId() {
		try {
			return Optional.ofNullable(request.getParameter("resourceId")).map(Long::valueOf);
		} catch (Exception error) {
			return Optional.empty();
		}
	}
}
