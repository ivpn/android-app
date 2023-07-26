package net.ivpn.liboqs;

/**
 * \brief Cryptographic scheme not supported
 */
public class MechanismNotSupportedError extends RuntimeException {
    
    public MechanismNotSupportedError(String alg_name) {
        super("\"" + alg_name + "\" is not supported by OQS");
    }
    
    public MechanismNotSupportedError(String alg_name, Throwable throwable) {
        super("\"" + alg_name + "\" is not supported by OQS", throwable);
    }
    
}
