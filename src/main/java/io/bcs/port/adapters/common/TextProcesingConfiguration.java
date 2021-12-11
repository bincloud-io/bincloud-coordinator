package io.bcs.port.adapters.common;

import io.bce.text.Text;
import io.bce.text.TextProcessor;
import io.bce.text.TextTemplate.Transformer;
import io.bce.text.TextTransformers;
import io.bce.text.transformers.BundleResolvingTransformer;
import io.bce.text.transformers.BundleResolvingTransformer.BundleResolver;
import io.bce.text.transformers.TemplateCompilingTransformer;
import io.bce.text.transformers.TemplateCompilingTransformer.TemplateCompiler;
import io.bce.text.transformers.compilers.HandlebarsTemplateCompiler;
import io.bce.text.transformers.resolvers.ResourceBundleResolver;
import java.util.Locale;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class TextProcesingConfiguration {
  @Produces
  public TextProcessor textProcessor() {
    return TextProcessor.create()
        .withTransformer(TextTransformers.deepDive(TextTransformers.trimming()))
        .withTransformer(TextTransformers.deepDive(bundleResolvingTransformer()))
        .withTransformer(TextTransformers.deepDive(templateCompilingTransformer()));
  }

  public void configureTextProcessing(@Observes @Initialized(ApplicationScoped.class) Object init) {
    Text.configureProcessor(textProcessor());
  }

  private Transformer bundleResolvingTransformer() {
    return new BundleResolvingTransformer(bundleResolver());
  }

  private Transformer templateCompilingTransformer() {
    return new TemplateCompilingTransformer(templateCompiler());
  }

  private BundleResolver bundleResolver() {
    return new ResourceBundleResolver(() -> new Locale("ru")).withResourceBundle("i18n/messages");
  }

  private TemplateCompiler templateCompiler() {
    return new HandlebarsTemplateCompiler();
  }
}
