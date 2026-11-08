package it.nexoapp.common.security;

/**
 * Roles a member can hold inside one association. Roles are combinable: a member holds a set of them.
 * They live in the application database and are never read from token claims.
 */
public enum Role {

    /** Legal representative of the association. */
    PRESIDENT,

    /** Deputy of the president. */
    VICE_PRESIDENT,

    /** Keeps the registers and the member records. */
    SECRETARY,

    /** Member of the board (direttivo) without a specific office. */
    COUNCILLOR,

    /** Handles fees, payments and receipts. */
    TREASURER,

    /** Trains one or more teams. */
    COACH,

    /** Practising member. */
    ATHLETE,

    /** Legal guardian of a minor member. */
    GUARDIAN

}
