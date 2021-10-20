package io.bcs.storage.port.adapter.file.repository;

import java.util.UUID;

import io.bce.text.TextTemplates;
import io.bcs.common.domain.model.generator.SequentialGenerator;
import io.bcs.common.domain.model.logging.Level;
import io.bcs.common.domain.model.logging.LogRecord;
import io.bcs.common.domain.model.logging.Loggers;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InstanceBasedFileIdGenerator implements SequentialGenerator<String> {
	private final String instanceId;

	@Override
	public String nextValue() {
		String generatedId = String.format("%s-%s--%s", instanceId, Thread.currentThread().getId(), UUID.randomUUID());
		Loggers.applicationLogger(InstanceBasedFileIdGenerator.class)
				.log(new LogRecord(Level.TRACE, TextTemplates.createBy("File id {{id}} generated")));

		return generatedId;
	}
}
