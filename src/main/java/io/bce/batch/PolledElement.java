package io.bce.batch;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class PolledElement<D> {
	private final Long index;
	private final D data;
}
