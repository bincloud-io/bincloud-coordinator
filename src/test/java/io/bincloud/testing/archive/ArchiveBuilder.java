package io.bincloud.testing.archive;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.container.ClassContainer;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenFormatStage;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public abstract class ArchiveBuilder<A extends Archive<A>, B extends ArchiveBuilder<A, B>> {
	protected A archive;

	private ArchiveBuilder(Class<A> archiveType, String archiveName) {
		super();
		this.archive = ShrinkWrap.create(archiveType, archiveName);
	}

	public DependencyResolver resolveDependencies(String pomFile) {
		return new DependencyResolver(Maven.resolver(), pomFile);
	}
	
	public B appendLibraries(Archive<?>... libraryArchives) {
		this.archive = this.appendLibrariesToArchive(libraryArchives);
		return self();
	}

	public A build() {
		return this.archive;
	}

	@SuppressWarnings("unchecked")
	protected B self() {
		return (B) this;
	}

	protected abstract A appendLibrariesToArchive(Archive<?>... archives);
	
	@RequiredArgsConstructor
	public class DependencyResolver {
		private final MavenResolverSystem resolverSystem;
		private final String pomFile;
		private Set<String> profiles = new HashSet<>();
		private Set<ScopeType> scopes = new HashSet<ScopeType>();
		
		public DependencyResolver withProfile(String profile) {
			this.profiles.add(profile);
			return this;
		}
		
		public DependencyResolver withScope(ScopeType scope) {
			this.scopes.add(scope);
			return this;
		}
		
		public B apply() {
			appendLibrariesToArchive(createDependenciesResolver().get().as(Archive.class));
			return self();
		}
		
		private Supplier<MavenFormatStage> createDependenciesResolver() {
			return () -> createScopedDependencyLoader().get()
					.resolve().withTransitivity();
		}
		
		private Supplier<PomEquippedResolveStage> createScopedDependencyLoader() {
			final Supplier<PomEquippedResolveStage> result = createDependencyLoader();
			if (!scopes.isEmpty()) {
				return () -> result.get().importDependencies(scopes.toArray(new ScopeType[scopes.size()]));
			}
			return result;
		}
		
		private Supplier<PomEquippedResolveStage> createDependencyLoader() {
			return () -> resolverSystem.loadPomFromFile(pomFile, profiles.toArray(new String[profiles.size()]));
		}
		
	}

	public static abstract class ClassContainingArchiveBuilder<A extends Archive<A> & ClassContainer<A>, B extends ArchiveBuilder<A, B>>
			extends ArchiveBuilder<A, B> {

		protected ClassContainingArchiveBuilder(Class<A> archiveType, String archiveName) {
			super(archiveType, archiveName);
			appendPackagesRecursively("io.bincloud.testing");
		}

		public B appendClasses(Class<?>... appendedClasses) {
			this.archive = this.archive.addClasses(appendedClasses);
			return self();
		}

		public B appendPackagesRecursively(String... appendedPackages) {
			this.archive = this.archive.addPackages(true, appendedPackages);
			return self();
		}

		public B appendPackageNonRecursively(String... appendedPackages) {
			this.archive = this.archive.addPackages(false, appendedPackages);
			return self();
		}
		
		public B copyMetaInfResource(String resourceName) {
			String resourcePath = String.format("META-INF/%s", resourceName);
			return copyResource(resourcePath);
		}
		
		public B copyResource(String resourcePath) {
			this.archive = this.archive.addAsResource(resourcePath, resourcePath);
			return self();
		}
	}

	public static final class JarArchiveBuilder extends ClassContainingArchiveBuilder<JavaArchive, JarArchiveBuilder> {
		private JarArchiveBuilder(String archiveName) {
			super(JavaArchive.class, archiveName);
		}
		
		@Override
		protected JavaArchive appendLibrariesToArchive(Archive<?>... archives) {
			JavaArchive result = this.archive;
			for (Archive<?> archive : archives) {
				result = result.merge(archive);
			}
			return result;
		}
	}

	public static final class WarArchiveBuilder extends ClassContainingArchiveBuilder<WebArchive, WarArchiveBuilder> {
		private WarArchiveBuilder(String archiveName) {
			super(WebArchive.class, archiveName);
		}
		
		public WarArchiveBuilder copyWebInfResource(String resourceName) {
			String resourcePath = String.format("WEB-INF/%s", resourceName);
			this.archive = this.archive.addAsResource(resourcePath, resourcePath);
			return self();
		}

		@Override
		protected WebArchive appendLibrariesToArchive(Archive<?>... archives) {
			return this.archive.addAsLibraries(archives);
		}
	}

	public static final class EarArchiveBuilder extends ArchiveBuilder<EnterpriseArchive, EarArchiveBuilder> {
		private EarArchiveBuilder(String archiveName) {
			super(EnterpriseArchive.class, archiveName);
		}

		public EarArchiveBuilder appendModule(Archive<?> moduleArchive) {
			this.archive = this.archive.addAsModule(moduleArchive);
			return self();
		}

		@Override
		protected EnterpriseArchive appendLibrariesToArchive(Archive<?>... archives) {
			return this.archive.addAsLibraries(archives);
		}
	}

	public static final JarArchiveBuilder jar(@NonNull String archiveName) {
		return new JarArchiveBuilder(archiveName);
	}

	public static final WarArchiveBuilder war(@NonNull String archiveName) {
		return new WarArchiveBuilder(archiveName);
	}

	public static final EarArchiveBuilder ear(@NonNull String archiveName) {
		return new EarArchiveBuilder(archiveName);
	}
}
