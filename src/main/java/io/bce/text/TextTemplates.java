package io.bce.text;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.EqualsAndHashCode;

/**
 * This class provides text templates creators which the platform provides for
 * usage by default.
 * 
 * @author Dmitry Mikhaylenko
 *
 */
public class TextTemplates {
	/**
	 * Create text template is initialized by the empty string. Null pattern won't
	 * affect to the template text, because it isn't initialized by null.
	 * 
	 * @return The result text template
	 */
	public static final DefaultTextTemplate emptyTemplate() {
		return createBy("");
	}

	/**
	 * Create the text message from a random object. Object is converted to string
	 * using {@link Object#toString()} method and then the text template is
	 * initialized by the converted string
	 * 
	 * @param object A random object
	 * 
	 * @return The result text template
	 */
	public static final DefaultTextTemplate createBy(Object object) {
		return new DefaultTextTemplate(Optional.ofNullable(object).map(Object::toString), new HashMap<>());
	}

	/**
	 * Create the text message from the string value. Derived text template is
	 * null-safe, because if null value receives, the null pattern will returned by
	 * {@link TextTemplate#getTemplateText()} in the derived object.
	 * 
	 * @param template The template text
	 * @return The result text template
	 */
	public static final DefaultTextTemplate createBy(String template) {
		return createBy(template, new HashMap<>());
	}

	/**
	 * Create the text message from the string value and parameters map. Derived
	 * text template isnull-safe, because if null values receives, the null pattern
	 * will returned by {@link TextTemplate#getTemplateText()} in the derived object
	 * and empty map will returned by {@link TextTemplate#getParameters()}
	 * 
	 * @param template   The template text
	 * @param parameters The parameters for template processing
	 * @return The text template
	 */
	public static final DefaultTextTemplate createBy(String template, Map<String, Object> parameters) {
		return new DefaultTextTemplate(Optional.ofNullable(template), parameters);
	}

	/**
	 * Create the text message duplicate from the existing text template. Derived
	 * text template isnull-safe, because if null values receives, the null pattern
	 * will returned by {@link TextTemplate#getTemplateText()} in the derived object
	 * and empty map will returned by {@link TextTemplate#getParameters()}
	 * 
	 * @param textTemplate The existing template
	 * @return The text template
	 */
	public static final DefaultTextTemplate createBy(TextTemplate textTemplate) {
		return createBy(textTemplate.getTemplateText(), textTemplate.getParameters());
	}

	/**
	 * Wrap an already existing text message. The text template is created and
	 * initialized by the source template text and parameters. For example you have
	 * an unknown type text template and you need to reconfigure the source message
	 * (for example replace parameters value). You can wrap the source template and
	 * configure it using {@link DefaultTextTemplate#withNullPattern(String)},
	 * {@link DefaultTextTemplate#withParameter(String, Object)} and other. Also it
	 * may help in the situation when your source text template
	 * {@link TextTemplate#getTemplateText()} or/and
	 * {@link TextTemplate#getParameters()} methods returns computed values and you
	 * needs to prevent multiple computes.
	 * 
	 * @param template The source template
	 * @return The result text template
	 */
	public static final DefaultTextTemplate wrap(TextTemplate template) {
		return createBy(template.getTemplateText(), template.getParameters());
	}

	/**
	 * This class implements the text message template, which the platform provides
	 * by default. You can't create this object directly, but can re-configure this.
	 * To create this template you should use creation methods from the
	 * {@link TextTemplates} class. They cover most of cases assigned to text
	 * templates creation.
	 * 
	 * @author Dmitry Mikhaylenko
	 *
	 */
	@EqualsAndHashCode
	public static class DefaultTextTemplate implements TextTemplate {
		private static final String DEFAULT_NULL_PATTERN = "";

		private String nullPattern;
		private Optional<String> templateText;
		private Map<String, Object> parameters;

		private DefaultTextTemplate(Optional<String> templateText, Map<String, Object> parameters) {
			this(templateText, DEFAULT_NULL_PATTERN, parameters);
		}

		private DefaultTextTemplate(Optional<String> templateText, String nullPattern, Map<String, Object> parameters) {
			super();
			this.templateText = templateText;
			this.nullPattern = Optional.ofNullable(nullPattern).orElse(DEFAULT_NULL_PATTERN);
			this.parameters = Optional.ofNullable(parameters).map(HashMap::new).orElse(new HashMap<>());
		}

		@Override
		public final String getTemplateText() {
			return templateText.orElse(nullPattern);
		}

		@Override
		public final Map<String, Object> getParameters() {
			return parameters;
		}

		@Override
		public final String toString() {
			return getTemplateText();
		}

		@Override
		public TextTemplate transformBy(Transformer transformer) {
			return transformer.transform(this);
		}

		public final DefaultTextTemplate withNullPattern(String nullPattern) {
			return new DefaultTextTemplate(templateText, nullPattern, parameters);
		}

		public final DefaultTextTemplate withParameter(String key, Object value) {
			DefaultTextTemplate derived = new DefaultTextTemplate(templateText, nullPattern, parameters);
			derived.appendParameter(key, value);
			return derived;
		}

		private void appendParameter(String key, Object value) {
			this.parameters.put(key, value);
		}
	}
}
