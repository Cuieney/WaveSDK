package com.feetsdk.android.feetsdk.player;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.feetsdk.android.common.utils.Logger;
import com.feetsdk.android.common.utils.SharedPreferencesHelper;
import com.feetsdk.android.feetsdk.Music;
import com.feetsdk.android.feetsdk.annotation.EventType;
import com.feetsdk.android.feetsdk.db.OrmHelper;
import com.feetsdk.android.feetsdk.db.domain.table.FavSong;
import com.feetsdk.android.feetsdk.db.domain.table.LocalSongs;
import com.feetsdk.android.feetsdk.http.HttpControler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cuieney on 16/11/21.
 */
public class MusicMgr implements IMusicControler {

    private FeetPlayer player;
    private List<Music> musics;
    private int index;
    private OrmHelper ormHelper;
    private HttpControler httpControler;
    private WeakReference<Context> contextWeakReference;
    private int type;

    public MusicMgr(@NonNull WeakReference<Context> contextWeakReference) {
        this.contextWeakReference = contextWeakReference;
        player = new FeetPlayer(contextWeakReference.get());
        musics = new ArrayList<>();
        ormHelper = new OrmHelper(contextWeakReference.get());
        httpControler = new HttpControler(contextWeakReference.get());

    }

    private void initMusic(int type) {
        this.type = type;
        if (EventType.PLAY_MUSIC_TYPE_NORMAL == type) {
            List<LocalSongs> music = ormHelper.getMusic();
            for (LocalSongs song : music) {
                if (true) {
                    Music msc = new Music(song.getSongId(), song.getSongName(), song.getCoverImageUrl(),
                            song.getProgress(), song.getPath(), song.getSingerName(), song.getTempo(),
                            song.getSize(), song.getCollection(), song.getListener(),song.getImgPath());
                    musics.add(msc);
                }
            }
        } else if (EventType.PLAY_MUSIC_TYPE_COLLECTION == type) {
            List<FavSong> favMsc = ormHelper.getFavMsc();
            for (FavSong song : favMsc) {
                Music msc = new Music(song.getSongId(), song.getSongName(), song.getCoverImageUrl(),
                        song.getProgress(), song.getPath(), song.getSingerName(), song.getTempo(),
                        song.getSize(), true, true,song.getImgPath());
                musics.add(msc);
            }
        }
        SharedPreferencesHelper.getInstance(contextWeakReference.get()).putIntValue("PLAYER_TYPE",type);
    }



    private void configPlayer() {
//        int lastIndex = SharedPreferencesHelper.getInstance(contextWeakReference.get()).getIntValue("PLAYER_POSITION");
//        Logger.d("last="+lastIndex+",index="+index);
//        if (lastIndex!= 0 && lastIndex == index) {
//            return;
//        }
//        index = lastIndex;
        if (musics.size() > 0) {
            Music music = musics.get(index);
            player.play(music.getPath(),true);
            Logger.d("configPlayer" + music.getSongName());
            ormHelper.updateIsListener(music.getSongId());
            player.setOnComplicationListener(new CompletionCallBack() {
                @Override
                public void onCompletionListener() {
                    next();
                }
            });
            contextWeakReference.get().sendBroadcast(new Intent(PlayerControler.MUSIC_CHANGE));
//            SharedPreferencesHelper.getInstance(contextWeakReference.get()).putIntValue("PLAYER_POSITION",index);
        }
    }

    @Override
    public void pause() {
        player.onPlayPause(false);
    }

    @Override
    public void start() {
        player.onPlayPause(true);
    }

    @Override
    public void next() {
        playControler(0);
    }

    @Override
    public void preview() {
        playControler(1);
    }

    @Override
    public void stop() {
        if (ormHelper != null) {
            ormHelper.closeDb();
            ormHelper = null;
        }
        if (player != null) {
            player.onPlayPause(false);
            player = null;
        }
    }


    @Override
    public void setTempo(float rate) {
        player.setTempo(rate);
    }

    @Override
    public void setBpm(float bpm) {
        player.setBpm(bpm);
    }

    @Override
    public void setSeek(int percent) {
        player.setSeek(percent);
    }

    @Override
    public Music getCurrentMusic() {
        return musics.get(index);
    }

    @Override
    public void play(int type) {
        initMusic(type);
        index = 0;
        configPlayer();
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public void favMusic() {
        localFavMsc(getCurrentMusic());
    }

    private void localFavMsc(Music currentMusic) {
        if (EventType.PLAY_MUSIC_TYPE_NORMAL == type) {
           ormHelper.updateFavSong(currentMusic.getSongId());
        } else if (EventType.PLAY_MUSIC_TYPE_COLLECTION == type) {
            ormHelper.updateFavCollection(currentMusic.getSongId());
        }
    }

    private void playControler(int type) {
        if (musics.size() < 0) {
            return;
        }
        if (type == 0) {
            index++;
            index = (index >= musics.size() ? 0 : index);
        } else {
            index--;
            index = (index < 0 ? 0 : index);
        }
        configPlayer();
    }


}
