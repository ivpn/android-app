#include <oqs/oqs.h>
#include "Signature.h"
#include "handle.h"

/*
 * Class:     org_openquantumsafe_Signature
 * Method:    create_sig_new
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_net_ivpn_liboqs_Signature_create_1sig_1new
  (JNIEnv *env, jobject obj, jstring jstr)
{
    // Create get a liboqs::OQS_SIG pointer
    const char *str_native = (*env)->GetStringUTFChars(env, jstr, 0);
    OQS_SIG *sig = OQS_SIG_new(str_native);
    (*env)->ReleaseStringUTFChars(env, jstr, str_native);
    // Stow the native OQS_SIG pointer in the Java handle.
    setHandle(env, obj, sig, "native_sig_handle_");
}

/*
 * Class:     org_openquantumsafe_Signature
 * Method:    free_sig
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_net_ivpn_liboqs_Signature_free_1sig
  (JNIEnv *env, jobject obj)
{
    OQS_SIG *sig = (OQS_SIG *) getHandle(env, obj, "native_sig_handle_");
    OQS_SIG_free(sig);
}

/*
 * Class:     org_openquantumsafe_Signature
 * Method:    get_sig_details
 * Signature: ()Lnet/ivpn/liboqs/Signature/SignatureDetails;
 */
JNIEXPORT jobject JNICALL Java_net_ivpn_liboqs_Signature_get_1sig_1details
  (JNIEnv *env, jobject obj)
{
    jclass cls = (*env)->FindClass(env, "net/ivpn/liboqs/Signature$SignatureDetails");
    if (cls == NULL) { fprintf(stderr, "\nCould not find class\n"); return NULL; }

    // Get the Method ID of the constructor
    jmethodID constructor_meth_id_ = (*env)->GetMethodID(env, cls, "<init>", "(Lnet/ivpn/liboqs/Signature;)V");
    if (NULL == constructor_meth_id_) { fprintf(stderr, "\nCould not initialize class\n"); return NULL; }

    // Call back constructor to allocate a new instance, with an int argument
    jobject _nativeKED = (*env)->NewObject(env, cls, constructor_meth_id_, obj);

    OQS_SIG *sig = (OQS_SIG *) getHandle(env, obj, "native_sig_handle_");

    // Copy fields from C struct to Java class
    // String method_name;
    jfieldID _method_name = (*env)->GetFieldID(env, cls, "method_name", "Ljava/lang/String;");
    jstring j_method_name = (*env)->NewStringUTF(env, sig->method_name);
    (*env)->SetObjectField(env, _nativeKED, _method_name, j_method_name);

    // String alg_version;
    jfieldID _alg_version = (*env)->GetFieldID(env, cls, "alg_version", "Ljava/lang/String;");
    jstring j_alg_version = (*env)->NewStringUTF(env, sig->alg_version);
    (*env)->SetObjectField(env, _nativeKED, _alg_version, j_alg_version);

    // byte claimed_nist_level;
    jfieldID _claimed_nist_level = (*env)->GetFieldID(env, cls, "claimed_nist_level", "B");
    (*env)->SetByteField(env, _nativeKED, _claimed_nist_level, (jbyte) sig->claimed_nist_level);

    // boolean is_euf_cma;
    jfieldID _is_euf_cma = (*env)->GetFieldID(env, cls, "is_euf_cma", "Z");
    (*env)->SetBooleanField(env, _nativeKED, _is_euf_cma, (jboolean) sig->euf_cma);

    // long length_public_key;
    jfieldID _length_public_key = (*env)->GetFieldID(env, cls, "length_public_key", "J");
    (*env)->SetLongField(env, _nativeKED, _length_public_key, (jlong) sig->length_public_key);

    // long length_secret_key;
    jfieldID _length_secret_key = (*env)->GetFieldID(env, cls, "length_secret_key", "J");
    (*env)->SetLongField(env, _nativeKED, _length_secret_key, (jlong) sig->length_secret_key);

    // long max_length_signature;
    jfieldID _max_length_signature = (*env)->GetFieldID(env, cls, "max_length_signature", "J");
    (*env)->SetLongField(env, _nativeKED, _max_length_signature, (jlong) sig->length_signature);

    return _nativeKED;
}

/*
 * Class:     org_openquantumsafe_Signature
 * Method:    generate_keypair
 * Signature: ([B[B)I
 */
JNIEXPORT jint JNICALL Java_net_ivpn_liboqs_Signature_generate_1keypair
  (JNIEnv *env, jobject obj, jbyteArray jpublic_key, jbyteArray jsecret_key)
{
    jbyte *public_key_native = (*env)->GetByteArrayElements(env, jpublic_key, 0);
    jbyte *secret_key_native = (*env)->GetByteArrayElements(env, jsecret_key, 0);

    // Get pointer to sig
    OQS_SIG *sig = (OQS_SIG *) getHandle(env, obj, "native_sig_handle_");

    // Invoke liboqs sig keypair generation function
    OQS_STATUS rv_ = OQS_SIG_keypair(sig, (uint8_t*) public_key_native, (uint8_t*) secret_key_native);

    (*env)->ReleaseByteArrayElements(env, jpublic_key, public_key_native, JNI_COMMIT);
    (*env)->ReleaseByteArrayElements(env, jsecret_key, secret_key_native, JNI_COMMIT);
    return (rv_ == OQS_SUCCESS) ? 0 : -1;
}

/*
 * Class:     org_openquantumsafe_Signature
 * Method:    sign
 * Signature: ([BLjava/lang/Long;[BJ[B)I
 */
JNIEXPORT jint JNICALL Java_net_ivpn_liboqs_Signature_sign
  (JNIEnv * env, jobject obj, jbyteArray jsignature, jobject sig_len_obj,
      jbyteArray jmessage, jlong message_len, jbyteArray jsecret_key)
{
    // Convert to jbyte arrays
    jbyte *signature_native = (*env)->GetByteArrayElements(env, jsignature, 0);
    jbyte *message_native = (*env)->GetByteArrayElements(env, jmessage, 0);
    jbyte *secret_key_native = (*env)->GetByteArrayElements(env, jsecret_key, 0);

    OQS_SIG *sig = (OQS_SIG *) getHandle(env, obj, "native_sig_handle_");
    size_t len_sig;
    OQS_STATUS rv_ = OQS_SIG_sign(sig, (uint8_t*)signature_native, &len_sig,
                                    (uint8_t*)message_native, message_len,
                                    (uint8_t*)secret_key_native);

    // fill java signature bytes
    (*env)->SetByteArrayRegion(env, jsignature, 0, len_sig, (jbyte*) signature_native);

    // fill java object signature length
    jfieldID value_fid = (*env)->GetFieldID(env,
                                    (*env)->GetObjectClass(env, sig_len_obj),
                                    "value", "Ljava/lang/Object;");
    jclass cls = (*env)->FindClass(env, "java/lang/Long");
    jobject jlong_obj = (*env)->NewObject(env, cls,
                                (*env)->GetMethodID(env, cls, "<init>", "(J)V"),
                                (jlong) len_sig);
    (*env)->SetObjectField(env, sig_len_obj, value_fid, jlong_obj);

    // Release C memory
    (*env)->ReleaseByteArrayElements(env, jsignature, signature_native, JNI_COMMIT);
    (*env)->ReleaseByteArrayElements(env, jmessage, message_native, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, jsecret_key, secret_key_native, JNI_ABORT);

    return (rv_ == OQS_SUCCESS) ? 0 : -1;
}

/*
 * Class:     org_openquantumsafe_Signature
 * Method:    verify
 * Signature: ([BJ[BJ[B)Z
 */
JNIEXPORT jboolean JNICALL Java_net_ivpn_liboqs_Signature_verify
  (JNIEnv *env, jobject obj, jbyteArray jmessage, jlong message_len,
      jbyteArray jsignature, jlong signature_len, jbyteArray jpublic_key)
{
    // Convert to jbyte arrays
    jbyte *message_native = (*env)->GetByteArrayElements(env, jmessage, 0);
    jbyte *signature_native = (*env)->GetByteArrayElements(env, jsignature, 0);
    jbyte *public_key_native = (*env)->GetByteArrayElements(env, jpublic_key, 0);

    OQS_SIG *sig = (OQS_SIG *) getHandle(env, obj, "native_sig_handle_");
    OQS_STATUS rv_ = OQS_SIG_verify(sig, (uint8_t*) message_native, message_len,
                                    (uint8_t*) signature_native, signature_len,
                                    (uint8_t*) public_key_native);

    // Release C memory
    (*env)->ReleaseByteArrayElements(env, jsignature, signature_native, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, jmessage, message_native, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, jpublic_key, public_key_native, JNI_ABORT);

    return (rv_ == OQS_SUCCESS) ? JNI_TRUE : JNI_FALSE;
}
