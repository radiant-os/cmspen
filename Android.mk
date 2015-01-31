LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_STATIC_JAVA_LIBRARIES := \
	android-support-v4

LOCAL_MODULE_TAGS := optional

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_SRC_FILES := \
        $(call all-java-files-under, src) \

LOCAL_CERTIFICATE := platform
LOCAL_AAPT_INCLUDE_ALL_RESOURCES := true
LOCAL_PRIVILEGED_MODULE := true

LOCAL_PACKAGE_NAME := CMSPen

LOCAL_JNI_SHARED_LIBRARIES := libSPenEventInjector
include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
