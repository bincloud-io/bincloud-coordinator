package io.bincloud.resources.port.adapter.endpoint.files.download;

import java.util.Optional;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class DownloadFileRevision {
	private final Optional<Long> resourceId;
	private final Optional<String> fileId;
}
