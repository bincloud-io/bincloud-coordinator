package io.bce.validation;

import io.bce.validation.ValidationContext.Rule;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class TypeSafeRule<T> implements Rule<T> {
	protected final Class<T> type;
	
	@Override
	public boolean isAcceptableFor(T value) {
		return type.isInstance(value);
	}
}
