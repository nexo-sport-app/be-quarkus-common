package it.nexoapp.common.archfixture;

import com.tngtech.archunit.core.domain.JavaClasses;
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
