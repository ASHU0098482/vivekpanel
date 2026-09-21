class Tab1 {

public:
    void create(JNIEnv* env, const char* name ) {
        jclass CMenu = env->FindClass("com/vivek/Menu");
        jmethodID MTab1 = env->GetStaticMethodID(CMenu, "selectTab", "(Ljava/lang/String;)V");
        return env->CallStaticVoidMethod(CMenu, MTab1, env->NewStringUTF(name));
    }

};