package com.feetsdk.android.feetsdk.entity;

import java.util.List;

/**
 * Created by cuieney on 16/12/9.
 */
public class UserInfo {

    /**
     * mobile : 18365268012
     * artists : ["1260","3110","23352"]
     * membership : {"level":"basic","validTill":"2047-01-31T00:00:00.000Z"}
     */

    private String mobile;
    /**
     * level : basic
     * validTill : 2047-01-31T00:00:00.000Z
     */

    private MembershipBean membership;
    private List<String> artists;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public MembershipBean getMembership() {
        return membership;
    }

    public void setMembership(MembershipBean membership) {
        this.membership = membership;
    }

    public List<String> getArtists() {
        return artists;
    }

    public void setArtists(List<String> artists) {
        this.artists = artists;
    }

    public static class MembershipBean {
        private String level;
        private String validTill;

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public String getValidTill() {
            return validTill;
        }

        public void setValidTill(String validTill) {
            this.validTill = validTill;
        }
    }
}
