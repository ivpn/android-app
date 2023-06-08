package net.ivpn.liboqs;

/**
 * \brief Cryptographic scheme not enabled
 */
public class MechanismNotEnabledError extends RuntimeException {
    
    public MechanismNotEnabledError(String alg_name) {
        super("\"" + alg_name + "\" is not enabled by OQS");
    }
    
    public MechanismNotEnabledError(String alg_name, Throwable throwable) {
        super("\"" + alg_name + "\" is not enabled by OQS", throwable);
    }
    
}
