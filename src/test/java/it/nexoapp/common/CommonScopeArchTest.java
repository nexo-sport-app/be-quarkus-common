package it.nexoapp.common;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Guards the scope of the common library: cross-cutting code only. Domain logic with entities,
 * repositories or caching (storage, transcoding) belongs to the owning service.
 * <p>
 * These rules are common's own; the shared architecture rules for services are not applied here,
 * because common's cross-cutting utilities are public API by design.
 */
@AnalyzeClasses(packages = "it.nexoapp.common", importOptions = ImportOption.DoNotIncludeTests.class)
class CommonScopeArchTest {

    /** Common holds no infrastructure (storage, transcoding) package. */
    @ArchTest
    static final ArchRule noInfraPackage = noClasses()
            .should().resideInAPackage("..common.infra..")
            .because("infrastructure with domain logic belongs to the owning service");

    // ------------------------------------------------------------------------------------------

    /** Common declares no JPA entity apart from mapped superclasses. */
    @ArchTest
    static final ArchRule noEntity = noClasses()
            .should().beAnnotatedWith("jakarta.persistence.Entity")
            .because("entities belong to the owning service, common only offers base types");

    // ------------------------------------------------------------------------------------------

    /** Common does not depend on the Quarkus cache. */
    @ArchTest
    static final ArchRule noQuarkusCache = noClasses()
            .should().dependOnClassesThat().resideInAPackage("io.quarkus.cache..")
            .because("caching is configured and owned by each service");

}
