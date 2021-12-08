package io.bce.text.transformers.compilers;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import io.bce.MustNeverBeHappenedError;
import io.bce.text.transformers.TemplateCompilingTransformer.TemplateCompiler;
import java.io.IOException;
import java.util.Map;

/**
 * This class is the template compiler implementation, applying handlebars template tool.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class HandlebarsTemplateCompiler implements TemplateCompiler {
  private Handlebars handlebars = new Handlebars();

  @Override
  public String compile(String templateText, Map<String, String> parameters) {
    try {
      Template template = handlebars.compileInline(templateText);
      return template.apply(parameters);
    } catch (IOException error) {
      throw new MustNeverBeHappenedError(error);
    }
  }
}
