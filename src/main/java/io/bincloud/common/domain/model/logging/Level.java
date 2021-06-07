package io.bincloud.common.domain.model.logging;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Level {
	CRITIC(50), ERROR(40), WARN(30), INFO(20), DEBUG(10), TRACE(0);

	@Getter
	private final int code;
}