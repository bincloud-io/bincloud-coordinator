package io.bcs.common.port.adapters.config;

import java.util.Locale;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import io.bcs.common.port.adapters.messages.LocaleProvider;

@ApplicationScoped
public class I18nConfig {
	@Produces
	public LocaleProvider localeProvider() {
		return () -> Locale.getDefault();
	}
}
