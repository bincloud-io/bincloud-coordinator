package io.bcs.common.port.adapters.config;

import java.util.Locale;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import io.bce.text.transformers.resolvers.ResourceBundleResolver.LocaleProvider;


@ApplicationScoped
public class I18nConfig {
	@Produces
	public LocaleProvider localeProvider() {
		return () -> Locale.getDefault();
	}
}
