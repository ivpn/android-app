#include <oqs/oqs.h>
#include "KeyEncapsulation.h"
#include "handle.h"

/*
 * Class:     org_openquantumsafe_KeyEncapsulation
 * Method:    get_new_KEM
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_net_ivpn_liboqs_KeyEncapsulation_create_1KEM_1new
  (JNIEnv *env, jobject obj, jstring jstr)
{
    // Create get a liboqs::OQS_KEM pointer
    const char *str_native = (*env)->GetStringUTFChars(env, jstr, 0);
    OQS_KEM *kem = OQS_KEM_new(str_native);
    (*env)->ReleaseStringUTFChars(env, jstr, str_native);
    // Stow the native OQS_KEM pointer in the Java handle.
    setHandle(env, obj, kem, "native_kem_handle_");
}

/*
 * Class:     org_openquantumsafe_KeyEncapsulation
 * Method:    free_KEM
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_net_ivpn_liboqs_KeyEncapsulation_free_1KEM
  (JNIEnv *env, jobject obj)
{
    OQS_KEM *kem = (OQS_KEM *) getHandle(env, obj, "native_kem_handle_");
    OQS_KEM_free(kem);
}

/*
 * Class:     org_openquantumsafe_KeyEncapsulation
 * Method:    get_KEM_details
 * Signature: ()Lnet/ivpn/liboqs/KeyEncapsulation/KeyEncapsulationDetails;
 */
JNIEXPORT jobject JNICALL Java_net_ivpn_liboqs_KeyEncapsulation_get_1KEM_1details
  (JNIEnv *env, jobject obj)
{
    jclass cls = (*env)->FindClass(env, "net/ivpn/liboqs/KeyEncapsulation$KeyEncapsulationDetails");
    if (cls == NULL) { fprintf(stderr, "\nCould not find class\n"); return NULL; }

    // Get the Method ID of the constructor
    jmethodID constructor_meth_id_ = (*env)->GetMethodID(env, cls, "<init>", "(Lnet/ivpn/liboqs/KeyEncapsulation;)V");
    if (NULL == constructor_meth_id_) { fprintf(stderr, "\nCould not initialize class\n"); return NULL; }

    // Call back constructor to allocate a new instance, with an int argument
    jobject _nativeKED = (*env)->NewObject(env, cls, constructor_meth_id_, obj);

    OQS_KEM *kem = (OQS_KEM *) getHandle(env, obj, "native_kem_handle_");

    // Copy fields from C struct to Java class
    // String method_name;
    jfieldID _method_name = (*env)->GetFieldID(env, cls, "method_name", "Ljava/lang/String;");
    jstring j_method_name = (*env)->NewStringUTF(env, kem->method_name);
    (*env)->SetObjectField(env, _nativeKED, _method_name, j_method_name);

    // String alg_version;
    jfieldID _alg_version = (*env)->GetFieldID(env, cls, "alg_version", "Ljava/lang/String;");
    jstring j_alg_version = (*env)->NewStringUTF(env, kem->alg_version);
    (*env)->SetObjectField(env, _nativeKED, _alg_version, j_alg_version);

    // byte claimed_nist_level;
    jfieldID _claimed_nist_level = (*env)->GetFieldID(env, cls, "claimed_nist_level", "B");
    (*env)->SetByteField(env, _nativeKED, _claimed_nist_level, (jbyte) kem->claimed_nist_level);

    // boolean ind_cca;
    jfieldID _ind_cca = (*env)->GetFieldID(env, cls, "ind_cca", "Z");
    (*env)->SetBooleanField(env, _nativeKED, _ind_cca, (jboolean) kem->ind_cca);

    // long length_public_key;
    jfieldID _length_public_key = (*env)->GetFieldID(env, cls, "length_public_key", "J");
    (*env)->SetLongField(env, _nativeKED, _length_public_key, (jlong) kem->length_public_key);

    // long length_secret_key;
    jfieldID _length_secret_key = (*env)->GetFieldID(env, cls, "length_secret_key", "J");
    (*env)->SetLongField(env, _nativeKED, _length_secret_key, (jlong) kem->length_secret_key);

    // long length_ciphertext;
    jfieldID _length_ciphertext = (*env)->GetFieldID(env, cls, "length_ciphertext", "J");
    (*env)->SetLongField(env, _nativeKED, _length_ciphertext, (jlong) kem->length_ciphertext);

    // long length_shared_secret;
    jfieldID _length_shared_secret = (*env)->GetFieldID(env, cls, "length_shared_secret", "J");
    (*env)->SetLongField(env, _nativeKED, _length_shared_secret, (jlong) kem->length_shared_secret);

    return _nativeKED;
}

/*
 * Class:     org_openquantumsafe_KeyEncapsulation
 * Method:    generate_keypair
 * Signature: ([B[B)I
 */
JNIEXPORT jint JNICALL Java_net_ivpn_liboqs_KeyEncapsulation_generate_1keypair
  (JNIEnv *env, jobject obj, jbyteArray jpublic_key, jbyteArray jsecret_key)
{
    jbyte *public_key_native = (*env)->GetByteArrayElements(env, jpublic_key, 0);
    jbyte *secret_key_native = (*env)->GetByteArrayElements(env, jsecret_key, 0);

    // Get pointer to KEM
    OQS_KEM *kem = (OQS_KEM *) getHandle(env, obj, "native_kem_handle_");

    // Invoke liboqs KEM keypair generation function
    OQS_STATUS rv_ = OQS_KEM_keypair(kem, (uint8_t*) public_key_native, (uint8_t*) secret_key_native);

    (*env)->ReleaseByteArrayElements(env, jpublic_key, public_key_native, JNI_COMMIT);
    (*env)->ReleaseByteArrayElements(env, jsecret_key, secret_key_native, JNI_COMMIT);
    return (rv_ == OQS_SUCCESS) ? 0 : -1;
}

/*
 * Class:     org_openquantumsafe_KeyEncapsulation
 * Method:    encap_secret
 * Signature: ([B[B[B)I
 */
JNIEXPORT jint JNICALL Java_net_ivpn_liboqs_KeyEncapsulation_encap_1secret
  (JNIEnv *env, jobject obj, jbyteArray jciphertext, jbyteArray jshared_secret, jbyteArray jpublic_key)
{
    // Convert public_key to jbyte array
    jbyte *public_key = (*env)->GetByteArrayElements(env, jpublic_key, 0);
    jbyte* ciphertext = (*env)->GetByteArrayElements(env, jciphertext, 0);
    jbyte* shared_secret = (*env)->GetByteArrayElements(env, jshared_secret, 0);

    // Get pointer to KEM and invoke liboqs encapsulate secret function
    OQS_KEM *kem = (OQS_KEM *) getHandle(env, obj, "native_kem_handle_");
    OQS_STATUS rv_ = OQS_KEM_encaps(kem, (uint8_t*) ciphertext, (uint8_t*) shared_secret, (uint8_t*) public_key);

    // Release C public_key
    (*env)->ReleaseByteArrayElements(env, jpublic_key, public_key, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, jciphertext, ciphertext, JNI_COMMIT);
    (*env)->ReleaseByteArrayElements(env, jshared_secret, shared_secret, JNI_COMMIT);
    return (rv_ == OQS_SUCCESS) ? 0 : -1;
}

/*
 * Class:     org_openquantumsafe_KeyEncapsulation
 * Method:    decap_secret
 * Signature: ([B[B[B)I
 */
JNIEXPORT jint JNICALL Java_net_ivpn_liboqs_KeyEncapsulation_decap_1secret
  (JNIEnv *env, jobject obj, jbyteArray jshared_secret, jbyteArray jciphertext, jbyteArray jsecret_key)
{
    jbyte *shared_secret_native = (*env)->GetByteArrayElements(env, jshared_secret, 0);
    jbyte *ciphertext_native = (*env)->GetByteArrayElements(env, jciphertext, 0);
    jbyte *secret_key_native = (*env)->GetByteArrayElements(env, jsecret_key, 0);

    OQS_KEM *kem = (OQS_KEM *) getHandle(env, obj, "native_kem_handle_");
    OQS_STATUS rv_ = OQS_KEM_decaps(kem, (uint8_t*) shared_secret_native, (uint8_t*) ciphertext_native, (uint8_t*) secret_key_native);

    // release memory
    (*env)->ReleaseByteArrayElements(env, jshared_secret, shared_secret_native, JNI_COMMIT);
    (*env)->ReleaseByteArrayElements(env, jciphertext, ciphertext_native, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, jsecret_key, secret_key_native, JNI_ABORT);
    return (rv_ == OQS_SUCCESS) ? 0 : -1;
}
