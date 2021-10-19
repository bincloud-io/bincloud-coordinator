package io.bcs.common.domain.model.message.templates;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import io.bcs.common.domain.model.message.MessageTemplate;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class TextMessageTemplate implements MessageTemplate {
	private final String textMessage;
	private final Map<String, Object> parameters = new LinkedHashMap<String, Object>();

	public TextMessageTemplate(String textMessage) {
		this(textMessage, new LinkedHashMap<String, Object>());
	}
	
	public TextMessageTemplate(String textMessage, Map<String, Object> parameters) {
		super();
		this.textMessage = textMessage;
		this.parameters.putAll(parameters);
	}
	
	public TextMessageTemplate(MessageTemplate prototype) {
		this(prototype.getText(), prototype.getParameters());
	}
	
	@Override
	public String getText() {
		return Optional.ofNullable(textMessage).orElse("");
	}

	@Override
	public Map<String, Object> getParameters() {
		return Collections.unmodifiableMap(parameters);
	}

	public <V> TextMessageTemplate withParameter(String name, V value) {
		parameters.put(name, value);
		return this;
	}
	
	public TextMessageTemplate withoutParameter(String name) {
		parameters.remove(name);
		return this;
	}
}
