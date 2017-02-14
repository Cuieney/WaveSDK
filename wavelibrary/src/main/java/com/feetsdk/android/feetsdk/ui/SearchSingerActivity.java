package com.feetsdk.android.feetsdk.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.feetsdk.android.R;
import com.feetsdk.android.common.exception.HttpException;
import com.feetsdk.android.common.utils.AppManager;
import com.feetsdk.android.feetsdk.entity.response.RspSinger;
import com.feetsdk.android.feetsdk.http.HttpControler;
import com.feetsdk.android.feetsdk.http.HttpResponse;
import com.feetsdk.android.feetsdk.http.IHttpRspCallBack;
import com.zhy.autolayout.AutoLayoutActivity;
import com.zhy.autolayout.utils.AutoUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class SearchSingerActivity extends AutoLayoutActivity implements BaseAdapter.OnItemClickListener {

    private EditText searchEdit;
    private ViewPager vpg;
    private LinearLayout points;
    private List<GridView> gridViewList;
    public HttpControler httpControler;
    private List<RspSinger> mData;
    private boolean isLoaded = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_search_singer);
        AppManager.getAppManager().addActivity(this);
        gridViewList = new ArrayList<>();
        mData = new ArrayList<>();
        LinkedHashMap<Object, Object> chooseSingerList = new LinkedHashMap<>();
        chooseSingerList.put(1, null);
        chooseSingerList.put(2, null);
        chooseSingerList.put(3, null);
        httpControler = new HttpControler(this);
        initView();
        initData();
    }

    private void initData() {
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

        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isLoaded) {
                    isLoaded = false;
                    String singerName = s.toString();
                    try {
                        singerName = URLEncoder.encode(singerName, "utf-8");
                        singerName = URLEncoder.encode(singerName, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    searchData(singerName);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });



    }


    private void searchData(String name){
        httpControler.searchArtists(new IHttpRspCallBack() {
            @Override
            public void success(HttpResponse response) {
                mData.clear();
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

            }
        }, name);
    }

    private void initView() {
        searchEdit = (EditText) findViewById(R.id.search_edit);
        vpg = (ViewPager) findViewById(R.id.singer_vpg);
        points = (LinearLayout) findViewById(R.id.points);
        View cancelTxt = findViewById(R.id.cancel_txt);

        cancelTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadView() {
        int change = 0;
        SparseArray<List<RspSinger>> hashMap = new SparseArray<>();
        points.removeAllViews();
        List<RspSinger> save = new ArrayList<>();
        for (int i = 0; i < mData.size(); i++) {
            int index = i / 6;
            if (change == index) {
                save.add(mData.get(i));
                if (i == mData.size() - 1){
                    hashMap.put(change, save);
                    save = new ArrayList<>();
                }
            } else {
                hashMap.put(change, save);
                change = index;
                save = new ArrayList<>();
                save.add(mData.get(i));
            }
        }

        gridViewList = new ArrayList<>();
        int numberPager = mData.size() / 6;
        if (mData.size() == 1) {
            numberPager = 1;
        }
        for (int i = 0; i < numberPager; i++) {
            GridView gridView = new GridView(this);
            gridView.setNumColumns(3);
            gridView.setHorizontalSpacing(34);
            gridView.setVerticalSpacing(30);
            AutoUtils.auto(gridView);
            GirdAdapter adapter = new GirdAdapter(hashMap.get(i), this);
            adapter.setOnItemClickListener(SearchSingerActivity.this);
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
        isLoaded = true;
    }

    @Override
    public void onItemClick(Object t) {
        RspSinger artistReq = (RspSinger) t;
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("artist", artistReq);
        intent.putExtras(bundle);
        this.setResult(RESULT_OK, intent);
        this.finish();
    }
}
