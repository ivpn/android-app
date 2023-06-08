#include <oqs/oqs.h>
#include "KEMs.h"

/*
 * Class:     org_openquantumsafe_KEMs
 * Method:    max_number_KEMs
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_ivpn_liboqs_KEMs_max_1number_1KEMs
  (JNIEnv *env, jclass cls)
{
    return (jint) OQS_KEM_alg_count();
}

/*
 * Class:     org_openquantumsafe_KEMs
 * Method:    is_KEM_enabled
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_net_ivpn_liboqs_KEMs_is_1KEM_1enabled
  (JNIEnv *env, jclass cls, jstring java_str)
{
	const char *str_native = (*env)->GetStringUTFChars(env, java_str, 0);
    int is_enabled = OQS_KEM_alg_is_enabled (str_native);
	(*env)->ReleaseStringUTFChars(env, java_str, str_native);
    return (is_enabled) ? JNI_TRUE : JNI_FALSE;
}

/*
 * Class:     org_openquantumsafe_KEMs
 * Method:    get_KEM_name
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_net_ivpn_liboqs_KEMs_get_1KEM_1name
  (JNIEnv *env, jclass cls, jlong alg_id)
{
    const char *str_native = OQS_KEM_alg_identifier((size_t) alg_id);
    return (*env)->NewStringUTF(env, str_native);
}

