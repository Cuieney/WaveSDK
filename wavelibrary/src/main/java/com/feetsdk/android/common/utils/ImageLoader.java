package com.feetsdk.android.common.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.feetsdk.android.R;
import com.feetsdk.android.feetsdk.musicplayer.AlbumArtCache;

/**
 * Created by paohaile on 17/2/7.
 */

public class ImageLoader {

    private static ImageLoader imageLoader;

    public static ImageLoader getInstance(){
        return InnerCls.imageLoader;
    }

    private static class InnerCls{
        public static ImageLoader imageLoader = new ImageLoader();
    }

    public void displayImage(Context context, String url, ImageView imageView){

        String fetchArtUrl = null;
        Bitmap art = null;
        if (url != null) {
            art = AlbumArtCache.getInstance().getBigImage(url);
            if (art == null) {
                fetchArtUrl = url;
                art = BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.ic_default_art);
            }
        }

        imageView.setImageBitmap(art);
        if (fetchArtUrl != null) {
            fetchBitmapFromURLAsync(fetchArtUrl,imageView);
        }

    }

    private void fetchBitmapFromURLAsync(String bitmapUrl, final ImageView singerHead) {
        AlbumArtCache.getInstance().fetch(bitmapUrl, new AlbumArtCache.FetchListener() {
            @Override
            public void onFetched(String artUrl, Bitmap bitmap, Bitmap icon) {
                singerHead.setImageBitmap(icon);
            }
        });
    }
}
