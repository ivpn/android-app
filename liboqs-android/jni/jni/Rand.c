#include <oqs/oqs.h>
#include "Rand.h"

/*
 * Class:     org_openquantumsafe_Rand
 * Method:    randombytes
 * Signature: (J)[B
 */
JNIEXPORT jbyteArray JNICALL Java_net_ivpn_liboqs_Rand_randombytes
  (JNIEnv *env, jclass cls, jlong bytes_to_read)
{
    // create array that will be passed back to Java
    jbyteArray jrand_bytes = (jbyteArray)(*env)->NewByteArray(env, bytes_to_read);
    if (jrand_bytes == NULL) return NULL;

    // allocate and fill C based array
    jbyte *rand_bytes_native = calloc(bytes_to_read, sizeof(jbyte));
    if (rand_bytes_native == NULL) return NULL;

    // native call to liboqs
    OQS_randombytes((uint8_t*) rand_bytes_native, bytes_to_read);

    // Store java byte array
    (*env)->SetByteArrayRegion(env, jrand_bytes, 0, bytes_to_read, (jbyte*) rand_bytes_native);

    // free memory allocated inside C
    free(rand_bytes_native);
    return jrand_bytes;
}

/*
 * Class:     org_openquantumsafe_Rand
 * Method:    randombytes_switch_algorithm_native
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_net_ivpn_liboqs_Rand_randombytes_1switch_1algorithm_1native
  (JNIEnv *env, jclass cls, jstring jstr)
{
    const char *alg_name_native = (*env)->GetStringUTFChars(env, jstr, 0);
    int rv_ = OQS_randombytes_switch_algorithm(alg_name_native);
    (*env)->ReleaseStringUTFChars(env, jstr, alg_name_native);
    return (rv_ == OQS_SUCCESS) ? 0 : -1;
}

/*
 * Class:     org_openquantumsafe_Rand
 * Method:    randombytes_nist_kat_init
 * Signature: ([B[BJ)V
 */
JNIEXPORT void JNICALL Java_net_ivpn_liboqs_Rand_randombytes_1nist_1kat_1init
  (JNIEnv *env, jclass cls, jbyteArray jentropy_input, jbyteArray jpers_str, jlong pers_str_len)
{
    jbyte *entropy_input_native = (*env)->GetByteArrayElements(env, jentropy_input, 0);

    if (pers_str_len == 0) {
        OQS_randombytes_nist_kat_init_256bit((uint8_t*) entropy_input_native, NULL);
    } else {
        jbyte *pers_str_native = (*env)->GetByteArrayElements(env, jpers_str, 0);
        OQS_randombytes_nist_kat_init_256bit((uint8_t*) entropy_input_native, (uint8_t*) pers_str_native);
        (*env)->ReleaseByteArrayElements(env, jpers_str, pers_str_native, JNI_ABORT);
    }

    (*env)->ReleaseByteArrayElements(env, jentropy_input, entropy_input_native, JNI_ABORT);
}
