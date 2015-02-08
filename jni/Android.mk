LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_LDLIBS := -llog
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE    := libSPenEventInjector
LOCAL_SHARED_LIBRARIES := liblog
LOCAL_SRC_FILES := EventInjector.c
include $(BUILD_SHARED_LIBRARY)
