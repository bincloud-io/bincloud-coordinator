package io.bcs.common.domain.model.message.templates;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import io.bcs.common.domain.model.message.MessageTemplate;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class RecursiveMessageProcessingTemplate implements MessageTemplate {
	@EqualsAndHashCode.Include
	@Getter(value = AccessLevel.PROTECTED)
	private final MessageTemplate messageTemplate;
	
	@Override
	public Map<String, Object> getParameters() {
		Map<String, Object> result = new HashMap<String, Object>();
		for (Entry<String, Object> entry : messageTemplate.getParameters().entrySet()) {
			Object value = entry.getValue();
			if (value instanceof MessageTemplate) {
				value = this.processMessageTemplateParameter((MessageTemplate) value);
			}
			result.put(entry.getKey(), value);
		}
		return result;
	}
	
	protected abstract Object processMessageTemplateParameter(MessageTemplate messageTemplateParameter);
}
