/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <jni.h>
#include <time.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#define  LOG_TAG    "m2x_log_libqrcode"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

/* Set to 1 to enable debug log traces. */
#define DEBUG 0

static void
downscale(unsigned char* src, int width, int height, unsigned char* dst) {
    int w = (width >> 1);
    int h = (height >> 1);
    for (int y = 0; y < h; y++) {
        for (int x = 0; x < w; x++) {
            dst[y * w + x] = (unsigned char*) ((src[(y * width << 1) + (x << 1)]
                                    + src[((y << 1) + 1) * width + (x << 1)]
                                    + src[((y << 1) + 1) * width + (x << 1) + 1]
                                    + src[(y * width << 1) + (x << 1) + 1]) >> 2);
        }
    }
}

static void
increase_contrast(unsigned char* data, int width, int height) {

    int anzpixel= width * height;
    int* histogram = (int*)malloc(sizeof(int) * 256);
    // build a Lookup table LUT containing scale factor
    float* lut = (float*)malloc(sizeof(float) * 256);

    memset(histogram, 0, sizeof(int) * 256);

    //read pixel intensities into histogram
    for (int x = 1; x < width; x++) {
        for (int y = 1; y < height; y++) {
            int valueBefore = data[y * width + x] & 0xff;
            histogram[valueBefore]++;
        }
    }

    int sum =0;

    for (int i = 0; i < 255; i++) {
        sum += histogram[i];
        lut[i] = sum * 255 / anzpixel;
    }

    // transform image using sum histogram as a Lookup table
    for (int x = 1; x < width; x++) {
        for (int y = 1; y < height; y++) {
            unsigned char valueBefore = data[y * width + x];
            unsigned char valueAfter= (unsigned char) lut[valueBefore & 0xff];
            data[y * width + x] = valueAfter;
        }
    }

    free(histogram);
    free(lut);
}

JNIEXPORT jbyteArray JNICALL Java_com_google_zxing_NativeLib_downscaleByHalf(
        JNIEnv * env,
        jobject thiz,
        jbyteArray src,
        jint width,
        jint height)
{
    jbyte* srcBuffer = (*env)->GetByteArrayElements(env, src, NULL);
    jint size = (width >> 1) * (height >> 1);
    jbyteArray dst = (*env)->NewByteArray(env, size);
    jbyte* dstBuffer = (*env)->GetByteArrayElements(env, dst, NULL);

    downscale(srcBuffer, width, height, dstBuffer);

    (*env)->ReleaseByteArrayElements(env, src, srcBuffer, 0);
    (*env)->ReleaseByteArrayElements(env, dst, dstBuffer, 0);

    return dst;
}

JNIEXPORT void JNICALL Java_com_google_zxing_NativeLib_increaseContrast(
        JNIEnv * env,
        jobject thiz,
        jbyteArray src,
        jint width,
        jint height)
{
    jbyte* srcBuffer = (*env)->GetByteArrayElements(env, src, NULL);

    increase_contrast(srcBuffer, width, height);

    (*env)->SetByteArrayRegion(env, src, 0, width * height, srcBuffer);

    (*env)->ReleaseByteArrayElements(env, src, srcBuffer, 0);
}