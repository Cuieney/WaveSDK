package com.feetsdk.android.feetsdk.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.feetsdk.android.FeetConfig;
import com.feetsdk.android.common.utils.LocalPathResolver;
import com.feetsdk.android.feetsdk.db.domain.DaoMaster;
import com.feetsdk.android.feetsdk.db.domain.DaoSession;
import com.feetsdk.android.feetsdk.db.domain.FavSongDao;
import com.feetsdk.android.feetsdk.db.domain.LocalSongsDao;
import com.feetsdk.android.feetsdk.db.domain.table.FavSong;
import com.feetsdk.android.feetsdk.db.domain.table.LocalSongs;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cuieney on 16/11/11.
 */
public class OrmHelper {
    private final static String dbName = "feet_db";
    private static OrmHelper mInstance;

    private DaoMaster.DevOpenHelper openHelper;
    private LocalSongsDao songDao;
    private FavSongDao favSongDao;
    private WeakReference<Context> contextWeakReference;
    private DaoSession daoSession;
    private FeetConfig config;

    public OrmHelper(Context context) {
        this.contextWeakReference = new WeakReference<>(context);
        config = new FeetConfig(context);
        openHelper = new DaoMaster.DevOpenHelper(contextWeakReference.get(), dbName, null);
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        daoSession = daoMaster.newSession();
        songDao = daoSession.getLocalSongsDao();
        favSongDao = daoSession.getFavSongDao();
    }

    private Context getContext() {
        return contextWeakReference.get();
    }

    private SQLiteDatabase getWritableDatabase() {
        if (openHelper == null) {
            openHelper = new DaoMaster.DevOpenHelper(getContext(), dbName, null);
        }
        SQLiteDatabase db = openHelper.getWritableDatabase();
        return db;
    }

    private SQLiteDatabase getReadableDatabase() {
        if (openHelper == null) {
            openHelper = new DaoMaster.DevOpenHelper(getContext(), dbName, null);
        }
        SQLiteDatabase db = openHelper.getReadableDatabase();
        return db;
    }

    public static OrmHelper getInstance(Context context) {
        if (mInstance == null) {
            synchronized (OrmHelper.class) {
                if (mInstance == null) {
                    mInstance = new OrmHelper(context);
                }
            }
        }
        return mInstance;
    }


    public synchronized void insertSongToLocal(LocalSongs localSongs) {
        if (querySong(localSongs.getSongId()) == null && queryAllSong().size() <= config.getDefaultMusciSize()) {
            songDao.insertOrReplace(localSongs);
        }
    }





    public void deleteSong(String songId) {
        LocalSongs localSongs = querySong(songId);
        if (localSongs != null) {

            if (queryFavMsc(songId) == null) {
                //查询收藏表中是否有数据 有则不删MP3
                deleteFile(songId);
            }
            songDao.delete(localSongs);
        }
    }


    @NonNull
    public List<LocalSongs> queryAllSong() {
        List<LocalSongs> songses = new ArrayList<>();
        if (songDao != null) {
            songses = songDao.queryBuilder().list();
        }
        return songses;
    }

    public LocalSongs querySong(String id) {
        if (id == null) {
            return null;
        }
        return songDao.queryBuilder()
                .where(LocalSongsDao.Properties.SongId.eq(id))
                .unique();
    }

    public void updateSongDownloadProgress(String songId, int progress) {
        if (querySong(songId) != null) {
            LocalSongs localSongs = querySong(songId);
            int currentProgress = localSongs.getProgress();
            if (progress > currentProgress) {
                localSongs.setProgress(progress);
            }
            songDao.update(localSongs);
        }

    }

    public void updateImgDownloadProgress(String songId, int progress) {
        if (querySong(songId) != null) {
            LocalSongs localSongs = querySong(songId);
            int currentProgress = localSongs.getImgProgress();
            if (progress > currentProgress) {
                localSongs.setImgProgress(progress);
            }
            songDao.update(localSongs);
        }

    }



    public void updateFavMscProgress(String songId, int progress) {
        if (queryFavMsc(songId) != null) {
            FavSong localSongs = queryFavMsc(songId);
            int currentProgress = localSongs.getProgress();
            if (progress > currentProgress) {
                localSongs.setProgress(progress);
            }
            favSongDao.update(localSongs);
        }
    }

    public void updateImgFavMscProgress(String songId, int progress) {
        if (queryFavMsc(songId) != null) {
            FavSong localSongs = queryFavMsc(songId);
            int currentProgress = localSongs.getImgProgress();
            if (progress > currentProgress) {
                localSongs.setImgProgress(progress);
            }
            favSongDao.update(localSongs);
        }
    }


    //操作数据库1 对数据库2进行修改。（第一张表改字段，第二张表增删）
    public void updateFavSong(String songId) {
        LocalSongs querySong = querySong(songId);
        if (querySong != null) {
            boolean collection = querySong.getCollection();
            if (collection) {
                deleteFavMsc(songId);
            } else {
                insertFavMsc(new FavSong(
                        querySong.getSongId(),
                        querySong.getSongName(),
                        querySong.getCoverImageUrl(),
                        querySong.getProgress(),
                        querySong.getMp3Url(),
                        querySong.getPath(),
                        querySong.getSingerName(),
                        querySong.getTempo(),
                        querySong.getSize(),
                        true,querySong.getImgProgress()
                        ,querySong.getImgPath()

                ));
            }
            querySong.setCollection(!collection);

            songDao.update(querySong);
        }


    }

    public void updateIsListener(String songId) {
        if (querySong(songId) != null) {
            LocalSongs localSongs = querySong(songId);
            localSongs.setListener(true);
            songDao.update(localSongs);
        }
    }

    public void deleteAllSong() {
        if (queryAllSong().size() > 0) {
            List<LocalSongs> localSongses = queryAllSong();
            for (LocalSongs song : localSongses) {
                songDao.delete(song);
            }

            File root = new File(LocalPathResolver.getDir());
            File imgRoot = new File( LocalPathResolver.getImgDir());
            if (root.exists()) {
                deleteAllFiles(root);
            }
            if (imgRoot.exists()) {
                deleteAllFiles(imgRoot);
            }

        }
    }

    public void deletePrivateSong() {
        if (queryAllSong().size() > 0) {
            List<LocalSongs> localSongses = queryAllSong();
            for (LocalSongs song : localSongses) {
                deleteSong(song.getSongId());
            }
        }
    }

    public void closeDb() {
        if (openHelper != null) {
            openHelper.close();
            openHelper = null;
        }
        if (daoSession != null) {
            daoSession = null;
        }
        if (songDao != null) {
            songDao = null;
        }

        contextWeakReference = null;
    }

    private void deleteFile(String fileName) {
        String path = LocalPathResolver.getDir() + fileName+".mp3";
        String imgPath =  LocalPathResolver.getImgDir() + fileName+".jpg";
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        File imgFile = new File(imgPath);
        if (imgFile.exists()) {
            imgFile.delete();
        }

    }


    private void deleteAllFiles(File root) {
        File files[] = root.listFiles();
        if (files != null)
            for (File f : files) {
                if (f.isDirectory()) { // 判断是否为文件夹
                    deleteAllFiles(f);
                    try {
                        f.delete();
                    } catch (Exception e) {
                    }
                } else {
                    if (f.exists()) { // 判断是否存在
                        deleteAllFiles(f);
                        try {
                            f.delete();
                        } catch (Exception e) {
                        }
                    }
                }
            }
    }


    public List<LocalSongs> getMusic() {
        List<LocalSongs> musics = new ArrayList<>();
        List<LocalSongs> localSongses = queryAllSong();
        if (localSongses.size() > 0) {
            for (LocalSongs song : localSongses) {
                if (song.getProgress() == 100) {
                    musics.add(song);
                }
            }
        }
        return musics;
    }

    public List<FavSong> getFavMsc() {
        List<FavSong> musics = new ArrayList<>();
        List<FavSong> localSongses = queryAllFavMsc();
        if (localSongses.size() > 0) {
            for (FavSong song : localSongses) {
                if (song.getProgress() == 100) {
                    musics.add(song);
                }
            }
        }
        return musics;
    }


    public List<FavSong> queryAllFavMsc() {
        List<FavSong> musics = new ArrayList<>();
        if (songDao != null) {
            musics = favSongDao.queryBuilder().list();
        }
        return musics;
    }

    public FavSong queryFavMsc(String mscId) {
        FavSong unique = favSongDao.queryBuilder()
                .where(FavSongDao.Properties.SongId.eq(mscId))
                .unique();
        return unique;
    }

    public void insertFavMsc(FavSong favSong) {
        if (queryFavMsc(favSong.getSongId()) == null && queryAllFavMsc().size() <= config.getDefaultFavMusciSize()) {
            favSongDao.insertOrReplace(favSong);
        }
    }

    public void deleteFavMsc(String mscId) {
        if (queryFavMsc(mscId) != null) {
            favSongDao.delete(queryFavMsc(mscId));
            if (querySong(mscId) == null) {
                deleteFile(mscId);
            }
        }
    }


    //第二张表操作，只改collection字段
    public void updateFavCollection(String mscId) {
        FavSong favSong = queryFavMsc(mscId);
        if (favSong != null) {
            boolean collection = !favSong.getCollection();
            favSong.setCollection(collection);
            favSongDao.update(favSong);
            //查询第一张表是否存在第二张表的数据
            LocalSongs localSongs = querySong(mscId);
            if (localSongs != null) {
                localSongs.setCollection(collection);
                songDao.update(localSongs);
            }
        }

    }



    public void clearData() {
        List<LocalSongs> localSongses = queryAllSong();
        List<FavSong> favSongs = queryAllFavMsc();
        if (localSongses.size() > 0) {
            for (LocalSongs msc : localSongses) {
                songDao.delete(msc);
                deleteFile(msc.getSongId());
            }
        }

        if (favSongs.size() > 0) {
            for (FavSong msc : favSongs) {
                favSongDao.delete(msc);
                deleteFile(msc.getSongId());
            }
        }
    }
}
