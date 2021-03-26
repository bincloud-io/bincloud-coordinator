package io.bincloud.storage.integration

import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.shrinkwrap.api.Archive
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.asset.EmptyAsset
import org.jboss.shrinkwrap.api.spec.JavaArchive
import org.junit.runner.RunWith

import io.bincloud.storage.domain.model.file.FileRepository
import spock.lang.Specification

@RunWith(ArquillianSputnik)
class FileRepositorySpec extends Specification {
	public static Archive "create deployment"() {
		return ShrinkWrap.create(JavaArchive)
			.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
			.addClass(FileRepository);
	}
}
