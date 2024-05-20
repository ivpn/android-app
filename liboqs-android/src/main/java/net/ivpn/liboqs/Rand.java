package net.ivpn.liboqs;

/**
 * \brief Class containing RNG-related functions
 */
public class Rand {

    static {
        Common.loadNativeLibrary();
    }

    private Rand() {}

    /**
     * \brief Wrapper for OQS_API void OQS_randombytes(uint8_t *random_array,
     *                                                   size_t bytes_to_read);
     *
     * \param bytes_to_read The number of random byte[] to generate
     * \return random byte array
     */
    public static native byte[] randombytes(long bytes_to_read);

    /**
    * \brief Wrapper for OQS_API OQS_STATUS OQS_randombytes_switch_algorithm(
    *                                                           const char *);
    *
    * \param alg_name Algorithm name ["system", "NIST-KAT", "OpenSSL"]
    */
    private static native int randombytes_switch_algorithm_native(
                                                            String alg_name);

    public static void randombytes_switch_algorithm(String alg_name)
                                                    throws RuntimeException {
        int rv_ = randombytes_switch_algorithm_native(alg_name);
        if (rv_ != 0) {
            throw new RuntimeException("Cannot switch rand algorithm");
        }
    }
}
