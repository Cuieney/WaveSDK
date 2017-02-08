package com.feetsdk.android.feetsdk.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.feetsdk.android.R;
import com.feetsdk.android.common.exception.HttpException;
import com.feetsdk.android.common.utils.ImageLoader;
import com.feetsdk.android.common.utils.ToastUtil;
import com.feetsdk.android.feetsdk.entity.response.RspSinger;
import com.feetsdk.android.feetsdk.http.HttpControler;
import com.feetsdk.android.feetsdk.http.HttpResponse;
import com.feetsdk.android.feetsdk.http.IHttpRspCallBack;
import com.zhy.autolayout.AutoLayoutActivity;
import com.zhy.autolayout.utils.AutoUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChooseSingerActivity extends AutoLayoutActivity implements BaseAdapter.OnItemClickListener, View.OnClickListener {

    private ViewPager vpg;
    private LinearLayout points;
    private RelativeLayout searchContainer;
    private ImageView singerOne;
    private ImageView deleteSingerOne;
    private ImageView singerTwo;
    private ImageView deleteSingerTwo;
    private ImageView singerThree;
    private ImageView deleteSingerThree;
    private TextView openRunRadio;
    private TextView singerNameOne;
    private TextView singerNameTwo;
    private TextView singerNameThree;
    private View cancel;
    private HttpControler httpControler;

    private List<RspSinger> mData;
    private List<GridView> gridViewList;

    private Map<Integer, RspSinger> chooseSingerList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_choose_singer);

        mData = new ArrayList<>();
        gridViewList = new ArrayList<>();
        chooseSingerList = new LinkedHashMap<>();
        chooseSingerList.put(1, null);
        chooseSingerList.put(2, null);
        chooseSingerList.put(3, null);
        httpControler = new HttpControler(this);
        initView();
        loadData();
    }

    private void loadData() {

        vpg.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int integer) {
                for (int i = 0; i < points.getChildCount(); i++) {
                    View childAt = points.getChildAt(i);
                    if (integer == i) {
                        childAt.setBackground(getResources().getDrawable(R.drawable.point_light));
                    } else {
                        childAt.setBackground(getResources().getDrawable(R.drawable.point_dark));
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        httpControler.getArtists(new IHttpRspCallBack() {
            @Override
            public void success(HttpResponse response) {
                List<RspSinger> rspSingerList = new ArrayList<>();
                try {
                    JSONArray jsonArray = new JSONArray(response.getMessage());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String name = jsonObject.getString("name");
                        String id = jsonObject.getString("id");
                        String headingImgUrl = jsonObject.getString("headingImgUrl");
                        RspSinger rspSinger = new RspSinger(name, id, headingImgUrl);
                        rspSingerList.add(rspSinger);
                    }
                    mData.addAll(rspSingerList);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadView();
                    }
                });

            }

            @Override
            public void failed(HttpException exception) {
                Toast.makeText(ChooseSingerActivity.this, "请求网络失败", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadView() {
        int change = 0;
        HashMap<Integer, List<RspSinger>> hashMap = new HashMap<>();
        List<RspSinger> save = new ArrayList<>();
        for (int i = 0; i < mData.size(); i++) {
            int index = i / 6;
            if (change == index) {
                save.add(mData.get(i));
                if (i == mData.size() - 1)
                    hashMap.put(change, save);
            } else {
                hashMap.put(change, save);
                change = index;
                save = new ArrayList<>();
                save.add(mData.get(i));
            }
        }


        int numberPager = mData.size() / 6;
        for (int i = 0; i < numberPager; i++) {
            GridView gridView = new GridView(this);
            gridView.setNumColumns(3);
            gridView.setHorizontalSpacing(34);
            gridView.setVerticalSpacing(30);
            AutoUtils.auto(gridView);
            GirdAdapter adapter = new GirdAdapter(hashMap.get(i), this);
            adapter.setOnItemClickListener(this);
            gridView.setAdapter(adapter);
            gridViewList.add(gridView);

            View view = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(6, 6);
            lp.setMargins(7, 0, 7, 0);
            if (i == 0) {
                view.setBackground(getResources().getDrawable(R.drawable.point_light));
            } else {
                view.setBackground(getResources().getDrawable(R.drawable.point_dark));
            }
            view.setLayoutParams(lp);
            AutoUtils.auto(view);
            points.addView(view);
        }

        vpg.setAdapter(new ChooseSingerVpgAdapter(gridViewList));

    }

    private void initView() {
        cancel = findViewById(R.id.cancel);
        vpg = ((ViewPager) findViewById(R.id.singer_vpg));
        searchContainer = (RelativeLayout) findViewById(R.id.search_container);
        points = (LinearLayout) findViewById(R.id.points);
        singerOne = (ImageView) findViewById(R.id.singer_one);
        deleteSingerOne = (ImageView) findViewById(R.id.delete_singer_one);
        singerTwo = (ImageView) findViewById(R.id.singer_two);
        deleteSingerTwo = (ImageView) findViewById(R.id.delete_singer_two);
        singerThree = (ImageView) findViewById(R.id.singer_three);
        deleteSingerThree = (ImageView) findViewById(R.id.delete_singer_three);
        openRunRadio = ((TextView) findViewById(R.id.open_run_radio));
        singerNameOne = (TextView) findViewById(R.id.singer_name_one);
        singerNameTwo = (TextView) findViewById(R.id.singer_name_two);
        singerNameThree = (TextView) findViewById(R.id.singer_name_three);

        deleteSingerOne.setOnClickListener(this);
        deleteSingerTwo.setOnClickListener(this);
        deleteSingerThree.setOnClickListener(this);
        searchContainer.setOnClickListener(this);
        cancel.setOnClickListener(this);
        chooseThree();
    }


    @Override
    public void onItemClick(Object t) {
        judgeArtist(t);
        chooseThree();
    }

    private void chooseThree() {
        boolean isAll = true;
        for (Map.Entry<Integer, RspSinger> entry : chooseSingerList.entrySet()) {
            RspSinger value = entry.getValue();
            if (value == null) {
                isAll = false;
            }
        }
        if (isAll) {
            openRunRadio.setBackgroundResource(R.drawable.open_run_radio_sp);
        }else{
            openRunRadio.setBackgroundResource(R.drawable.search_singer_sp);
        }
    }

    public void judgeArtist(Object t) {
        RspSinger artistReq = (RspSinger) t;
        for (Map.Entry<Integer, RspSinger> entry : chooseSingerList.entrySet()) {
            RspSinger value = entry.getValue();
            if (value != null) {
                if (artistReq.getId().equals(value.getId())) {
                    return;
                }
            }
        }
        setSingerData(artistReq);
    }

    private void deleteSingerData(int index) {
        switch (index) {
            case 1:
                chooseSingerList.put(1, null);
                singerOne.setImageResource(R.drawable.singer_head_bg_sp);
                singerNameOne.setText("");
                deleteSingerOne.setVisibility(View.INVISIBLE);
                break;
            case 2:
                chooseSingerList.put(2, null);
                singerTwo.setImageResource(R.drawable.singer_head_bg_sp);
                singerNameTwo.setText("");
                deleteSingerTwo.setVisibility(View.INVISIBLE);
                break;
            case 3:
                chooseSingerList.put(3, null);
                singerThree.setImageResource(R.drawable.singer_head_bg_sp);
                singerNameThree.setText("");
                deleteSingerThree.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void setSingerData(RspSinger artistReq) {
        boolean isLive = false;
        for (Map.Entry<Integer, RspSinger> entry : chooseSingerList.entrySet()) {

            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());

            if (entry.getValue() == null) {
                Integer i = entry.getKey();
                switch (i) {
                    case 1:
                        chooseSingerList.put(1, artistReq);
                        ImageLoader.getInstance().displayImage(this, artistReq.getHeadingImgUrl(), singerOne);
                        singerNameOne.setText(artistReq.getName());
                        deleteSingerOne.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        chooseSingerList.put(2, artistReq);
                        ImageLoader.getInstance().displayImage(this, artistReq.getHeadingImgUrl(), singerTwo);
                        singerNameTwo.setText(artistReq.getName());
                        deleteSingerTwo.setVisibility(View.VISIBLE);
                        break;
                    case 3:
                        chooseSingerList.put(3, artistReq);
                        ImageLoader.getInstance().displayImage(this, artistReq.getHeadingImgUrl(), singerThree);
                        singerNameThree.setText(artistReq.getName());
                        deleteSingerThree.setVisibility(View.VISIBLE);
                        break;
                }
                isLive = true;
                return;
            }

        }
        if (!isLive) {
            ToastUtil.showToast(this, "只能选择三个歌手！");
        }

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.delete_singer_one) {
            deleteSingerData(1);

        } else if (i == R.id.delete_singer_two) {
            deleteSingerData(2);

        } else if (i == R.id.delete_singer_three) {
            deleteSingerData(3);

        } else if (i == R.id.search_container) {
            startActivityForResult(new Intent(this, SearchSingerActivity.class), 1);
        } else if(i == R.id.cancel){
            finish();
        }
        chooseThree();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            Bundle extras = data.getExtras();
            RspSinger artist = (RspSinger) extras.getSerializable("artist");
            judgeArtist(artist);
        }

    }
}
