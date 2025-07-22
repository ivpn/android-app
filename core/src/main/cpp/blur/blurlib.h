/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Tamim Hossain.
 Copyright (c) 2025 IVPN Limited.

 This file is part of the IVPN Android app.

 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.

 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.

 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/
#ifndef IVPN_ANDROID_APP_BLURLIB_H
#define IVPN_ANDROID_APP_BLURLIB_H

#include <jni.h>
#include <android/bitmap.h>
#include <algorithm>
#include <cstring>

#define SQUARE(i) ((i)*(i))
#define MAX(x, y) ((x) > (y)) ? (x) : (y)
#define MIN(x, y) ((x) < (y)) ? (x) : (y)

#define SUCCESS 1
#define INVALID_RADIUS -1
#define CAN_NOT_GET_BITMAP_INFO -2
#define INVALID_BITMAP_FORMAT -3
#define BITMAP_CONCURRENCY_ERROR -4

inline static void zeroClearInt(int *p, size_t count) {
    memset(p, 0, sizeof(int) * count);
}

extern "C" {
    JNIEXPORT jobject JNICALL Java_net_ivpn_core_v2_account_widget_MaskedImageView_blur(JNIEnv *env, jobject clazz, jobject bitmap, jint radius);
    JNIEXPORT jobject JNICALL Java_net_ivpn_core_v2_account_widget_MaskedTextView_blur(JNIEnv *env, jobject clazz, jobject bitmap, jint radius);
}

#endif //IVPN_ANDROID_APP_BLURLIB_H


