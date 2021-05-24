package io.bincloud.common.domain.model.logging;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Level {
	CRITIC(50), ERROR(40), WARN(30), INFO(20), DEBUG(10), TRACE(0);

	@Getter
	private final int code;

	public static Level findByCode(int code) {
		return Arrays.stream(values()).filter(item -> item.code == code).findFirst().orElse(INFO);
	}
}