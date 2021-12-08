package io.bce.text;

import io.bce.text.TextTemplate.Transformer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/**
 * This class provides text transformers creators which the platform provides for usage by default.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class TextTransformers {
  /**
   * Create the transformer trimming the {@link TextTemplate#getTemplateText()} value.
   *
   * @return The trimming transformer
   */
  public static final Transformer trimming() {
    return new TrimmingTransformer();
  }

  /**
   * Create transformer chaining two transformers.
   *
   * @param original An original transformer applying first
   * @param next     A transformer applying after the original transformer
   * @return The result of two transformation applying
   */
  public static final Transformer chain(Transformer original, Transformer next) {
    return new ChainTransformer(original, next);
  }

  /**
   * Create the "deep dive" transformer, which applies the specified transformation to each
   * parameter, implementing the {@link TextTemplate} interface and create derived text template
   * with transformed parameters. Then it applies the transformation to the derived text template.
   * It is important that the transformer traverses all parameters recursively (parameters of root,
   * parameters of the parameters of root...).
   *
   * @param transformer The non-recursive transformer applying to the message template
   * @return The recursive deep-dive transformer applying the non-recursive transformer to the
   *         parameters recursively
   */
  public static final Transformer deepDive(Transformer transformer) {
    return new DeepDiveTransformer(transformer);
  }

  private static class TrimmingTransformer implements Transformer {
    @Override
    public TextTemplate transform(TextTemplate sourceTemplate) {
      return TextTemplates.createBy(sourceTemplate.getTemplateText().trim(),
          sourceTemplate.getParameters());
    }
  }

  @RequiredArgsConstructor
  private static class ChainTransformer implements Transformer {
    private final Transformer originalTransformer;
    private final Transformer nextTransformer;

    @Override
    public TextTemplate transform(TextTemplate sourceTemplate) {
      return sourceTemplate.transformBy(originalTransformer).transformBy(nextTransformer);
    }
  }

  @RequiredArgsConstructor
  private static class DeepDiveTransformer implements Transformer {
    private final Transformer transformer;

    @Override
    public TextTemplate transform(TextTemplate sourceTemplate) {
      String templateText = sourceTemplate.getTemplateText();
      Map<String, Object> templateParameters = transformParametersRecursively(sourceTemplate);
      return TextTemplates.createBy(templateText, templateParameters).transformBy(transformer);
    }

    private Map<String, Object> transformParametersRecursively(TextTemplate template) {
      return template.getParameters().entrySet().stream()
          .collect(Collectors.toMap(Entry::getKey, entry -> {
            Object value = entry.getValue();
            if (value instanceof TextTemplate) {
              return ((TextTemplate) value).transformBy(new DeepDiveTransformer(transformer))
                  .toString();
            }
            return value;
          }));
    }
  }
}
