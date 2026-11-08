package it.nexoapp.common.testing;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.junit.ArchTest;

import java.util.List;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Shared ArchUnit rule set of the Nexo platform (FND-09, D-07), published in the test-jar of the common library.
 * <p>
 * Scope: the rules are applied by each service and library repository to its OWN root package only
 * ({@code it.nexoapp.env}, {@code it.nexoapp.usr}, {@code it.nexoapp.doc}, ..., {@code it.nexoapp.messaging}) through
 * {@code @AnalyzeClasses(packages = "<root>", importOptions = DoNotIncludeTests)} and
 * {@code @ArchTest static final ArchTests RULES = ArchTests.in(ArchitectureRules.class)}. They are NOT applied to
 * {@code it.nexoapp.common}: the public cross-cutting utilities of common ({@code TenantContext},
 * {@code ExecutionScope}, {@code MDCUtils}, {@code HeaderAllowlist}, ...) are public API by design, and the
 * boundaries of common are guarded by its own {@code CommonScopeArchTest}.
 * <p>
 * Every rule allows an empty subject set, so a repository without repositories or resources (a skeleton service)
 * does not fail. Each rule is proven to bite on a deliberately bad layout by {@code ArchitectureRulesFixtureTest}
 * (that fixture is not part of the published jar).
 */
public final class ArchitectureRules {

    /* - names and packages - */

    /** Simple-name suffix of REST resources. */
    private static final String RESOURCE_SUFFIX = "Resource";

    /** Simple-name suffix of repositories. */
    private static final String REPOSITORY_SUFFIX = "Repository";

    /** Simple-name suffix of service implementations. */
    private static final String IMPLEMENTATION_SUFFIX = "Impl";

    /** Packages in which a repository may reside. */
    private static final String[] REPOSITORY_PACKAGES = {"..service..", "..infra..", "..outbox..", "..inbox..", "..access.."};

    /** Packages in which native SQL is allowed (outbox and inbox poll with FOR UPDATE SKIP LOCKED). */
    private static final String[] NATIVE_SQL_PACKAGES = {"..outbox..", "..inbox.."};

    /** Types whose native-query factories bypass the Hibernate tenant filter. */
    private static final List<String> NATIVE_QUERY_OWNERS = List.of(
            "jakarta.persistence.EntityManager",
            "org.hibernate.Session",
            "org.hibernate.SharedSessionContract");

    // ------------------------------------------------------------------------------------------

    /* - rules - */

    /** Resources talk to services, never to repositories (resource to service to repository hierarchy). */
    @ArchTest
    public static final ArchRule RESOURCES_DO_NOT_USE_REPOSITORIES = noClasses()
            .that().haveSimpleNameEndingWith(RESOURCE_SUFFIX)
            .should().dependOnClassesThat().haveSimpleNameEndingWith(REPOSITORY_SUFFIX)
            .because("calls follow the hierarchy resource, service, repository")
            .allowEmptyShould(true);

    // ------------------------------------------------------------------------------------------

    /**
     * Implementations are package-private so they cannot be used directly from outside their package.
     * <p>
     * The only exemption is a MapStruct-generated mapper implementation, which the processor always emits as public.
     */
    @ArchTest
    public static final ArchRule IMPLEMENTATIONS_ARE_PACKAGE_PRIVATE = classes()
            .that().haveSimpleNameEndingWith(IMPLEMENTATION_SUFFIX)
            .and(DescribedPredicate.not(mapStructGeneratedImplementation()))
            .should().notBePublic()
            .andShould().notBeProtected()
            .because("only interfaces are public, implementations are package-private")
            .allowEmptyShould(true);

    // ------------------------------------------------------------------------------------------

    /** Repositories are package-private. */
    @ArchTest
    public static final ArchRule REPOSITORIES_ARE_PACKAGE_PRIVATE = classes()
            .that().haveSimpleNameEndingWith(REPOSITORY_SUFFIX)
            .should().notBePublic()
            .because("repositories are only reachable from their own package")
            .allowEmptyShould(true);

    // ------------------------------------------------------------------------------------------

    /** Repositories live in a service, infra, outbox, inbox or access package. */
    @ArchTest
    public static final ArchRule REPOSITORIES_RESIDE_IN_SERVICE_LAYER = classes()
            .that().haveSimpleNameEndingWith(REPOSITORY_SUFFIX)
            .should().resideInAnyPackage(REPOSITORY_PACKAGES)
            .because("repositories belong to the service layer of their own slice")
            .allowEmptyShould(true);

    // ------------------------------------------------------------------------------------------

    /**
     * Public top-level classes are interfaces, records, enums, annotations, JPA entity types, JAX-RS resources,
     * throwables, final utility classes ({@code *Utils}, {@code *Constant}, {@code *Constants}) or MapStruct-generated
     * mapper implementations ({@code *Impl} classes implementing an interface annotated with {@code @Mapper}).
     */
    @ArchTest
    public static final ArchRule ONLY_ALLOWED_KINDS_ARE_PUBLIC = classes()
            .that().arePublic()
            .and().areTopLevelClasses()
            .should(ArchCondition.from(allowedPublicKind()))
            .because("only interfaces and a few well-known kinds are public")
            .allowEmptyShould(true);

    // ------------------------------------------------------------------------------------------

    /** DTOs do not depend on the service layer. */
    @ArchTest
    public static final ArchRule DTOS_DO_NOT_DEPEND_ON_SERVICES = noClasses()
            .that().resideInAPackage("..dto..")
            .should().dependOnClassesThat().resideInAPackage("..service..")
            .because("DTOs are plain data carriers and must not know the service layer")
            .allowEmptyShould(true);

    // ------------------------------------------------------------------------------------------

    /** Repositories do not depend on DTOs, resources or services (proxy for no business logic in repositories). */
    @ArchTest
    public static final ArchRule REPOSITORIES_DO_NOT_DEPEND_ON_UPPER_LAYERS = noClasses()
            .that().haveSimpleNameEndingWith(REPOSITORY_SUFFIX)
            .should().dependOnClassesThat(upperLayer())
            .because("no business logic in repository classes")
            .allowEmptyShould(true);

    // ------------------------------------------------------------------------------------------

    /** Native SQL is only used by outbox and inbox code; it bypasses the Hibernate tenant filter. */
    @ArchTest
    public static final ArchRule NATIVE_SQL_ONLY_IN_OUTBOX_INBOX = noClasses()
            .that().resideOutsideOfPackages(NATIVE_SQL_PACKAGES)
            .should().callMethodWhere(nativeQueryCall())
            .because("native SQL bypasses the tenant filter and is only allowed with an explicit tenant bind")
            .allowEmptyShould(true);

    // ------------------------------------------------------------------------------------------

    /**
     * Not instantiable: a holder of constants.
     */
    private ArchitectureRules() {
    }

    // ------------------------------------------------------------------------------------------

    /**
     * Describes the kinds of top-level class that may be public.
     *
     * @return the predicate matching an allowed public kind
     */
    private static DescribedPredicate<JavaClass> allowedPublicKind() {
        final DescribedPredicate<JavaClass> structural = DescribedPredicate.describe(
                "be an interface, record, enum or annotation",
                c -> c.isInterface() || c.isRecord() || c.isEnum() || c.isAnnotation());
        final DescribedPredicate<JavaClass> jpa = annotatedWith("jakarta.persistence.Entity")
                .or(annotatedWith("jakarta.persistence.MappedSuperclass"))
                .or(annotatedWith("jakarta.persistence.Embeddable"));
        final DescribedPredicate<JavaClass> resource = annotatedWith("jakarta.ws.rs.Path");
        final DescribedPredicate<JavaClass> throwable = JavaClass.Predicates.assignableTo(Throwable.class);
        final DescribedPredicate<JavaClass> utility = DescribedPredicate.describe(
                "be a final class named *Utils, *Constant or *Constants",
                c -> c.getModifiers().contains(JavaModifier.FINAL)
                        && (c.getSimpleName().endsWith("Utils")
                        || c.getSimpleName().endsWith("Constant")
                        || c.getSimpleName().endsWith("Constants")));
        return structural.or(jpa).or(resource).or(throwable).or(utility).or(mapStructGeneratedImplementation());
    }

    // ------------------------------------------------------------------------------------------

    /**
     * Describes a MapStruct-generated mapper implementation: a class named {@code *Impl} that implements an interface
     * annotated with {@code org.mapstruct.Mapper}. The annotation is matched by its qualified name (it has CLASS
     * retention and ArchUnit reads it from the bytecode), so common needs no MapStruct dependency for it.
     *
     * @return the predicate matching generated mapper implementations
     */
    private static DescribedPredicate<JavaClass> mapStructGeneratedImplementation() {
        return DescribedPredicate.describe(
                "be a MapStruct-generated implementation of an interface annotated with @org.mapstruct.Mapper",
                c -> c.getSimpleName().endsWith(IMPLEMENTATION_SUFFIX)
                        && c.getAllRawInterfaces().stream().anyMatch(i -> i.isAnnotatedWith("org.mapstruct.Mapper")));
    }

    // ------------------------------------------------------------------------------------------

    /**
     * Describes a class carrying the given annotation.
     *
     * @param annotationName the fully qualified annotation name
     * @return the predicate matching classes annotated with it
     */
    private static DescribedPredicate<JavaClass> annotatedWith(final String annotationName) {
        return DescribedPredicate.describe("be annotated with @" + annotationName, c -> c.isAnnotatedWith(annotationName));
    }

    // ------------------------------------------------------------------------------------------

    /**
     * Describes the classes a repository must not depend on.
     *
     * @return the predicate matching DTOs, resources, services and service implementations
     */
    private static DescribedPredicate<JavaClass> upperLayer() {
        return JavaClass.Predicates.resideInAPackage("..dto..")
                .or(JavaClass.Predicates.simpleNameEndingWith(RESOURCE_SUFFIX))
                .or(JavaClass.Predicates.simpleNameEndingWith("Service"))
                .or(JavaClass.Predicates.simpleNameEndingWith("ServiceImpl"));
    }

    // ------------------------------------------------------------------------------------------

    /**
     * Describes a call of a {@code createNative*} factory on an entity manager or Hibernate session.
     *
     * @return the predicate matching such a call
     */
    private static DescribedPredicate<JavaMethodCall> nativeQueryCall() {
        return DescribedPredicate.describe(
                "a createNative* method on EntityManager, Session or SharedSessionContract",
                call -> call.getTarget().getName().startsWith("createNative")
                        && NATIVE_QUERY_OWNERS.stream().anyMatch(call.getTargetOwner()::isAssignableTo));
    }

}
