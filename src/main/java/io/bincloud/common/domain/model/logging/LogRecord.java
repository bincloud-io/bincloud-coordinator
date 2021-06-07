package io.bincloud.common.domain.model.logging;

import java.time.Instant;
import java.util.Map;

import io.bincloud.common.domain.model.error.ErrorDescriptor;
import io.bincloud.common.domain.model.message.MessageTemplate;
import io.bincloud.common.domain.model.message.MessageTransformer;
import io.bincloud.common.domain.model.message.templates.ErrorDescriptorTemplate;
import io.bincloud.common.domain.model.message.templates.StringifiedObjectTemplate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class LogRecord {
	private Level level;
	private Instant timestamp;
	@Getter(AccessLevel.NONE)
	private MessageTemplate message;
	
	public LogRecord(Level level, Object loggableObject) {
		this(level, new StringifiedObjectTemplate(loggableObject));
	}

	public LogRecord(Level level, String messageText) {
		this(level, new StringifiedObjectTemplate(messageText));
	}
	
	
	public LogRecord(Level level, ErrorDescriptor errorDescriptor) {
		this(level, new ErrorDescriptorTemplate(errorDescriptor));
	}
	
	public LogRecord(Level level, MessageTemplate message) {
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
	
	public LogRecord transformMessage(MessageTransformer transformer) {
		LogRecord derived = new LogRecord(this);
		derived.message = transformer.transformMessage(message);
		return derived;
	}
	
	public String getMessageText() {
		return message.getText();
	}
	
	public Map<String, Object> getMessageParameters() {
		return message.getParameters();
	}
}
