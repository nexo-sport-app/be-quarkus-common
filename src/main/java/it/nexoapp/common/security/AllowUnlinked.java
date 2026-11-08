package it.nexoapp.common.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a REST resource class or method that an authenticated identity may call even when it is not linked to a
 * membership yet, or has not selected an association (D-04). Without it every endpoint answers {@code 403
 * IDENTITY_NOT_LINKED} to such callers; the exemption exists for the endpoints the frontend needs to route a new
 * user to onboarding, for example "who am I" and "which associations do I belong to".
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface AllowUnlinked {

}
