package io.bincloud.files.port.adapters.repository

import io.bincloud.common.domain.model.generator.SequentialGenerator
import io.bincloud.files.port.adapter.file.repository.InstanceBasedFileIdGenerator
import spock.lang.Specification

class InstanceBasedFileIdGeneratorSpec extends Specification {
	private static final String INSTANCE_ID = "12345"
	private static final String CHECK_REGEXP = "%s-%s--.{8}-.{4}-.{4}-.{4}-.{12}";
	def "Scenario: generate file identifier"() {
		setup:
			SequentialGenerator<String> generator = new InstanceBasedFileIdGenerator(INSTANCE_ID)
			String regexp = String.format(CHECK_REGEXP, INSTANCE_ID, Thread.currentThread().getId())
		expect:
			generator.nextValue().matches(regexp)
	}
}
