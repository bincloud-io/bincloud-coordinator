package io.bincloud.storage.integration

import io.bincloud.storage.domain.model.file.File.IdGenerator
import io.bincloud.storage.port.adapter.file.FileIdGenerator
import spock.lang.Specification

class FileIdGeneratorSpec extends Specification {
	private static final String INSTANCE_ID = "12345"
	private static final String CHECK_REGEXP = "%s-%s--.{8}-.{4}-.{4}-.{4}-.{12}";
	def "Scenario: generate file identifier"() {
		setup:
			IdGenerator generator = new FileIdGenerator(INSTANCE_ID)
			String regexp = String.format(CHECK_REGEXP, INSTANCE_ID, Thread.currentThread().getId())
		expect:
			generator.generateId().matches(regexp)
	}
}
