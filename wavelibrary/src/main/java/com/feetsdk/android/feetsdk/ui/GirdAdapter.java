package com.feetsdk.android.feetsdk.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.feetsdk.android.R;
import com.feetsdk.android.common.utils.ImageLoader;
import com.feetsdk.android.feetsdk.entity.response.RspSinger;
import com.feetsdk.android.feetsdk.musicplayer.AlbumArtCache;

import java.util.List;

import static android.R.id.list;

/**
 * Created by cuieney on 16/11/29.
 */
public class GirdAdapter extends BaseAdapter<RspSinger> {
    public GirdAdapter(List<RspSinger> list, Context context) {
        super(list, context);
    }


    @Override
    protected View getViewContainer(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView == null) {
            view = inflater.inflate(R.layout.singer_item,parent,false);
            convertView = view;
            convertView.setTag(new MyHolder(view));
        }
        final MyHolder holder = (MyHolder) convertView.getTag();
        RspSinger rspSinger = list.get(position);
        ImageLoader.getInstance().displayImage(context,rspSinger.getHeadingImgUrl(),holder.singerHead);
        holder.singerName.setText(rspSinger.getName());

        return convertView;
    }



    public static class MyHolder {

        public final RoundImageView singerHead;
        public final TextView singerName;

        public MyHolder(View itemView) {
            singerHead = ((RoundImageView) itemView.findViewById(R.id.singer));
            singerName = ((TextView) itemView.findViewById(R.id.singer_name));
        }
    }
}
