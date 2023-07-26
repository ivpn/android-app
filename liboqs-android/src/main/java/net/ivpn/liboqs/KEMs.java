package net.ivpn.liboqs;

import java.util.ArrayList;

/**
 * \brief Key Encapsulation Mechanisms Singleton class.
 * Contains details about supported/enabled key exchange mechanisms (KEMs)
 */
public class KEMs {
    
    static {
        Common.loadNativeLibrary();
    }

    /**
     * The single KEMs class instance.
     */
    private static KEMs single_instance = null;

    private KEMs() {}

    /**
     * \brief Make sure that at most one instance is generated.
     * \return Singleton instance
     */
    public static synchronized KEMs get_instance() {
        if (single_instance == null) {
            single_instance = new KEMs();
        }
        return single_instance; 
    }
    
    /**
     * \brief Wrapper for OQS_API int OQS_KEM_alg_count(void);
     * \return Maximum number of supported KEM algorithms
     */
    public static native int max_number_KEMs();

    /**
     * \brief Wrapper for OQS_API int OQS_KEM_alg_is_enabled(const char *method_name);
     * Checks whether the KEM algorithm alg_name is enabled
     * \param alg_name Cryptographic algorithm name
     * \return True if the KEM algorithm is enabled, false otherwise
     */
    public static native boolean is_KEM_enabled(String alg_name);

    /**
     * \brief Wrapper for OQS_API const char *OQS_KEM_alg_identifier(size_t i);
     * \param alg_id Cryptographic algorithm numerical id
     * \return KEM algorithm name
     */
    public static native String get_KEM_name(long alg_id);


    /**
     * \brief ArrayList of supported KEM algorithms
     * \return ArrayList of supported KEM algorithms
     */
    public static ArrayList<String> get_supported_KEMs() {
        ArrayList<String> supported_KEMs = new ArrayList<>();
        for (int i = 0; i < max_number_KEMs(); ++i) {
            supported_KEMs.add(get_KEM_name(i));
        }
        return supported_KEMs;
    }
    
    /**
     * \brief Vector of enabled KEM algorithms
     * \return Vector of enabled KEM algorithms
     */
    public static ArrayList<String> get_enabled_KEMs() {
        ArrayList<String> enabled_KEMs = new ArrayList<>();
        for (String elem : get_supported_KEMs()) {
            if (is_KEM_enabled(elem)) {
                enabled_KEMs.add(elem);
            }
        }
        return enabled_KEMs;
    }
    
    /**
     * \brief Checks whether the KEM algorithm \a alg_name is supported
     * \param alg_name Cryptographic algorithm name
     * \return True if the KEM algorithm is supported, false otherwise
     */
    public static boolean is_KEM_supported(String alg_name) {
        ArrayList<String> supported_KEMs = get_supported_KEMs();
        return supported_KEMs.contains(alg_name);
    }
    
}
