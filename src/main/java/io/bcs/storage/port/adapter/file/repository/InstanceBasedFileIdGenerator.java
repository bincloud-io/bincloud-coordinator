package io.bcs.storage.port.adapter.file.repository;

import java.util.UUID;

import io.bcs.common.domain.model.generator.SequentialGenerator;
import io.bcs.common.domain.model.logging.Level;
import io.bcs.common.domain.model.logging.LogRecord;
import io.bcs.common.domain.model.logging.Loggers;
import io.bcs.common.domain.model.message.templates.TextMessageTemplate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InstanceBasedFileIdGenerator implements SequentialGenerator<String> {
	private final String instanceId;

	@Override
	public String nextValue() {
		String generatedId = String.format("%s-%s--%s", instanceId, Thread.currentThread().getId(), UUID.randomUUID());
		Loggers.applicationLogger(InstanceBasedFileIdGenerator.class).log(new LogRecord(Level.TRACE,
				new TextMessageTemplate("File id {{id}} generated").withParameter("id", generatedId)));
		return generatedId;
	}
}
