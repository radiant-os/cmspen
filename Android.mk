#
# Copyright (C) 2008 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_SRC_FILES := \
        $(call all-java-files-under, src) \

LOCAL_CERTIFICATE := platform
LOCAL_AAPT_INCLUDE_ALL_RESOURCES := true
LOCAL_PRIVILEGED_MODULE := true

LOCAL_PACKAGE_NAME := CMSPen
LOCAL_LDLIBS  := -llog
LOCAL_JNI_SHARED_LIBRARIES := libSPenEventInjector

ifneq ($(INCREMENTAL_BUILDS),)
    LOCAL_PROGUARD_ENABLED := disabled
    LOCAL_JACK_ENABLED := incremental
    LOCAL_DX_FLAGS := --multi-dex
    LOCAL_JACK_FLAGS := --multi-dex native
endif

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
