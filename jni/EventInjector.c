/*
 * Android Event Injector 
 *
 * Copyright (c) 2013 by Radu Motisan , radu.motisan@gmail.com
 * Original at https://github.com/radhoo/android-event-injector/blob/master/jni/EventInjector.c
 * Modifications by Tushar Dudani (previously available at https://github.com/tushar9989/cmspen)
 * Additional modifications (c) 2015 by the CyanogenMod community
 * Additional modifications (c) 2017 by the LineageOS community
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * For more information on the GPL, please go to:
 * http://www.gnu.org/copyleft/gpl.html
 *
 */

#include <string.h>
#include <stdint.h>
#include <jni.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <stdio.h>
#include <dirent.h>
#include <time.h>
#include <errno.h>

#include <sys/ioctl.h>
#include <sys/mman.h>
#include <sys/types.h>
#include <sys/inotify.h>
#include <sys/limits.h>
#include <sys/poll.h>

#include <linux/fb.h>
#include <linux/kd.h>
#include <linux/input.h>

#include <android/log.h>
#define TAG "CMSPen::JNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , TAG, __VA_ARGS__) 
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define EVENT_SIZE  ( sizeof (struct inotify_event) )
#define BUF_LEN     ( 1024 * ( EVENT_SIZE + 16 ) )



/* Debug tools
 */
 int g_debug = 0;



jint Java_net_pocketmagic_android_eventinjector_Events_intEnableDebug( JNIEnv* env,jobject thiz, jint enable ) {

	g_debug = enable;
	return g_debug;
}
 
jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
	return JNI_VERSION_1_2; //1_2 1_4
}

void JNI_OnUnload(JavaVM *vm, void *reserved)
{

}

static struct typedev {
	struct pollfd ufds;
	char *device_path;
	char *device_name;
} *pDevs = NULL;
struct pollfd *ufds;
static nfds_t nDevsCount;

const char *device_path = "/dev/input";

int g_Polling = 0;
struct input_event event;
int c;
int i;
int pollres;
int get_time = 0;
char *newline = "\n";
uint16_t get_switch = 0;
struct input_event event;
int version;

int dont_block = -1;
int event_count = 0;
int sync_rate = 0;
int64_t last_sync_time = 0;
const char *device = NULL; 


static int open_device(int index)
{
	if (index >= nDevsCount || pDevs == NULL) return -1;
	char *device = pDevs[index].device_path;
	
    int version;
    int fd;
    
    char name[80];
    char location[80];
    char idstr[80];
    struct input_id id;
	
    fd = open(device, O_RDWR);
    if(fd < 0) {
		pDevs[index].ufds.fd = -1;
		
		pDevs[index].device_name = NULL;
        return -1;
    }
    
	pDevs[index].ufds.fd = fd;
	ufds[index].fd = fd;
	
    name[sizeof(name) - 1] = '\0';
    if(ioctl(fd, EVIOCGNAME(sizeof(name) - 1), &name) < 1) {
        name[0] = '\0';
    }
	
	pDevs[index].device_name = strdup(name);
    
    
    return 0;
}

int remove_device(int index)
{
	if (index >= nDevsCount || pDevs == NULL ) return -1;
	
	int count = nDevsCount - index - 1;
	free(pDevs[index].device_path);
	free(pDevs[index].device_name);
	
	memmove(&pDevs[index], &pDevs[index+1], sizeof(pDevs[0]) * count);
	nDevsCount--;
	return 0;
} 



static int scan_dir(const char *dirname)
{
	nDevsCount = 0;
    char devname[PATH_MAX];
    char *filename;
    DIR *dir;
    struct dirent *de;
    dir = opendir(dirname);
    if(dir == NULL)
        return -1;
    strcpy(devname, dirname);
    filename = devname + strlen(devname);
    *filename++ = '/';
    while((de = readdir(dir))) {
        if(de->d_name[0] == '.' &&
           (de->d_name[1] == '\0' ||
            (de->d_name[1] == '.' && de->d_name[2] == '\0')))
            continue;
        strcpy(filename, de->d_name);
		// add new filename to our structure: devname
		struct typedev *new_pDevs = realloc(pDevs, sizeof(pDevs[0]) * (nDevsCount + 1));
		if(new_pDevs == NULL) {
			return -1;
		}
		pDevs = new_pDevs;
		
		struct pollfd *new_ufds = realloc(ufds, sizeof(ufds[0]) * (nDevsCount + 1));
		if(new_ufds == NULL) {
			return -1;
		}
		ufds = new_ufds; 
		ufds[nDevsCount].events = POLLIN;
		
		pDevs[nDevsCount].ufds.events = POLLIN;
		pDevs[nDevsCount].device_path = strdup(devname);
		
        nDevsCount++;
    }
    closedir(dir);
    return 0;
}



jint Java_net_pocketmagic_android_eventinjector_Events_ScanFiles( JNIEnv* env,jobject thiz ) {
	int res = scan_dir(device_path);
	if(res < 0) {
		return -1;
	}
	
	return nDevsCount;
}

jstring Java_net_pocketmagic_android_eventinjector_Events_getDevPath( JNIEnv* env,jobject thiz, jint index) {
	return (*env)->NewStringUTF(env, pDevs[index].device_path);
}
jstring Java_net_pocketmagic_android_eventinjector_Events_getDevName( JNIEnv* env,jobject thiz, jint index) {
	if (pDevs[index].device_name == NULL) return NULL;
	else return (*env)->NewStringUTF(env, pDevs[index].device_name);
}

jint Java_net_pocketmagic_android_eventinjector_Events_OpenDev( JNIEnv* env,jobject thiz, jint index ) {
	return open_device(index);
}

jint Java_net_pocketmagic_android_eventinjector_Events_RemoveDev( JNIEnv* env,jobject thiz, jint index ) {
	return remove_device(index);
}

jint Java_net_pocketmagic_android_eventinjector_Events_PollDev( JNIEnv* env,jobject thiz, jint index ) {
	if (index >= nDevsCount || pDevs[index].ufds.fd == -1) return -1;
	int pollres = poll(ufds, nDevsCount, -1);
	if(ufds[index].revents) {
		if(ufds[index].revents & POLLIN) {
			ssize_t res = read(ufds[index].fd, &event, sizeof(event));
			if(res < (int)sizeof(event)) {
				return 1;
			} 
			else return 0;
		}
	}
	return -1;
}

//Added by Tushar Dudani

JNIEXPORT jint JNICALL Java_org_lineageos_cmspen_SPenDetection_AddFileChangeListener( JNIEnv* env, jobject thiz, jint index ) {
	int fd;
	int wd;
	ssize_t length = 0;
	if (index >= nDevsCount || pDevs[index].ufds.fd == -1) {LOGD("Device is null.");return -1;}
	fd = inotify_init();
	if (fd < 0) {LOGD("Notify failed.");return -1;}
	if (pDevs[index].device_path == NULL) {LOGD("Device path null");return -1;}
	wd = inotify_add_watch (fd, pDevs[index].device_path, IN_ACCESS | IN_MODIFY | IN_OPEN);
	//LOGD("Waiting for S Pen event....");
	while(length <= 0)
	{
		length = read( fd, &event, sizeof(event) );
	}
	
	jclass cls = (*env)->FindClass(env, "org/lineageos/cmspen/SPenDetection");
	jmethodID mID = (*env)->GetStaticMethodID(env, cls, "waitForEvent", "()V");
	//LOGD("Calling method waitForEvent()");
	(*env)->CallStaticVoidMethod(env, thiz, mID);
	
	( void ) inotify_rm_watch( fd, wd );
	( void ) close( fd );
	return 0;
}

//end

jint Java_net_pocketmagic_android_eventinjector_Events_getType( JNIEnv* env,jobject thiz ) {
	return event.type;
}

jint Java_net_pocketmagic_android_eventinjector_Events_getCode( JNIEnv* env,jobject thiz ) {
	return event.code;
}

jint Java_net_pocketmagic_android_eventinjector_Events_getValue( JNIEnv* env,jobject thiz ) {
	return event.value;
}
