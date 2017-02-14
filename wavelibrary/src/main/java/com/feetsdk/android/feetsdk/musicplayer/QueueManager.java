package com.feetsdk.android.feetsdk.musicplayer;

import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;

import com.feetsdk.android.common.utils.Logger;
import com.feetsdk.android.feetsdk.Music;
import com.feetsdk.android.feetsdk.db.OrmHelper;
import com.feetsdk.android.feetsdk.db.domain.table.LocalSongs;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cuieney on 17/1/3.
 */
public class QueueManager {
    private static final String TAG = "QueueManager";
    private OrmHelper mMusicProvider;
    private MetadataUpdateListener mListener;

    // "Now playing" queue:
    private List<LocalSongs> mPlayingQueue;
    private int mCurrentIndex;

    public QueueManager(@NonNull OrmHelper musicProvider,
                        @NonNull MetadataUpdateListener listener) {
        this.mMusicProvider = musicProvider;
        this.mListener = listener;
        mPlayingQueue = new ArrayList<>();
        mCurrentIndex = 0;
        song2Item();

    }


    public void song2Item() {
        mPlayingQueue.clear();
        List<LocalSongs> music = mMusicProvider.getMusic();
        if (music.size() > 0) {
            mPlayingQueue.addAll(music);
        }
        mMusicProvider.updateIsListener(music.get(0).getSongId());
    }


    public void setCurrentQueueIndex(int index) {
        if (index >= 0 && index < mPlayingQueue.size()) {
            mCurrentIndex = index;
            mListener.onCurrentQueueIndexUpdated(mCurrentIndex);
        }
    }



    public boolean skipQueuePosition(int amount) {
        int index = mCurrentIndex + amount;
        if (index < 0) {
            // skip backwards before the first song will keep you on the first song
            index = 0;
        } else {
            // skip forwards when in last song will cycle back to start of the queue
            index %= mPlayingQueue.size();
        }
        if (!isIndexPlayable(index, mPlayingQueue)) {
            Logger.e(TAG, "Cannot increment queue index by "+amount+
                    ". Current="+ mCurrentIndex+" queue length="+ mPlayingQueue.size());
            return false;
        }
        mCurrentIndex = index;
        mMusicProvider.updateIsListener(getCurrentMusic().getSongId());

        return true;
    }


    public static boolean isIndexPlayable(int index, List<LocalSongs> queue) {
        return (queue != null && index >= 0 && index < queue.size());
    }

    public LocalSongs getCurrentMusic() {
        return mPlayingQueue.get(mCurrentIndex);
    }

    public int getCurrentQueueSize() {
        if (mPlayingQueue == null) {
            return 0;
        }
        return mPlayingQueue.size();
    }


    public void updateMetadata() {
        LocalSongs currentMusic = getCurrentMusic();
        if (currentMusic == null) {
            mListener.onMetadataRetrieveError();
            return;
        }
        mListener.onMetadataChanged(localSong2Music(currentMusic));
    }

    private Music localSong2Music(LocalSongs song){
        return new Music(song.getSongId(), song.getSongName(), song.getCoverImageUrl(),
                song.getProgress(), song.getPath(), song.getSingerName(), song.getTempo(),
                song.getSize(), song.getCollection(), song.getListener(),song.getImgPath());
    }


    public static MediaMetadataCompat music2Metadata(Music song) {
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.getSongName())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.getSingerName())
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,BitmapFactory.decodeFile(song.getImgPath()))
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.getSize())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,song.getCoverImageUrl())
                .build();
    }

    public interface MetadataUpdateListener {
        void onMetadataChanged(Music music);

        void onMetadataRetrieveError();

        void onCurrentQueueIndexUpdated(int queueIndex);

    }
}
