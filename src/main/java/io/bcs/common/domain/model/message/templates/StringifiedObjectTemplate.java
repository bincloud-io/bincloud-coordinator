package io.bcs.common.domain.model.message.templates;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import io.bcs.common.domain.model.message.MessageTemplate;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class StringifiedObjectTemplate implements MessageTemplate {
	private Optional<Object> wrappedObject;
	
	public StringifiedObjectTemplate(Object wrappedObject) {
		super();
		this.wrappedObject = Optional.ofNullable(wrappedObject);
	}

	@Override
	public String getText() {
		return wrappedObject.map(Object::toString).orElse("");
	}

	@Override
	public Map<String, Object> getParameters() {
		return Collections.emptyMap();
	}

	@Override
	public String toString() {
		return getText();
	}
}
