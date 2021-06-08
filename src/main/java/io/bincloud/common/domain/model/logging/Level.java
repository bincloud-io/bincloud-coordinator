package io.bincloud.common.domain.model.logging;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Level {
	CRITIC, ERROR, WARN, INFO, DEBUG, TRACE;
}