#ifndef _HANDLE_H_INCLUDED_
#define _HANDLE_H_INCLUDED_

#include <jni.h>
#include <oqs/oqs.h>

jfieldID getHandleField(JNIEnv *, jobject, char *);

void *getHandle(JNIEnv *, jobject, char *);

void setHandle(JNIEnv *, jobject, void *, char *);

#endif
