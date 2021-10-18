package io.bce.text.transformers.resolvers;

import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.Set;

import io.bce.text.transformers.BundleResolvingTransformer.BundleResolver;

public final class ResourceBundleResolver implements BundleResolver {
	private final Set<String> resourceBundles;
	private final LocaleProvider localeProvider;

	/**
	 * Create the resource bundle provider
	 * 
	 * @param localeProvider The locale provider
	 */
	public ResourceBundleResolver(LocaleProvider localeProvider) {
		super();
		this.resourceBundles = new HashSet<>();
		this.localeProvider = localeProvider;
	}
	
	private ResourceBundleResolver(ResourceBundleResolver proto) {
		super();
		this.resourceBundles = new HashSet<>(proto.resourceBundles);
		this.localeProvider = proto.localeProvider;
	}

	@Override
	public final Optional<String> resolve(String bundleKey) {
		for (String resourceBundle : resourceBundles) {
			Optional<String> resolvedTemplate = resolveBundleUsingBundle(resourceBundle, bundleKey).map(String::trim);
			if (resolvedTemplate.isPresent()) {
				return resolvedTemplate;
			}
		}
		return Optional.empty();
	}
	
	/**
	 * Create derived {@link ResourceBundleResolver} with appended
	 * resource bundle name
	 *  
	 * @param bundleName The resource bundle name 
	 * @return The derived resource bundle resolver
	 */
	public final ResourceBundleResolver withResourceBundle(String bundleName) {
		ResourceBundleResolver derived = new ResourceBundleResolver(this);
		derived.appendResourceBundle(bundleName);
		return derived;
	}
	
	private void appendResourceBundle(String bundleName) {
		this.resourceBundles.add(bundleName);
	}

	private Optional<String> resolveBundleUsingBundle(String resourceBundle, String bundleKey) {
		return getLocalizedPropertiesResorceBundle(resourceBundle).filter(bundle -> bundle.containsKey(bundleKey))
				.map(bundle -> bundle.getString(bundleKey));
	}

	private Optional<ResourceBundle> getLocalizedPropertiesResorceBundle(String resourceBundle) {
		Locale locale = localeProvider.getLocale();
		ClassLoader classLoader = getClass().getClassLoader();
		Control bundleControl = Control.getNoFallbackControl(Control.FORMAT_PROPERTIES);
		return Optional.ofNullable(ResourceBundle.getBundle(resourceBundle, locale, classLoader, bundleControl));
	}

	public interface LocaleProvider {
		public Locale getLocale();
	}
}
