package io.bincloud.storage.integration

import javax.inject.Inject

import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.shrinkwrap.api.Archive
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.asset.EmptyAsset
import org.jboss.shrinkwrap.api.spec.JavaArchive
import org.junit.runner.RunWith

import spock.lang.Specification

@RunWith(ArquillianSputnik)
//@Ignore
class SampleComponentArquillianITSpec extends Specification {
	@Deployment
	static def Archive "create deployment"() {
		return ShrinkWrap.create(JavaArchive)
			.addClass(SampleComponent)
			.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
	}
	
	@Inject
	private SampleComponent component
	
	def "Scenario: The sample of working spock integration test on arquillian"() {
		expect:
			component.greeting == "Hello"
	}
}
