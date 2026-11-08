package it.nexoapp.common.archfixture.bad.service;

import it.nexoapp.common.archfixture.bad.BadRepository;

/**
 * Deliberately bad fixture: a public implementation class (also a disallowed public kind).
 */
public class BadServiceImpl {

}

/**
 * Deliberately bad fixture: a resource depending directly on a repository.
 */
class BadResource {

    /* - dependencies - */

    /** A repository a resource must not depend on. */
    private BadRepository repository;

}
