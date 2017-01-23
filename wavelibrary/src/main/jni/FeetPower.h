#ifndef Header_FeetPower
#define Header_FeetPower

#include <math.h>
#include <pthread.h>

#include "FeetPower.h"
#include "Superpowered/SuperpoweredAdvancedAudioPlayer.h"
#include "Superpowered/SuperpoweredFilter.h"
#include "Superpowered/SuperpoweredRoll.h"
#include "Superpowered/SuperpoweredFlanger.h"
#include "Superpowered/SuperpoweredAndroidAudioIO.h"

#define NUM_BUFFERS 2
#define HEADROOM_DECIBEL 3.0f
static const float headroom = powf(10.0f, -HEADROOM_DECIBEL * 0.025);

class FeetPower {
public:

	FeetPower(const char *path, int *params);
	~FeetPower();

	bool process(short int *output, unsigned int numberOfSamples);
	void onPlayPause(bool play);
	void setVolume(int play);
	void onCrossfader(int value);
	void onFxSelect(int value);
	void onFxOff();
	void onFxValue(int value);
	void onChangeBpm(float value);
	void setTempo (float tempo, bool masterTempo);
	void setBpm(float bpm);
	void openPathB(const char *path, int *params);
	void openPathA(const char *path, int *params);
	void openPath(const char *path);
	void setSeek(float percent);
	float getBufferEndPerCent();
	long getDurationSeconds();
	long getPositonSeconds();
	float getPositionPercent();
    bool isPlaying();




private:
    pthread_mutex_t mutex;
    SuperpoweredAndroidAudioIO *audioSystem;
    SuperpoweredAdvancedAudioPlayer *playerA, *playerB;
    SuperpoweredRoll *roll;
    SuperpoweredFilter *filter;
    SuperpoweredFlanger *flanger;
    float *stereoBuffer;
    unsigned char activeFx;
    float crossValue, volA, volB;
};

#endif
