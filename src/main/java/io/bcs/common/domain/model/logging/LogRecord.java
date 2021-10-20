package io.bcs.common.domain.model.logging;

import java.time.Instant;
import java.util.Map;

import io.bce.domain.ErrorDescriptorTemplate;
import io.bce.domain.errors.ErrorDescriptor;
import io.bce.text.TextTemplate;
import io.bce.text.TextTemplate.Transformer;
import io.bce.text.TextTemplates;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class LogRecord {
	private Level level;
	private Instant timestamp;
	@Getter(AccessLevel.NONE)
	private TextTemplate message;
	
	public LogRecord(Level level, Object loggableObject) {		
		this(level, TextTemplates.createBy(loggableObject));
	}

	public LogRecord(Level level, String messageText) {
		this(level, TextTemplates.createBy(messageText));
	}
	
	
	public LogRecord(Level level, ErrorDescriptor errorDescriptor) {	
		this(level, ErrorDescriptorTemplate.createFor(errorDescriptor));
	}
	
	public LogRecord(Level level, TextTemplate message) {
		super();
		this.level = level;
		this.message = message;
		this.timestamp = Instant.now();
	}
	
	private LogRecord(LogRecord proto) {
		super();
		this.level = proto.level;
		this.timestamp = proto.timestamp;
		this.message = proto.message;
	}
	
	public LogRecord transformMessage(Transformer transformer) {
		LogRecord derived = new LogRecord(this);
		derived.message = transformer.transform(message);
		return derived;
	}
	
	public String getMessageText() {
		return message.getTemplateText();
	}
	
	public Map<String, Object> getMessageParameters() {
		return message.getParameters();
	}
}
