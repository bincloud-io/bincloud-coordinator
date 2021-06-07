package io.bincloud.common.port.adapters.messages;

import java.util.Locale;

@FunctionalInterface
public interface LocaleProvider {
	public Locale getLocale();
}
