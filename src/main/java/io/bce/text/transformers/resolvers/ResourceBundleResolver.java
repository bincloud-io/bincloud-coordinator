package io.bce.text.transformers.resolvers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.Set;
import java.util.stream.Collectors;

import io.bce.text.transformers.BundleResolvingTransformer.BundleResolver;

public final class ResourceBundleResolver implements BundleResolver {
	private static final KeyMatcher DEFAULT_KEY_MATCHER = (containingKey, requestedKey) -> containingKey
			.equals(requestedKey);

	private final Set<String> resourceBundles;
	private final LocaleProvider localeProvider;
	private final KeyMatcher keyMatcher;

	/**
	 * Create the resource bundle provider
	 * 
	 * @param localeProvider The locale provider
	 */
	public ResourceBundleResolver(LocaleProvider localeProvider) {
		this(localeProvider, DEFAULT_KEY_MATCHER);
	}

	/**
	 * Create the resource bundle provider
	 * 
	 * @param localeProvider The locale provider
	 * @param keyMatcher     The key matcher
	 */
	public ResourceBundleResolver(LocaleProvider localeProvider, KeyMatcher keyMatcher) {
		super();
		this.resourceBundles = new HashSet<>();
		this.localeProvider = localeProvider;
		this.keyMatcher = keyMatcher;
	}

	private ResourceBundleResolver(ResourceBundleResolver proto) {
		super();
		this.resourceBundles = new HashSet<>(proto.resourceBundles);
		this.localeProvider = proto.localeProvider;
		this.keyMatcher = proto.keyMatcher;
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
	 * Create derived {@link ResourceBundleResolver} with appended resource bundle
	 * name
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
		return getBundleContent(resourceBundle).entrySet().stream()
				.filter(entry -> keyMatcher.isMatched(entry.getKey(), bundleKey))
				.map(entry -> Optional.ofNullable(entry.getValue()).orElse("")).findFirst();
	}

	private Map<String, String> getBundleContent(String resourceBundle) {
		return getLocalizedPropertiesResorceBundle(resourceBundle).map(this::getBundlePropertiesMap)
				.orElse(Collections.emptyMap());
	}

	private Map<String, String> getBundlePropertiesMap(ResourceBundle resourceBundle) {
		return resourceBundle.keySet().stream()
				.collect(Collectors.toMap(key -> key, key -> resourceBundle.getString(key)));
	}

	private Optional<ResourceBundle> getLocalizedPropertiesResorceBundle(String resourceBundle) {
		Locale locale = localeProvider.getLocale();
		ClassLoader classLoader = getClass().getClassLoader();
		Control bundleControl = Control.getNoFallbackControl(Control.FORMAT_PROPERTIES);
		return Optional.ofNullable(ResourceBundle.getBundle(resourceBundle, locale, classLoader, bundleControl));
	}

	/**
	 * This interface describes the contract for the locale obtaining. This
	 * component should guaranteed return the locale which should be used for the
	 * locale resolving
	 * 
	 * @author Dmitry Mikhaylenko
	 *
	 */
	public interface LocaleProvider {
		/**
		 * Get the locale
		 * 
		 * @return The locale
		 */
		public Locale getLocale();
	}

	/**
	 * This interface describes the contract for the bundle keys matching. It is
	 * extension point allowing redefine logic of bundle key matcing with the
	 * requested key. For example if our bundle keys are regular expressions and we
	 * want to use regexp matching instead equality checking. By default the
	 * equality checking is used.
	 * 
	 * @author Dmitry Mikhaylenko
	 *
	 */
	public interface KeyMatcher {
		/**
		 * Match if the key, containing into the bundle is matched to the requested key
		 * 
		 * @param containingKey The key, containing into the bundle
		 * @param requestedKey  The requested key, which should be resolved
		 * @return True if they are matched and false otherwise
		 */
		public boolean isMatched(String containingKey, String requestedKey);
	}
}
