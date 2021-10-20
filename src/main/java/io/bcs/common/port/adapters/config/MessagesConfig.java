package io.bcs.common.port.adapters.config;

import static io.bce.text.TextTransformers.chain;
import static io.bce.text.TextTransformers.deepDive;
import static io.bce.text.TextTransformers.trimming;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.bce.text.TextProcessor;
import io.bce.text.TextTemplate.Transformer;
import io.bce.text.transformers.BundleResolvingTransformer;
import io.bce.text.transformers.BundleResolvingTransformer.BundleResolver;
import io.bce.text.transformers.TemplateCompilingTransformer;
import io.bce.text.transformers.TemplateCompilingTransformer.TemplateCompiler;
import io.bce.text.transformers.compilers.HandlebarsTemplateCompiler;
import io.bce.text.transformers.resolvers.ResourceBundleResolver;
import io.bce.text.transformers.resolvers.ResourceBundleResolver.LocaleProvider;

@ApplicationScoped
public class MessagesConfig {
	@Inject
	private LocaleProvider localeProvider;
	
	@Produces
	public TextProcessor textProcessor() {
		return TextProcessor.create()
				.withTransformer(combinedTransformer());
	}
	
	private Transformer combinedTransformer() {
		Transformer combined = trimming();
		combined = chain(combined, bundleResolverTransformer());
		combined = chain(combined, templateCompilerTransformer());
		return deepDive(combined);	
	}
	
	private Transformer templateCompilerTransformer() {
		return new TemplateCompilingTransformer(templateCompiler());
	}
	
	private TemplateCompiler templateCompiler() {
		return new HandlebarsTemplateCompiler();
	}
	
	private Transformer bundleResolverTransformer() {
		return new BundleResolvingTransformer(bundleResolver());
	}
	
	private BundleResolver bundleResolver() {
		return new ResourceBundleResolver(localeProvider)
				.withResourceBundle("i18n/messages");
	}
	
//	private MessageTransformer bundleResolverTransformer() {
//		final BundleResolver bundleResolver = bundleResolver();
//		return messageTemplate -> new BundleResolvingTemplate(messageTemplate, bundleResolver);
//	}
//	
//	private MessageTransformer messageInterpolatorTransformer() {
//		final MessageInterpolator messageInterpolator = messageInterpolator();
//		return messageTemplate -> new MessageTextResolvingTemplate(messageTemplate, messageInterpolator);
//	}
//	
//	private MessageInterpolator messageInterpolator() {
//		return new MustacheInterpolator();
//	}
//	
//	private BundleResolver bundleResolver() {
//		return new ResourceBundleResolver(localeProvider)
//			.withResourceBundle("i18n/messages");
//	}
}
