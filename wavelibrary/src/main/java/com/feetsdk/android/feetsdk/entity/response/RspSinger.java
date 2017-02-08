package com.feetsdk.android.feetsdk.entity.response;

import java.io.Serializable;

/**
 * Created by paohaile on 17/2/7.
 */

public class RspSinger implements Serializable {

    /**
     * name : 降央卓玛
     * id : 57594
     * headingImgUrl : http://artist-img.paohaile.com/57594_2.jpg
     */

    private String name;
    private String id;
    private String headingImgUrl;

    @Override
    public String toString() {
        return "RspSinger{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", headingImgUrl='" + headingImgUrl + '\'' +
                '}';
    }

    public RspSinger(String name, String id, String headingImgUrl) {
        this.name = name;
        this.id = id;
        this.headingImgUrl = headingImgUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHeadingImgUrl() {
        return headingImgUrl;
    }

    public void setHeadingImgUrl(String headingImgUrl) {
        this.headingImgUrl = headingImgUrl;
    }
}
