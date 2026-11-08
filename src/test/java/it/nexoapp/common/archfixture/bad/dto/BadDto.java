package it.nexoapp.common.archfixture.bad.dto;

import it.nexoapp.common.archfixture.bad.service.BadServiceImpl;

/**
 * Deliberately bad fixture: a DTO depending on a service class.
 *
 * @param service a service implementation a DTO must not depend on
 */
public record BadDto(BadServiceImpl service) {

}
