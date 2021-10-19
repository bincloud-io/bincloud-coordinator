package io.bcs.common.domain.model.message.templates

import org.junit.experimental.categories.Category

import io.bcs.common.domain.model.error.ErrorDescriptor
import io.bcs.common.domain.model.message.MessageTemplate
import io.bcs.common.domain.model.message.templates.ErrorDescriptorTemplate
import spock.lang.Narrative
import spock.lang.Specification

@Narrative("""
	To convert internal error descriptors to the error messages,
	as a developer I am needed in a component which will adapt
	error descriptor to interpolated template. 
""")
class ErrorDescriptorTemplateSpec extends Specification {
	def "Scenario: adapt error descriptor to message template using error descriptor template"() {
		given: "The error descriptor"
		ErrorDescriptor descriptor = createDescriptor()
		
		when: "The error descriptor has been wrapped by the error descriptor template"
		MessageTemplate template = new ErrorDescriptorTemplate(descriptor)
		
		then: "The message template text will be contain context and code"
		template.getText() == "ERROR.CONTEXT.100"
		
		and: "The parameters map will be equal to error descriptor details"
		template.getParameters() == descriptor.getDetails()
	}
	
	private ErrorDescriptor createDescriptor() {
		Map<String, Object> details = new HashMap<>();
		details.put("key", "value")
		return Stub(ErrorDescriptor) {
			getErrorCode() >> 100L;
			getContext() >> "CONTEXT";
			getDetails() >> details;
		}
	}
}
