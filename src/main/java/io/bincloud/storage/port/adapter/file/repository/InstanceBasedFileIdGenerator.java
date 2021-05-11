package io.bincloud.storage.port.adapter.file.repository;

import java.util.UUID;

import io.bincloud.common.domain.model.logging.Level;
import io.bincloud.common.domain.model.logging.LogRecord;
import io.bincloud.common.domain.model.logging.Loggers;
import io.bincloud.common.domain.model.message.templates.TextMessageTemplate;
import io.bincloud.storage.domain.model.file.File.IdGenerator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InstanceBasedFileIdGenerator implements IdGenerator {
	private final String instanceId;

	@Override
	public String generateId() {
		String generatedId = String.format("%s-%s--%s", instanceId, Thread.currentThread().getId(), UUID.randomUUID());
		Loggers.applicationLogger(InstanceBasedFileIdGenerator.class).log(new LogRecord(Level.TRACE,
				new TextMessageTemplate("File id {{id}} generated").withParameter("id", generatedId)));
		return generatedId;
	}
}
