package io.bce.text.transformers;

import io.bce.text.TextTemplate;
import io.bce.text.TextTemplate.Transformer;
import io.bce.text.TextTemplates;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/**
 * This class is a {@link Transformer} implementation, resolving templates text for messages, which
 * text template is an identifier in the remote bundle store. For example bundle store keeps message
 * text "Hello world" with identifier "hello". This transformer passes the identifier to the
 * {@link BundleResolver} and creates new text template with replaced text. If bundle id couldn't be
 * resolved it won't be replaced and will returned as is.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class BundleResolvingTransformer implements Transformer {
  private final BundleResolver bundleResolver;

  @Override
  public TextTemplate transform(TextTemplate sourceTemplate) {
    return resolveBundleSafely(sourceTemplate).<TextTemplate>map(resolvedTemplate -> TextTemplates
        .createBy(resolvedTemplate, sourceTemplate.getParameters())).orElse(sourceTemplate);

  }

  private Optional<String> resolveBundleSafely(TextTemplate template) {
    try {
      return bundleResolver.resolve(template.getTemplateText());
    } catch (Exception error) {
      return Optional.empty();
    }
  }

  /**
   * This interface declares the contract for bundle resolving.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public interface BundleResolver {
    public Optional<String> resolve(String bundleKey);
  }
}
