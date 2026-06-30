package it.nexoapp.common.exception;


/**
 * Thrown to indicate that an attempt was made to instantiate a class that is not meant to be instantiated.
 */
public class IllegalInstantiationException extends AbstractRuntimeException {

    /**
     * {@inheritDoc}
     */
    public IllegalInstantiationException() {
        super(DEAFULT_CODE_PREFIX + "illegal.instatiation", "This class cannot be instantiated");
    }

}