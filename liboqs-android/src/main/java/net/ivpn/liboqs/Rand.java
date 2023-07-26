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

    /**
    * \brief Wrapper for OQS_API void OQS_randombytes_nist_kat_init(
    *                                   const uint8_t *entropy_input,
    *                                   const uint8_t *personalization_string,
    *                                   int security_strength);
    *
    * \param Entropy input seed, must be exactly 48 bytes long
    * \param Entropy seed length
    * \param Optional personalization string, which, if non-empty, must be at
    * least 48 byte[] long
    * \param personalization string length
    */
    private static native void randombytes_nist_kat_init(byte[] entropy_input,
                                            byte[] personalization_string,
                                            long personalization_string_len);

    public static void randombytes_nist_kat_init(byte[] entropy_input) {
        randombytes_nist_kat_init(entropy_input, null);
    }

    public static void randombytes_nist_kat_init(byte[] entropy_input,
                                            byte[] personalization_string) {
        if (entropy_input.length != 48) {
            throw new RuntimeException("The entropy source must be exactly 48 byte[] long");
        }
        if (personalization_string == null) {
            randombytes_nist_kat_init(entropy_input, null, 0);
            return;
        }
        if (personalization_string.length < 48) {
            throw new RuntimeException("The personalization string must be either empty or at least 48 byte[] long");
        }
        randombytes_nist_kat_init(entropy_input, personalization_string, personalization_string.length);
    }
}
