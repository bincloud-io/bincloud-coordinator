package io.bincloud.common.port.adapters.messages;

import java.io.IOException;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

import io.bincloud.common.domain.model.error.MustNeverBeHappenedError;
import io.bincloud.common.domain.model.message.MessageInterpolator;
import io.bincloud.common.domain.model.message.MessageTemplate;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MustacheInterpolator implements MessageInterpolator {
	private Handlebars handlebars = new Handlebars();

	@Override
	public String interpolate(MessageTemplate messageTemplate) {
		try {
			return processTemplate(messageTemplate);
		} catch (IOException error) {
			throw new MustNeverBeHappenedError(error);
		}
	}
	
	private String processTemplate(MessageTemplate messageTemplate) throws IOException {
		Template template = handlebars.compileInline(messageTemplate.getText());
		return template.apply(messageTemplate.getParameters());
	}
}
