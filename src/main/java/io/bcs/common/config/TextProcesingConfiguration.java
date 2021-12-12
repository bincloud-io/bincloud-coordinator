package io.bcs.common.config;

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

/**
 * This class configures text processing.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@ApplicationScoped
public class TextProcesingConfiguration {
  /**
   * The text processor configuration.
   *
   * @return The text processor
   */
  @Produces
  public TextProcessor textProcessor() {
    return TextProcessor.create()
        .withTransformer(TextTransformers.deepDive(TextTransformers.trimming()))
        .withTransformer(TextTransformers.deepDive(bundleResolvingTransformer()))
        .withTransformer(TextTransformers.deepDive(templateCompilingTransformer()));
  }

  /**
   * Initialize text processing globally.
   *
   * @param init The CDI context event
   */
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
