#include "handle.h"

jfieldID getHandleField(JNIEnv *env, jobject obj, char *handle_field) {
    jclass c = (*env)->GetObjectClass(env, obj);
    return (*env)->GetFieldID(env, c, handle_field, "J");
}

void *getHandle(JNIEnv *env, jobject obj, char *handle_field) {
    jlong handle = (*env)->GetLongField(env, obj, getHandleField(env, obj, handle_field));
    return (void *)(handle);
}

void setHandle(JNIEnv *env, jobject obj, void *t, char *handle_field) {
    jlong handle = (jlong) (t);
    (*env)->SetLongField(env, obj, getHandleField(env, obj, handle_field), handle);
}
