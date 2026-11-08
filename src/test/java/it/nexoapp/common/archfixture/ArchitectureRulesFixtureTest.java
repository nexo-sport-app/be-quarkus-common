package it.nexoapp.common.archfixture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import it.nexoapp.common.testing.ArchitectureRules;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Proves the shared {@link ArchitectureRules}: every rule reports a violation on the deliberately bad layout in
 * {@code archfixture.bad} and none on the clean target layout in {@code archfixture.good}.
 * <p>
 * Plain JUnit, no Quarkus: the fixtures carry no bean-defining, JAX-RS or JPA annotation, so the common test
 * application does not pick them up.
 */
class ArchitectureRulesFixtureTest {

    /** Number of rules the shared set declares; a rule removed by accident fails this test. */
    private static final int RULE_COUNT = 8;

    // ------------------------------------------------------------------------------------------

    /** Every rule bites on the bad fixture. */
    @Test
    void everyRuleReportsAViolationOnTheBadFixture() {
        final JavaClasses bad = new ClassFileImporter().importPackages("it.nexoapp.common.archfixture.bad");
        assertEquals(4, bad.size(), "bad fixture classes: " + bad);
        final List<Executable> assertions = new ArrayList<>();
        rules().forEach((name, rule) -> assertions.add(() -> assertTrue(
                rule.evaluate(bad).hasViolation(),
                "rule " + name + " must report a violation on the bad fixture")));
        assertAll(assertions);
    }

    // ------------------------------------------------------------------------------------------

    /** No rule flags the clean fixture. */
    @Test
    void noRuleReportsAViolationOnTheGoodFixture() {
        final JavaClasses good = new ClassFileImporter().importPackages("it.nexoapp.common.archfixture.good");
        assertEquals(5, good.size(), "good fixture classes: " + good);
        final List<Executable> assertions = new ArrayList<>();
        rules().forEach((name, rule) -> assertions.add(() -> assertFalse(
                rule.evaluate(good).hasViolation(),
                "rule " + name + " must not report a violation on the good fixture: "
                        + rule.evaluate(good).getFailureReport())));
        assertAll(assertions);
    }

    // ------------------------------------------------------------------------------------------

    /** A public MapStruct-generated mapper implementation is allowed by every rule. */
    @Test
    void generatedMapStructImplementationIsAllowed() {
        final JavaClasses mapper = new ClassFileImporter().importPackages("it.nexoapp.common.archfixture.mapperimpl");
        assertTrue(mapper.stream().anyMatch(c -> c.getSimpleName().equals("GoodMapperImpl") && c.getModifiers().contains(JavaModifier.PUBLIC)),
                "the processor must have generated a public GoodMapperImpl: " + mapper);
        final List<Executable> assertions = new ArrayList<>();
        rules().forEach((name, rule) -> assertions.add(() -> assertFalse(
                rule.evaluate(mapper).hasViolation(),
                "rule " + name + " must not report a violation on a generated mapper: "
                        + rule.evaluate(mapper).getFailureReport())));
        assertAll(assertions);
    }

    // ------------------------------------------------------------------------------------------

    /** A public implementation of an interface that is not a MapStruct mapper is still rejected. */
    @Test
    void publicImplementationOfPlainInterfaceStillFails() {
        final JavaClasses plain = new ClassFileImporter().importPackages("it.nexoapp.common.archfixture.plainimpl");
        assertEquals(2, plain.size(), "plain fixture classes: " + plain);
        assertTrue(ArchitectureRules.IMPLEMENTATIONS_ARE_PACKAGE_PRIVATE.evaluate(plain).hasViolation(),
                "a public Impl of a non-mapper interface must fail the package-private rule");
        assertTrue(ArchitectureRules.ONLY_ALLOWED_KINDS_ARE_PUBLIC.evaluate(plain).hasViolation(),
                "a public Impl of a non-mapper interface must fail the public-kinds rule");
    }

    // ------------------------------------------------------------------------------------------

    /**
     * Collects every {@code ArchRule} field of {@link ArchitectureRules} by reflection, so a new rule is covered
     * automatically.
     *
     * @return the rules by field name
     */
    private static Map<String, ArchRule> rules() {
        final Map<String, ArchRule> rules = new LinkedHashMap<>();
        for (final Field field : ArchitectureRules.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && ArchRule.class.isAssignableFrom(field.getType())) {
                try {
                    rules.put(field.getName(), (ArchRule) field.get(null));
                } catch (final IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        assertEquals(RULE_COUNT, rules.size(), "rules: " + rules.keySet());
        return rules;
    }

}
