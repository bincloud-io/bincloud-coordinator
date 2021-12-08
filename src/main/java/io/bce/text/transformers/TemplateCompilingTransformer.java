package io.bce.text.transformers;

import io.bce.text.TextTemplate;
import io.bce.text.TextTemplate.Transformer;
import io.bce.text.TextTemplates;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

/**
 * This class is the transformer implementation, applying the template tool. Template tool applying
 * logic defined into {@link TemplateCompiler} implementation.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class TemplateCompilingTransformer implements Transformer {
  private final TemplateCompiler compiler;

  @Override
  public TextTemplate transform(TextTemplate sourceTemplate) {
    String interpolated =
        compiler.compile(sourceTemplate.getTemplateText(), extractParameters(sourceTemplate));
    return TextTemplates.createBy(interpolated, sourceTemplate.getParameters());
  }

  private Map<String, String> extractParameters(TextTemplate template) {
    return template.getParameters().entrySet().stream()
        .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().toString()));
  }

  /**
   * This interface describes the contract for template compiling. To use a template tool for
   * template compiling you have to create component implementing this interface and make template
   * compilation in the implemented {@link TemplateCompiler#compile(String, Map)} method.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public interface TemplateCompiler {
    /**
     * Compile the text template to string.
     *
     * @param template   The template text
     * @param parameters The template parameters
     * @return The compiled
     */
    public String compile(String template, Map<String, String> parameters);
  }
}
