package io.bcs.testing.archive;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.container.ClassContainer;
import org.jboss.shrinkwrap.api.container.ManifestContainer;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenStrategyStage;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;

public abstract class ArchiveBuilder<A extends Archive<A>, B extends ArchiveBuilder<A, B>> {
	protected A archive;

	private ArchiveBuilder(Class<A> archiveType, String archiveName) {
		super();
		this.archive = ShrinkWrap.create(archiveType, archiveName);
	}

	public DependencyResolver resolveDependencies(String pomFile) {
		return new DependencyResolver(pomFile);
	}

	public B appendLibraries(Archive<?>... libraryArchives) {
		this.archive = this.appendLibrariesToArchive(libraryArchives);
		return self();
	}

	public abstract A build();

	@SuppressWarnings("unchecked")
	protected B self() {
		return (B) this;
	}

	protected abstract A appendLibrariesToArchive(Archive<?>... archives);

	public class DependencyResolver {
		private PomEquippedResolveStage resolver;
		private Set<String> profiles = new HashSet<>();
		private Set<ScopeType> scopes = new HashSet<ScopeType>();
		private Set<Archive<?>> resolved = new HashSet<Archive<?>>();

		public DependencyResolver(String pomFile) {
			super();
			this.resolver = loadPomFile(pomFile);
		}

		public DependencyResolver withProfile(String profile) {
			this.profiles.add(profile);
			return this;
		}

		public DependencyResolver withScopes(ScopeType... scopes) {
			this.scopes.addAll(Arrays.asList(scopes));
			return this;
		}

		public B resolveAll() {
			this.resolved.addAll(resolveDependencies(() -> resolver.importRuntimeAndTestDependencies().resolve()));
			return apply();
		}
		
		public DependencyResolver resolveDependency(String group, String artifact) {
			this.resolved.addAll(resolveDependencies(() -> resolver.resolve(String.format("%s:%s", group, artifact))));
			return this;
		}

		private Collection<Archive<?>> resolveDependencies(Supplier<MavenStrategyStage> resolveFunction) {
			try {
				return Arrays.stream(resolveFunction.get().withTransitivity().asFile())
						.map(archiveFile -> ShrinkWrap.create(ZipImporter.class, archiveFile.getName())
								.importFrom(archiveFile).as(GenericArchive.class))
						.collect(Collectors.toList());
			} catch (Exception e) {
				return Collections.emptyList();
			}
		}

		public B apply() {
			appendLibrariesToArchive(resolved.toArray(new Archive<?>[resolved.size()]));
			return self();
		}

		private PomEquippedResolveStage loadPomFile(String pomFileLocation) {
			PomEquippedResolveStage result = createDependencyLoader(pomFileLocation);
			if (!scopes.isEmpty()) {
				return result.importDependencies(scopes.toArray(new ScopeType[scopes.size()]));
			}
			return result;
		}

		private PomEquippedResolveStage createDependencyLoader(String pomFileLocation) {
			return Maven.resolver().loadPomFromFile(pomFileLocation, profiles.toArray(new String[profiles.size()]));
		}

	}

	public static abstract class ClassContainingArchiveBuilder<A extends Archive<A> & ClassContainer<A> & ManifestContainer<A>, B extends ArchiveBuilder<A, B>>
			extends ArchiveBuilder<A, B> {

		protected ClassContainingArchiveBuilder(Class<A> archiveType, String archiveName) {
			super(archiveType, archiveName);
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

		public B appendResource(String resourcePath) {
			this.archive = this.archive.addAsResource(resourcePath, resourcePath);
			return self();
		}

		public B appendResource(String resourcePath, String targetPath) {
			this.archive = this.archive.addAsResource(resourcePath, targetPath);
			return self();
		}
		
		public B appendManifestResource(String resourcePath, String targetPath) {
			this.archive = this.archive.addAsManifestResource(resourcePath, targetPath);
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
			return result.as(JavaArchive.class).addManifest();
		}

		@Override
		public JavaArchive build() {
			return archive.as(JavaArchive.class);
		}
	}

	public static final class WarArchiveBuilder extends ClassContainingArchiveBuilder<WebArchive, WarArchiveBuilder> {
		private WarArchiveBuilder(String archiveName) {
			super(WebArchive.class, archiveName);
		}

		public WarArchiveBuilder appendWebResource(String resourcePath, String targetPath) {
			this.archive = this.archive.addAsWebInfResource(resourcePath, targetPath);
			return self();
		}

		@Override
		protected WebArchive appendLibrariesToArchive(Archive<?>... archives) {
			return this.archive.addAsLibraries(archives);
		}

		@Override
		public WebArchive build() {
			return archive.as(WebArchive.class);
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

		@Override
		public EnterpriseArchive build() {
			return archive.as(EnterpriseArchive.class);
		}
	}

	public static final JarArchiveBuilder jar(String archiveName) {
		return new JarArchiveBuilder(archiveName);
	}

	public static final WarArchiveBuilder war(String archiveName) {
		return new WarArchiveBuilder(archiveName);
	}

	public static final EarArchiveBuilder ear(String archiveName) {
		return new EarArchiveBuilder(archiveName);
	}
}
