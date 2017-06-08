LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4
LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_PACKAGE_NAME := CarRecorder
LOCAL_MODULE_TAGS := optional
LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/res
LOCAL_RENDERSCRIPT_TARGET_API := 23
LOCAL_MULTILIB :=32
LOCAL_PREBUILT_JNI_LIBS := libs/armeabi/libImageProc.so
include $(BUILD_PACKAGE)
include $(call all-makefiles-under, $(LOCAL_PATH))