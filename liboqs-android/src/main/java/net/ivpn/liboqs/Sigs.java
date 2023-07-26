package net.ivpn.liboqs;

import java.util.ArrayList;

/**
 * \brief Signatures singleton class.
 * Singleton class, contains details about supported/enabled signature mechanisms
 */
public class Sigs {

    static {
        Common.loadNativeLibrary();
    }
    
    /**
     * The single Sigs class instance.
     */
    private static Sigs single_instance = null;

    private Sigs() {}

    /**
     * \brief Make sure that at most one instance is generated.
     * \return Singleton instance
     */
    public static synchronized Sigs get_instance() {
        if (single_instance == null) {
            single_instance = new Sigs();
        }
        return single_instance; 
    }
    
    /**
     * \brief Wrapper for OQS_API int OQS_SIG_alg_count(void);
     * \return Maximum number of supported signature algorithms
     */
    public static native int max_number_sigs();

    /**
     * \brief Wrapper for OQS_API int OQS_SIG_alg_is_enabled(const char *method_name);
     * Checks whether the signature algorithm alg_name is enabled
     * \param alg_name Cryptographic algorithm name
     * \return True if the signature algorithm is enabled, false otherwise
     */
    public static native boolean is_sig_enabled(String alg_name);

    /**
     * \brief Wrapper for OQS_API const char *OQS_SIG_alg_identifier(size_t i);
     * \param alg_id Cryptographic algorithm numerical id
     * \return signature algorithm name
     */
    public static native String get_sig_name(long alg_id);


    /**
     * \brief ArrayList of supported signature algorithms
     * \return ArrayList of supported signature algorithms
     */
    public static ArrayList<String> get_supported_sigs() {
        ArrayList<String> supported_Sigs = new ArrayList<>();
        for (int i = 0; i < max_number_sigs(); ++i) {
            supported_Sigs.add(get_sig_name(i));
        }
        return supported_Sigs;
    }
    
    /**
     * \brief Vector of enabled signature algorithms
     * \return Vector of enabled signature algorithms
     */
    public static ArrayList<String> get_enabled_sigs() {
        ArrayList<String> enabled_Sigs = new ArrayList<>();
        for (String elem : get_supported_sigs()) {
            if (is_sig_enabled(elem)) {
                enabled_Sigs.add(elem);
            }
        }
        return enabled_Sigs;
    }
    
    /**
     * \brief Checks whether the signature algorithm \a alg_name is supported
     * \param alg_name Cryptographic algorithm name
     * \return True if the signature algorithm is supported, false otherwise
     */
    public static boolean is_sig_supported(String alg_name) {
        ArrayList<String> supported_Sigs = get_supported_sigs();
        return supported_Sigs.contains(alg_name);
    }
    
}
