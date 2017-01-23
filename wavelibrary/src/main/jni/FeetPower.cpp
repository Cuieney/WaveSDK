#include "FeetPower.h"
#include "SuperpoweredSimple.h"
#include <jni.h>
#include <stdlib.h>
#include <stdio.h>
#include <android/log.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_AndroidConfiguration.h>


// Helper functions to update some Java object instance's member variables
static inline void setFloatField(JNIEnv *javaEnvironment, jobject self, jclass thisClass, const char *name, float value) {
    javaEnvironment->SetFloatField(self, javaEnvironment->GetFieldID(thisClass, name, "F"), value);
}

static inline void setLongField(JNIEnv *javaEnvironment, jobject self, jclass thisClass, const char *name, unsigned int value) {
    javaEnvironment->SetLongField(self, javaEnvironment->GetFieldID(thisClass, name, "J"), value);
}

static inline void setBoolField(JNIEnv *javaEnvironment, jobject self, jclass thisClass, const char *name, bool value) {
    javaEnvironment->SetBooleanField(self, javaEnvironment->GetFieldID(thisClass, name, "Z"), value);
}


static void playerEventCallbackA(void *clientData, SuperpoweredAdvancedAudioPlayerEvent event, void *value) {
    if (event == SuperpoweredAdvancedAudioPlayerEvent_LoadSuccess) {
    	SuperpoweredAdvancedAudioPlayer *playerA = *((SuperpoweredAdvancedAudioPlayer **)clientData);
        playerA->setBpm(126.0f);
        playerA->setFirstBeatMs(353);
        playerA->setPosition(playerA->firstBeatMs, false, false);
    };
}

static void playerEventCallbackB(void *clientData, SuperpoweredAdvancedAudioPlayerEvent event, void *value) {
    if (event == SuperpoweredAdvancedAudioPlayerEvent_LoadSuccess) {
    	SuperpoweredAdvancedAudioPlayer *playerB = *((SuperpoweredAdvancedAudioPlayer **)clientData);
        playerB->setBpm(123.0f);
        playerB->setFirstBeatMs(40);
        playerB->setPosition(playerB->firstBeatMs, false, false);
    }else{
        __android_log_print(ANDROID_LOG_DEBUG, "HLSExample", "Open error: %s", (char *)value);
    };
}

static bool audioProcessing(void *clientdata, short int *audioIO, int numberOfSamples, int samplerate) {
	return ((FeetPower *)clientdata)->process(audioIO, numberOfSamples);
}

FeetPower::FeetPower(const char *path, int *params) : activeFx(0), crossValue(0.0f), volB(0.0f), volA(1.0f * headroom) {
    pthread_mutex_init(&mutex, NULL); // This will keep our player volumes and playback states in sync.
    unsigned int samplerate = params[4], buffersize = params[5];
    stereoBuffer = (float *)memalign(16, (buffersize + 16) * sizeof(float) * 2);

    playerA = new SuperpoweredAdvancedAudioPlayer(&playerA , playerEventCallbackA, samplerate, 0);
    playerA->open(path, params[0], params[1]);
    playerB = new SuperpoweredAdvancedAudioPlayer(&playerB, playerEventCallbackB, samplerate, 0);
    playerB->open(path, params[2], params[3]);

    playerA->syncMode = playerB->syncMode = SuperpoweredAdvancedAudioPlayerSyncMode_TempoAndBeat;

    roll = new SuperpoweredRoll(samplerate);
    filter = new SuperpoweredFilter(SuperpoweredFilter_Resonant_Lowpass, samplerate);
    flanger = new SuperpoweredFlanger(samplerate);

    audioSystem = new SuperpoweredAndroidAudioIO(samplerate, buffersize, false, true, audioProcessing, this, -1, SL_ANDROID_STREAM_MEDIA, buffersize * 2);
}

FeetPower::~FeetPower() {
    delete audioSystem;
    delete playerA;
    delete playerB;
    free(stereoBuffer);
    pthread_mutex_destroy(&mutex);
}

void FeetPower::onPlayPause(bool play) {
    pthread_mutex_lock(&mutex);
    if (!play) {
     bool masterIsA = (crossValue <= 0.5f);
        playerA->pause(false);
        playerB->pause(false);
    } else {
        bool masterIsA = (crossValue <= 0.5f);
        playerA->play(true);
        playerB->play(true);
    };
    pthread_mutex_unlock(&mutex);
}

void FeetPower::setVolume(int value){
     pthread_mutex_lock(&mutex);
      bool masterIsA = (crossValue <= 0.5f);
      if(masterIsA){
            if(value == 10){
               playerB->pause(masterIsA);
               volA = 0.1f;
            }else{
                playerB->play(masterIsA);
                volA = 1.0f * headroom;
            }
      }else{
        if(value == 10){
            playerA->pause(masterIsA);
            volB = 0.1f;
        }else{
            playerA->play(masterIsA);
            volB = 1.0f * headroom;
        }
      }
     pthread_mutex_unlock(&mutex);

}


void FeetPower::onChangeBpm(float value) {
    pthread_mutex_lock(&mutex);
         bool masterIsA = (crossValue <= 0.5f);
         if(masterIsA){
           playerA->setBpm(value);
         }else{
           playerB->setBpm(value);
         }
    pthread_mutex_unlock(&mutex);
}

void FeetPower::setTempo (float tempo, bool masterTempo){
  pthread_mutex_lock(&mutex);
 //__android_log_print(ANDROID_LOG_VERBOSE, "FeetPower", "currentTemp %f", playerA->tempo);
 //__android_log_print(ANDROID_LOG_VERBOSE, "FeetPower", "currentBpm %f", playerA->currentBpm);
   //crossValue = 1+float(tempo) * 0.01f;
         playerA->setTempo(tempo,masterTempo);
         playerB->setTempo(tempo,masterTempo);
    pthread_mutex_unlock(&mutex);
}

void FeetPower::setBpm(float bpm){

 pthread_mutex_lock(&mutex);
         playerA->setBpm(bpm);
         playerB->setBpm(bpm);
    pthread_mutex_unlock(&mutex);
}

void FeetPower::openPathB(const char *path, int *params){
 pthread_mutex_lock(&mutex);
   playerB->pause(true);
   playerB->open(path, params[0], params[1]);
   playerB->play(true);
  // __android_log_print(ANDROID_LOG_VERBOSE, "FeetPower", "openPathB %f", volB);
 pthread_mutex_unlock(&mutex);
}


void FeetPower::openPathA(const char *path, int *params){
 pthread_mutex_lock(&mutex);
   playerA->pause(true);
   playerA->open(path, params[0], params[1]);
   playerA->play(true);
   // __android_log_print(ANDROID_LOG_VERBOSE, "FeetPower", "openPathA %f", volA);
 pthread_mutex_unlock(&mutex);
}

void FeetPower::openPath(const char *path){
   playerA->pause(true);
   playerA->open(path);
   playerA->play(true);
    //__android_log_print(ANDROID_LOG_VERBOSE, "FeetPower", "openPathA %f", volA);
}


void FeetPower::setSeek(float percent){
 pthread_mutex_lock(&mutex);
       bool masterIsA = (crossValue <= 0.5f);
       if(masterIsA){
            playerA->seek(percent);
       }else{
            playerB->seek(percent);
       }
 pthread_mutex_unlock(&mutex);


}

float FeetPower::getBufferEndPerCent(){
           bool masterIsA = (crossValue <= 0.5f);
           if(masterIsA){
                return playerA->bufferEndPercent;
           }else{
                return playerB->bufferEndPercent;
           }

}

long FeetPower::getDurationSeconds(){
     //pthread_mutex_lock(&mutex);
           bool masterIsA = (crossValue <= 0.5f);
           if(masterIsA){
                return playerA->durationSeconds;
           }else{
                return playerB->durationSeconds;
           }

    // pthread_mutex_unlock(&mutex);
}

long FeetPower::getPositonSeconds(){
    // pthread_mutex_lock(&mutex);
           bool masterIsA = (crossValue <= 0.5f);
           if(masterIsA){
                return playerA->positionSeconds;
           }else{
                return playerB->positionSeconds;
           }
   // pthread_mutex_unlock(&mutex);

}

float FeetPower::getPositionPercent(){
           bool masterIsA = (crossValue <= 0.5f);
           if(masterIsA){
                return playerA->positionPercent;
           }else{
                return playerB->positionPercent;
           }
}

bool FeetPower::isPlaying(){
    // pthread_mutex_lock(&mutex);
           bool masterIsA = (crossValue <= 0.5f);
           if(masterIsA){
                return playerA->playing;
           }else{
                return playerB->playing;
           }

     //pthread_mutex_unlock(&mutex);
}

void FeetPower::onCrossfader(int value) {
    pthread_mutex_lock(&mutex);

    crossValue = float(value) * 0.01f;
    if (crossValue < 0.01f) {
        volA = 1.0f * headroom;
        volB = 0.0f;
    } else if (crossValue > 0.99f) {
        volA = 0.0f;
        volB = 1.0f * headroom;
    } else { // constant power curve
        volA = cosf(M_PI_2 * crossValue) * headroom;
        volB = cosf(M_PI_2 * (1.0f - crossValue)) * headroom;
        // __android_log_print(ANDROID_LOG_VERBOSE, "FeetPower", "onCrossfader %i", value);
         //__android_log_print(ANDROID_LOG_VERBOSE, "FeetPower", "volA %f", volA);
         //__android_log_print(ANDROID_LOG_VERBOSE, "FeetPower", "volB %f", volB);
    };
    pthread_mutex_unlock(&mutex);
}

void FeetPower::onFxSelect(int value) {
	//__android_log_print(ANDROID_LOG_VERBOSE, "FeetPower", "FXSEL %i", value);
	activeFx = value;
}

void FeetPower::onFxOff() {
    filter->enable(false);
    roll->enable(false);
    flanger->enable(false);
}

#define MINFREQ 60.0f
#define MAXFREQ 20000.0f

static inline float floatToFrequency(float value) {
    if (value > 0.97f) return MAXFREQ;
    if (value < 0.03f) return MINFREQ;
    value = powf(10.0f, (value + ((0.4f - fabsf(value - 0.4f)) * 0.3f)) * log10f(MAXFREQ - MINFREQ)) + MINFREQ;
    return value < MAXFREQ ? value : MAXFREQ;
}

void FeetPower::onFxValue(int ivalue) {
    float value = float(ivalue) * 0.01f;
    switch (activeFx) {
        case 1:
            filter->setResonantParameters(floatToFrequency(1.0f - value), 0.2f);
            filter->enable(true);
            flanger->enable(false);
            roll->enable(false);
            break;
        case 2:
            if (value > 0.8f) roll->beats = 0.0625f;
            else if (value > 0.6f) roll->beats = 0.125f;
            else if (value > 0.4f) roll->beats = 0.25f;
            else if (value > 0.2f) roll->beats = 0.5f;
            else roll->beats = 1.0f;
            roll->enable(true);
            filter->enable(false);
            flanger->enable(false);
            break;
        default:
            flanger->setWet(value);
            flanger->enable(true);
            filter->enable(false);
            roll->enable(false);
    };
}

bool FeetPower::process(short int *output, unsigned int numberOfSamples) {
    pthread_mutex_lock(&mutex);

    bool masterIsA = (crossValue <= 0.5f);
    float masterBpm = masterIsA ? playerA->currentBpm : playerB->currentBpm;
    double msElapsedSinceLastBeatA = playerA->msElapsedSinceLastBeat; // When playerB needs it, playerA has already stepped this value, so save it now.

    bool silence = !playerA->process(stereoBuffer, false, numberOfSamples, volA, masterBpm, playerB->msElapsedSinceLastBeat);
    if (playerB->process(stereoBuffer, !silence, numberOfSamples, volB, masterBpm, msElapsedSinceLastBeatA)) silence = false;

    roll->bpm = flanger->bpm = masterBpm; // Syncing fx is one line.

    if (roll->process(silence ? NULL : stereoBuffer, stereoBuffer, numberOfSamples) && silence) silence = false;
    if (!silence) {
        filter->process(stereoBuffer, stereoBuffer, numberOfSamples);
        flanger->process(stereoBuffer, stereoBuffer, numberOfSamples);
    };

    pthread_mutex_unlock(&mutex);

    // The stereoBuffer is ready now, let's put the finished audio into the requested buffers.
    if (!silence) SuperpoweredFloatToShortInt(stereoBuffer, output, numberOfSamples);
    return !silence;
}

extern "C" {
	JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_FeetPower(JNIEnv *javaEnvironment, jobject self, jstring apkPath, jlongArray offsetAndLength);
	JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_onPlayPause(JNIEnv *javaEnvironment, jobject self, jboolean play);
	JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_onCrossfader(JNIEnv *javaEnvironment, jobject self, jint value);
	JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_onFxSelect(JNIEnv *javaEnvironment, jobject self, jint value);
	JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_onFxOff(JNIEnv *javaEnvironment, jobject self);
	JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_onFxValue(JNIEnv *javaEnvironment, jobject self, jint value);
	JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_onChangeBpm(JNIEnv *javaEnvironment, jobject self, jfloat value);
	JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_setTempo(JNIEnv *javaEnvironment, jobject self, jfloat tempo,jboolean masterTempo);
	JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_setBpm(JNIEnv *javaEnvironment, jobject self, jfloat bpm);
	JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_openPathB(JNIEnv *javaEnvironment, jobject self, jstring apkPath, jlongArray params);
	JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_openPathA(JNIEnv *javaEnvironment, jobject self, jstring apkPath, jlongArray params);
	JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_openPath(JNIEnv *javaEnvironment, jobject self, jstring apkPath);
	JNIEXPORT float Java_com_feetsdk_android_feetsdk_player_JniBridge_getPositionPercent(JNIEnv *javaEnvironment, jobject self);
	JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_Seek(JNIEnv *javaEnvironment, jobject self, jfloat percent);
	JNIEXPORT bool Java_com_feetsdk_android_feetsdk_player_JniBridge_isPlaying(JNIEnv *javaEnvironment, jobject self);
	JNIEXPORT long Java_com_feetsdk_android_feetsdk_player_JniBridge_getPositonSeconds(JNIEnv *javaEnvironment, jobject self);
	JNIEXPORT long Java_com_feetsdk_android_feetsdk_player_JniBridge_getDurationSeconds(JNIEnv *javaEnvironment, jobject self);
	JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_setVolume(JNIEnv *javaEnvironment, jobject self, jint value);
	JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_UpdateStatus(JNIEnv *javaEnvironment, jobject self);
}

static FeetPower *example = NULL;




// Android is not passing more than 2 custom parameters, so we had to pack file offsets and lengths into an array.
JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_FeetPower(JNIEnv *javaEnvironment, jobject self, jstring apkPath, jlongArray params) {
	// Convert the input jlong array to a regular int array.
    jlong *longParams = javaEnvironment->GetLongArrayElements(params, JNI_FALSE);
    int arr[6];
    for (int n = 0; n < 6; n++) arr[n] = longParams[n];
    javaEnvironment->ReleaseLongArrayElements(params, longParams, JNI_ABORT);

    const char *path = javaEnvironment->GetStringUTFChars(apkPath, JNI_FALSE);
    example = new FeetPower(path, arr);
    javaEnvironment->ReleaseStringUTFChars(apkPath, path);

}

JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_onPlayPause(JNIEnv *javaEnvironment, jobject self, jboolean play) {
	example->onPlayPause(play);
}

JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_onCrossfader(JNIEnv *javaEnvironment, jobject self, jint value) {
	example->onCrossfader(value);
}

JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_onFxSelect(JNIEnv *javaEnvironment, jobject self, jint value) {
	example->onFxSelect(value);
}

JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_onFxOff(JNIEnv *javaEnvironment, jobject self) {
	example->onFxOff();
}

JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_onFxValue(JNIEnv *javaEnvironment, jobject self, jint value) {
	example->onFxValue(value);
}

JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_onChangeBpm(JNIEnv *javaEnvironment, jobject self, jfloat value) {
	example->onChangeBpm(value);
}

JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_setTempo(JNIEnv *javaEnvironment, jobject self, jfloat tempo,jboolean masterTempo) {
	example->setTempo(tempo,masterTempo);
}

JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_setBpm(JNIEnv *javaEnvironment, jobject self, jfloat bpm) {
	example->setBpm(bpm);
}


JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_openPathB(JNIEnv *javaEnvironment, jobject self, jstring apkPath, jlongArray params) {
    jlong *longParams = javaEnvironment->GetLongArrayElements(params, JNI_FALSE);
        int arr[4];
        for (int n = 0; n < 4; n++) arr[n] = longParams[n];
        javaEnvironment->ReleaseLongArrayElements(params, longParams, JNI_ABORT);

        const char *path = javaEnvironment->GetStringUTFChars(apkPath, JNI_FALSE);
	    example->openPathB(path,arr);
        javaEnvironment->ReleaseStringUTFChars(apkPath, path);
}


JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_openPathA(JNIEnv *javaEnvironment, jobject self, jstring apkPath, jlongArray params) {
    jlong *longParams = javaEnvironment->GetLongArrayElements(params, JNI_FALSE);
        int arr[4];
        for (int n = 0; n < 4; n++) arr[n] = longParams[n];
        javaEnvironment->ReleaseLongArrayElements(params, longParams, JNI_ABORT);

        const char *path = javaEnvironment->GetStringUTFChars(apkPath, JNI_FALSE);
	    example->openPathA(path,arr);
        javaEnvironment->ReleaseStringUTFChars(apkPath, path);
}

JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_openPath(JNIEnv *javaEnvironment, jobject self, jstring apkPath){

        const char *path = javaEnvironment->GetStringUTFChars(apkPath, JNI_FALSE);
	    example->openPath(path);
        javaEnvironment->ReleaseStringUTFChars(apkPath, path);

}


JNIEXPORT float Java_com_feetsdk_android_feetsdk_player_JniBridge_getPositionPercent(JNIEnv *javaEnvironment, jobject self) {
      return example->getPositionPercent();
}

JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_Seek(JNIEnv *javaEnvironment, jobject self, jfloat percent){
    example->setSeek(percent);
}

JNIEXPORT bool Java_com_feetsdk_android_feetsdk_player_JniBridge_isPlaying(JNIEnv *javaEnvironment, jobject self){

    return example->isPlaying();
}

JNIEXPORT long Java_com_feetsdk_android_feetsdk_player_JniBridge_getPositonSeconds(JNIEnv *javaEnvironment, jobject self){

    return example->getPositonSeconds();
}

JNIEXPORT long Java_com_feetsdk_android_feetsdk_player_JniBridge_getDurationSeconds(JNIEnv *javaEnvironment, jobject self){

    return example->getDurationSeconds();
}

JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_setVolume(JNIEnv *javaEnvironment, jobject self, jint value) {
	example->setVolume(value);
}


JNIEXPORT void Java_com_feetsdk_android_feetsdk_player_JniBridge_UpdateStatus(JNIEnv *javaEnvironment, jobject self) {
    jclass thisClass = javaEnvironment->GetObjectClass(self);
    //setFloatField(javaEnvironment, self, thisClass, "bufferStartPercent", player->bufferStartPercent);

         setLongField(javaEnvironment, self, thisClass, "durationSeconds", example->getDurationSeconds());
         setLongField(javaEnvironment, self, thisClass, "positionSeconds", example->getPositonSeconds());
         setFloatField(javaEnvironment, self, thisClass, "positionPercent", example->getPositionPercent());
         setBoolField(javaEnvironment, self, thisClass, "playing", example->isPlaying());

}


