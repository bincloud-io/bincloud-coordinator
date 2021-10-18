package io.bincloud.resources.domain.model.resource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResourceHasBeenDeleted {
	private final Long resourceId;
}
