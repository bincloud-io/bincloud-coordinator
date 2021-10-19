package io.bcs.common.port.adapters.messages;

import java.io.UnsupportedEncodingException;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

import io.bcs.common.domain.model.error.MustNeverBeHappenedError;
import io.bcs.common.domain.model.message.templates.BundleResolvingTemplate.BundleResolver;

import java.util.Set;

public class ResourceBundleResolver implements BundleResolver {
	private Set<String> resourceBundles;
	private LocaleProvider localeProvider;

	public ResourceBundleResolver(LocaleProvider localeProvider) {
		super();
		this.resourceBundles = new LinkedHashSet<>();
		this.localeProvider = localeProvider;
	}

	private ResourceBundleResolver(ResourceBundleResolver proto) {
		super();
		this.resourceBundles = new LinkedHashSet<>(proto.resourceBundles);
		this.localeProvider = proto.localeProvider;
	}

	@Override
	public Optional<String> resolveBundle(String templateId) {
		for (String resourceBundle : resourceBundles) {
			Optional<String> message = getResolvedTextFromBundle(resourceBundle, templateId).map(String::trim);
			if (message.isPresent()) {
				return message;
			}
		}
		return Optional.empty();
	}

	public ResourceBundleResolver withResourceBundle(String bundleName) {
		ResourceBundleResolver result = new ResourceBundleResolver(this);
		result.appendResourceBundle(bundleName);
		return result;
	}

	private Optional<String> getResolvedTextFromBundle(String resourceBundle, String templateId) {
		return getLocalizedPropertiesResourceBundle(resourceBundle).filter(bundle -> bundle.containsKey(templateId))
				.map(bundle -> bundle.getString(templateId))
				.map(resolvedText -> {
					try {
						return new String(resolvedText.getBytes("ISO-8859-1"), "UTF-8");
					} catch (UnsupportedEncodingException error) {
						throw new MustNeverBeHappenedError(error);
					}
				});
	}

	private Optional<ResourceBundle> getLocalizedPropertiesResourceBundle(String resourceBundle) {
		Locale locale = localeProvider.getLocale();
		ClassLoader classLoader = getClass().getClassLoader();
		Control bundleControl = Control.getControl(Control.FORMAT_PROPERTIES);
		return Optional.ofNullable(ResourceBundle.getBundle(resourceBundle, locale, classLoader, bundleControl));
	}

	private void appendResourceBundle(String bundleName) {
		this.resourceBundles.add(bundleName);
	}
}
