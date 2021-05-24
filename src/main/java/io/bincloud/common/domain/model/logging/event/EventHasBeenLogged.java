package io.bincloud.common.domain.model.logging.event;

import io.bincloud.common.domain.model.logging.AuditableEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EventHasBeenLogged {
	private final Long recordId;
	private final AuditableEvent loggedEvent;
}
