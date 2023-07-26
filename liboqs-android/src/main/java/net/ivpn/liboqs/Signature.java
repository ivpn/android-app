package net.ivpn.liboqs;

import java.util.Arrays;

/**
 * \brief Signature Mechanisms
 */
public class Signature {

    /**
     * \brief Signature algorithm details
     */
    class SignatureDetails {

        String method_name;
        String alg_version;
        byte claimed_nist_level;
        boolean is_euf_cma;
        long length_public_key;
        long length_secret_key;
        long max_length_signature;

        /**
         * \brief Print Signature algorithm details
         */
        void printSignature() {
            System.out.println("Signature Details:" +
                "\n  Name: " + this.method_name +
                "\n  Version: " + this.alg_version +
                "\n  Claimed NIST level: " + this.claimed_nist_level +
                "\n  Is IND-CCA: " + this.is_euf_cma +
                "\n  Length public key (bytes): " + this.length_public_key +
                "\n  Length secret key (bytes): " + this.length_secret_key +
                "\n  Maximum length signature (bytes): " + this.max_length_signature
            );
        }

    }

    /**
     * Keep native pointers for Java to remember which C memory it is managing.
     */
    private long native_sig_handle_;

    private byte[] public_key_;
    private byte[] secret_key_;

    /**
     * Private object that has the signature details.
     */
    private SignatureDetails alg_details_;

    /**
     * \brief Constructs an instance of oqs::Signature
     * \param alg_name Cryptographic algorithm method_name
     */
    public Signature(String alg_name) throws RuntimeException {
        this(alg_name, null);
    }

    /**
     * \brief Constructs an instance of oqs::Signature
     * \param alg_name Cryptographic algorithm method_name
     * \param secret_key Secret key
     */
    public Signature(String alg_name, byte[] secret_key)
                                                    throws RuntimeException {
        // signature not enabled
        if (!Sigs.is_sig_enabled(alg_name)) {
            // perhaps it's supported
            if (Sigs.is_sig_supported(alg_name)) {
                throw new MechanismNotEnabledError(alg_name);
            } else {
                throw new MechanismNotSupportedError(alg_name);
            }
        }
        create_sig_new(alg_name);
        alg_details_ = get_sig_details();
        // initialize keys
        if (secret_key != null) {
            this.secret_key_ = Arrays.copyOf(secret_key, secret_key.length);
        } else {
            this.secret_key_ = new byte[(int) alg_details_.length_secret_key];
        }
        this.public_key_ = new byte[(int) alg_details_.length_public_key];
    }

    /**
     * \brief Wrapper for OQS_API OQS_SIG *OQS_SIG_new(const char *method_name);
     * Calls OQS_SIG_new and stores return value to native_sig_handle_.
     */
    public native void create_sig_new(String method_name);

    /**
     * \brief Wrapper for OQS_API void OQS_SIG_free(OQS_SIG *sig);
     * Frees an OQS_SIG object that was constructed by OQS_SIG_new.
     */
    private native void free_sig();

    /**
     * \brief Initialize and fill a SignatureDetails object from the native
     * C struct pointed by native_sig_handle_.
     */
    private native SignatureDetails get_sig_details();

    /**
     * \brief Wrapper for OQS_API OQS_STATUS OQS_SIG_keypair(const OQS_SIG *sig,
     *                              uint8_t *public_key, uint8_t *secret_key);
     * \param Public key
     * \param Secret key
     * \return Status
     */
    private native int generate_keypair(byte[] public_key, byte[] secret_key);

    /**
     * \brief Wrapper for OQS_API OQS_STATUS OQS_SIG_sign(const OQS_SIG *sig,
     *                                              uint8_t *signature,
     *                                              size_t *signature_len,
     *                                              const uint8_t *message,
     *                                              size_t message_len,
     *                                              const uint8_t *secret_key);
     * \param signature
     * \param signature_len_ret
     * \param message
     * \param message_len
     * \param secret_key
     * \return Status
     */
    private native int sign(byte[] signature, Mutable<Long> signature_len_ret,
                        byte[] message, long message_len, byte[] secret_key);

    /**
     * \brief Wrapper for OQS_API OQS_STATUS OQS_SIG_verify(const OQS_SIG *sig,
     *                                              const uint8_t *message,
     *                                              size_t message_len,
     *                                              const uint8_t *signature,
     *                                              size_t signature_len,
     *                                              const uint8_t *public_key);
     * \param message
     * \param message_len
     * \param signature
     * \param signature_len
     * \param public_key
     * \return True if the signature is valid, false otherwise
     */
    private native boolean verify(byte[] message, long message_len,
                                byte[] signature, long signature_len,
                                byte[] public_key);

    /**
     * \brief Invoke native free_sig
     */
    public void dispose_sig() {
        Common.wipe(this.secret_key_);
        free_sig();
    }

    /**
     * \brief Invoke native generate_keypair method using the PK and SK lengths
     * from alg_details_. Check return value and if != 0 throw RuntimeException.
     */
    public byte[] generate_keypair() throws RuntimeException {
        int rv_ = generate_keypair(this.public_key_, this.secret_key_);
        if (rv_ != 0) throw new RuntimeException("Cannot generate keypair");
        return this.public_key_;
    }

    /**
     * \brief Return public key
     */
    public byte[] export_public_key() {
        return this.public_key_;
    }

    /**
    * \brief Return secret key
     */
    public byte[] export_secret_key() {
        return this.secret_key_;
    }

    /**
     * \brief Invoke native sign method
     * \param message
     * \return signature
     */
    public byte[] sign(byte[] message) throws RuntimeException {
        if (this.secret_key_.length != alg_details_.length_secret_key) {
            throw new RuntimeException("Incorrect secret key length, " +
                                    "make sure you specify one in the " +
                                    "constructor or run generate_keypair()");
        }
        byte[] signature = new byte[(int) alg_details_.max_length_signature];
        Mutable<Long> signature_len_ret = new Mutable<>();
        int rv_= sign(signature, signature_len_ret,
                        message, message.length, this.secret_key_);
        long actual_signature_len = signature_len_ret.value;
        byte[] actual_signature = new byte[(int) actual_signature_len];
        System.arraycopy(signature, 0,
                            actual_signature, 0, (int) actual_signature_len);
        if (rv_ != 0) throw new RuntimeException("Cannot sign message");
        return actual_signature;
    }

    /**
     * \brief Invoke native verify method
     * \param message
     * \param signature
     * \param public_key
     * \return True if the signature is valid, false otherwise
     */
    public boolean verify(byte[] message, byte[] signature, byte[] public_key)
                                                    throws RuntimeException {
        if (public_key.length != alg_details_.length_public_key) {
            throw new RuntimeException("Incorrect public key length");
        }
        if (signature.length > alg_details_.max_length_signature) {
            throw new RuntimeException("Incorrect signature length");
        }

        return verify(message, message.length, signature, signature.length, public_key);
    }

    /**
     * \brief Print Signature. If a SignatureDetails object is not
     * initialized, initialize it and fill it using native C code.
     */
    public void print_signature() {
        if (alg_details_ == null) {
            alg_details_ = get_sig_details();
        }
        System.out.println("Signature: " + alg_details_.method_name);
    }

    /**
     * \brief print signature algorithm details. If a SignatureDetails object is
     * not initialized, initialize it and fill it using native C code.
     */
    public void print_details() {
        if (alg_details_ == null) {
            alg_details_ = get_sig_details();
        }
        alg_details_.printSignature();
    }

    public static class Mutable<T> {
        T value;
        public void setValue(T t) { this.value = t; }
        public T getValue() { return this.value; }
    }

}
