package io.bincloud.common.port.adapters.validation

import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.COMPILE
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.RUNTIME
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.TEST

import javax.inject.Inject
import javax.validation.Validator

import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.shrinkwrap.api.Archive
import org.junit.runner.RunWith

import io.bincloud.common.domain.model.error.ApplicationException
import io.bincloud.common.domain.model.logging.Loggers
import io.bincloud.common.domain.model.message.MessageProcessor
import io.bincloud.common.domain.model.message.MessageTemplate
import io.bincloud.common.domain.model.message.templates.MessageTextResolvingTemplate
import io.bincloud.common.domain.model.validation.ValidationService
import io.bincloud.common.domain.model.validation.ValidationState
import io.bincloud.common.port.adapters.messages.MustacheInterpolator
import io.bincloud.testing.archive.ArchiveBuilder
import spock.lang.Narrative
import spock.lang.Specification

@Narrative("""
	To make validation service compatible to JSR bean validation specification,
	as a developer I am needed in the corresponding component which will adapt 
	JSR validation API to the internal contract.
""")
@RunWith(ArquillianSputnik)
class JSRBeanValidationServiceITSpec extends Specification {

	@Deployment
	public static final Archive "create deployment"() {
		return ArchiveBuilder.jar("jsr-bean-validation-spec.jar")
				.resolveDependencies("pom.xml")
				.withScopes(COMPILE, RUNTIME, TEST)
				.resolveDependency("com.github.jknack", "handlebars")
				.apply()
				.appendPackagesRecursively(ApplicationException.getPackage().getName())
				.appendPackagesRecursively(Loggers.getPackage().getName())
				.appendPackagesRecursively(MessageTemplate.getPackage().getName())
				.appendPackagesRecursively(ValidationService.getPackage().getName())
				.appendClasses(AlwaysFailed, AlwaysFailedValidator, JSRBeanValidationService, MustacheInterpolator)
				.appendManifestResource("META-INF/beans.xml", "beans.xml")
				.build()
	}

	private final MessageProcessor messageProcessor = new MessageProcessor().configure()
		.withTransformation({template -> new MessageTextResolvingTemplate(template, new MustacheInterpolator())})
		.apply();

	@Inject
	private Validator validator;
	
	def "Scenario: validate object with validation framework"() {
		given: "The wrong bean"
		WrongBean wrongBean = new WrongBean()

		and: "The validation service"
		ValidationService validationService = new JSRBeanValidationService(validator, messageProcessor)

		when: "The validation has been requested"
		ValidationState validationState = validationService.validate(wrongBean, ValidationGroup)

		then: "The result validation state will contain all expected constraints"
		validationState == createExpectedValidationState()
	}
	
	private ValidationState createExpectedValidationState() {
		return new ValidationState()
				.withUngrouped("Violation constraint: Ungrouped property")
				.withGrouped("stringValue", "Violation constraint: Grouped property")
	}

	interface ValidationGroup {}

	@AlwaysFailed(passedProperty="Ungrouped property", message="Violation constraint: {{passedProperty}}", groups=[ValidationGroup])
	class WrongBean {
		@AlwaysFailed(passedProperty="Grouped property", message="Violation constraint: {{passedProperty}}", groups=[ValidationGroup])
		private String stringValue;

		public WrongBean() {
			super();
			this.stringValue = "WRONG VALUE";
		}
	}
}
