package io.bce.text;

import io.bce.text.TextTemplate.Transformer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * This class is responsible for text processing.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextProcessor {
  private final Transformer transformersChain;

  /**
   * Interpolate the text template.
   *
   * @param template The text template
   * @return The interpolated message
   */
  public String interpolate(TextTemplate template) {
    return template.transformBy(transformersChain).toString();
  }

  /**
   * Create the text processor with chained transformer.
   *
   * @param transformer The appended transformer
   * @return The derived text processor with chained transformer
   */
  public TextProcessor withTransformer(Transformer transformer) {
    return new TextProcessor(TextTransformers.chain(transformersChain, transformer));
  }

  /**
   * Create the default text processor.
   *
   * @return The text processor
   */
  public static final TextProcessor create() {
    return new TextProcessor(template -> template);
  }
}