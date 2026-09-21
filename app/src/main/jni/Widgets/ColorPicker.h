#ifndef COLORPICKER_H
#define COLORPICKER_H

#include <jni.h>

class ColorPicker {

public:
    void create(JNIEnv* env, const char* name, jint ID) {
        jclass CMenu = env->FindClass("com/vivek/Menu");
        if (!CMenu) return;
        jmethodID MColorPicker = env->GetStaticMethodID(CMenu, "addColorPicker", "(Ljava/lang/String;I)V");
        if (!MColorPicker) return;
        jstring jName = env->NewStringUTF(name);
        env->CallStaticVoidMethod(CMenu, MColorPicker, jName, ID);
        env->DeleteLocalRef(jName);
    }

};

#endif
