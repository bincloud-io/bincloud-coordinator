package io.bce.validation

import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.COMPILE
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.RUNTIME
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.TEST

import javax.inject.Inject
import javax.validation.Validator

import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.shrinkwrap.api.Archive
import org.junit.runner.RunWith

import io.bce.domain.errors.ApplicationException
import io.bce.text.TextProcessor
import io.bce.text.TextTemplate
import io.bce.text.TextTemplates
import io.bce.text.transformers.TemplateCompilingTransformer
import io.bce.text.transformers.compilers.HandlebarsTemplateCompiler
import io.bce.validation.JSRBeanValidationService
import io.bce.validation.ValidationService
import io.bce.validation.ValidationState
import io.bcs.common.domain.model.logging.Loggers
import io.bcs.testing.archive.ArchiveBuilder
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
				.appendPackagesRecursively(TextTemplate.getPackage().getName())
				.appendPackagesRecursively(ValidationService.getPackage().getName())
				.appendClasses(AlwaysFailed, AlwaysFailedValidator, JSRBeanValidationService, HandlebarsTemplateCompiler)
				.appendManifestResource("META-INF/beans.xml", "beans.xml")
				.build()
	}

	private final TextProcessor messageProcessor = TextProcessor.create().withTransformer(new TemplateCompilingTransformer(new HandlebarsTemplateCompiler()))

	@Inject
	private Validator validator;

	def "Scenario: validate object with validation framework"() {
		given: "The wrong bean"
		WrongBean wrongBean = new WrongBean()

		and: "The validation service"
		ValidationService validationService = new JSRBeanValidationService(validator, messageProcessor)

		when: "The validation has been requested"
		ValidationState validationState = validationService.validate(wrongBean)

		then: "The result validation state will contain all expected constraints"
		validationState == createExpectedValidationState()
	}

	private ValidationState createExpectedValidationState() {
		return new ValidationState()
				.withUngrouped("Violation constraint: Ungrouped property")
				.withGrouped("stringValue", "Violation constraint: Grouped property")
	}

	@AlwaysFailed(passedProperty="Ungrouped property", message="Violation constraint: {{passedProperty}}")
	class WrongBean {
		@AlwaysFailed(passedProperty="Grouped property", message="Violation constraint: {{passedProperty}}")
		private String stringValue;

		public WrongBean() {
			super();
			this.stringValue = "WRONG VALUE";
		}
	}
}
