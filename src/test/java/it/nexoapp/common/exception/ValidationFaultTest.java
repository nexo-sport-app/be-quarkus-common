package it.nexoapp.common.exception;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Validation and routing errors reach the client as one {@code Fault} with the real HTTP status (FND-05).
 */
@QuarkusTest
@TestSecurity(user = "probe")
class ValidationFaultTest {

    /** Value that must never come back in an error body. */
    private static final String SECRET_NAME = "x";

    /** Value of the tax code that must never come back in an error body. */
    private static final String SECRET_TAX_CODE = "secret-tax-code";

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Posts a body to the probe resource.
     *
     * @param body the JSON body, may be empty
     * @return the response
     */
    private static Response post(final String body) {
        return given().contentType(ContentType.JSON).body(body).post("/probe-validation");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Every violation of every field comes back in one Fault, in a stable order, with the field names stripped of
     * the method and parameter nodes.
     */
    @Test
    void allViolationsInOneFaultSorted() {
        final String body = "{\"name\":\"\",\"taxCode\":\"x\"}";
        final Response first = post(body);
        first.then()
                .statusCode(400)
                .body("code", equalTo("nexo.validation"))
                .body("trace", notNullValue())
                .body("timestamp", notNullValue())
                .body("violations.size()", equalTo(4));
        final List<Map<String, String>> violations = first.jsonPath().getList("violations");
        assertEquals(
                List.of("age:NotNull", "name:NotBlank", "name:Size", "taxCode:Pattern"),
                violations.stream().map(v -> v.get("field") + ":" + v.get("code")).toList());
        violations.forEach(v -> assertFalse(v.get("message").isBlank()));
        assertEquals(violations, post(body).jsonPath().getList("violations"), "order is stable across calls");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Two violations on the same field are both returned.
     */
    @Test
    void sameFieldViolationsAreNotMerged() {
        final List<String> codes = post("{\"name\":\"\",\"taxCode\":\"12345678901\",\"age\":3}")
                .then().statusCode(400)
                .extract().jsonPath().getList("violations.findAll { it.field == 'name' }.code");
        assertEquals(List.of("NotBlank", "Size"), codes);
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * An empty body is a 400 Fault with a violation on the field {@code body}, never a 500.
     */
    @Test
    void emptyBodyIsAViolationOnBody() {
        final Response response = given().contentType(ContentType.JSON).post("/probe-validation");
        response.then()
                .statusCode(400)
                .body("code", equalTo("nexo.validation"))
                .body("violations.size()", equalTo(1))
                .body("violations[0].field", equalTo("body"))
                .body("violations[0].code", equalTo("NotNull"));
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * The error body contains the violated rules but never the submitted values.
     */
    @Test
    void violationsDoNotEchoRejectedValues() {
        final String raw = post("{\"name\":\"" + SECRET_NAME + "\",\"taxCode\":\"" + SECRET_TAX_CODE + "\"}")
                .then().statusCode(400).extract().asString();
        assertFalse(raw.contains(SECRET_TAX_CODE), "the rejected tax code must not be echoed");
        assertFalse(raw.contains("\"" + SECRET_NAME + "\""), "the rejected name must not be echoed");
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * A valid request passes untouched.
     */
    @Test
    void validRequestPasses() {
        post("{\"name\":\"Mario\",\"taxCode\":\"12345678901\",\"age\":30}").then().statusCode(200).body(equalTo("Mario"));
    }

}
