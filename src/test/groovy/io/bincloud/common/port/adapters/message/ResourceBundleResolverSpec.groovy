package io.bincloud.common.port.adapters.message

import io.bincloud.common.domain.model.message.templates.BundleResolvingTemplate.BundleResolver
import io.bincloud.common.port.adapters.messages.LocaleProvider
import io.bincloud.common.port.adapters.messages.ResourceBundleResolver
import spock.lang.Narrative
import spock.lang.Specification

@Narrative("""
	To use Java SE resource bundles for message bundle resolving,
	as a developer I am needed in the component which will make it 
	compatible with internal abstractions 
""")
class ResourceBundleResolverSpec extends Specification {
	private static final String TEST_BUNDLE_NAME = "bundles/simple-bundle"
	
	private LocaleProvider localeProvider;
	
	def setup() {
		this.localeProvider = Stub(LocaleProvider)
		this.localeProvider.getLocale() >> new Locale("ru", "RU")
	}
	
	def "Scenario: resolve message template"() {
		given: "The resource bundle resolver"
		BundleResolver bundleResolver = new ResourceBundleResolver(localeProvider)
			.withResourceBundle(TEST_BUNDLE_NAME)
			
		expect: "The bundle id will resolved using resource bundle"
		bundleResolver.resolveBundle(templateId) == resolvedText
		
		where:
		templateId           | resolvedText
		"unknown"            | Optional.empty()
		"message.empty"      | Optional.of("")
		"message.default"    | Optional.of("DEFAULT MESSAGE")
		"message.localized"  | Optional.of("СООБЩЕНИЕ С ЛОКАЛИЗАЦИЕЙ")
		
	}
}
