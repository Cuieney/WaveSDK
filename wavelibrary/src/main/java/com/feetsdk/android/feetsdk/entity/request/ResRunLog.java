package com.feetsdk.android.feetsdk.entity.request;

import java.util.List;

/**
 * Created by cuieney on 16/11/10.
 */
public class ResRunLog {

    private String startedOn;
    private int stepCount;
    private int duration;

    private DeviceBean device;

    private List<SongsBean> songs;

    @Override
    public String toString() {
        return "ResRunLog{" +
                "startedOn='" + startedOn + '\'' +
                ", stepCount=" + stepCount +
                ", duration=" + duration +
                ", device=" + device +
                ", songs=" + songs +
                '}';
    }

    public String getStartedOn() {
        return startedOn;
    }

    public void setStartedOn(String startedOn) {
        this.startedOn = startedOn;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public DeviceBean getDevice() {
        return device;
    }

    public void setDevice(DeviceBean device) {
        this.device = device;
    }

    public List<SongsBean> getSongs() {
        return songs;
    }

    public void setSongs(List<SongsBean> songs) {
        this.songs = songs;
    }

    public static class DeviceBean {
        private String os;
        private String version;

        public DeviceBean( String version) {
            this.os = "android";
            this.version = version;
        }

        public String getOs() {
            return os;
        }

        public void setOs(String os) {
            this.os = os;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    public static class SongsBean {
        private String songId;
        private int duration;
        private int durationPlayed;
        private int matchRate;
        private int stepCount;
        private int tempoReal;

        public SongsBean(String songId, int duration, int durationPlayed, int matchRate, int stepCount, int tempoReal) {
            this.songId = songId;
            this.duration = duration;
            this.durationPlayed = durationPlayed;
            this.matchRate = matchRate;
            this.stepCount = stepCount;
            this.tempoReal = tempoReal;
            this.geo = new GeoBean(0,0);
        }

        private GeoBean geo;

        public String getSongId() {
            return songId;
        }

        public void setSongId(String songId) {
            this.songId = songId;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public int getDurationPlayed() {
            return durationPlayed;
        }

        public void setDurationPlayed(int durationPlayed) {
            this.durationPlayed = durationPlayed;
        }

        public int getMatchRate() {
            return matchRate;
        }

        public void setMatchRate(int matchRate) {
            this.matchRate = matchRate;
        }

        public int getStepCount() {
            return stepCount;
        }

        public void setStepCount(int stepCount) {
            this.stepCount = stepCount;
        }

        public int getTempoReal() {
            return tempoReal;
        }

        public void setTempoReal(int tempoReal) {
            this.tempoReal = tempoReal;
        }

        public GeoBean getGeo() {
            return geo;
        }

        public void setGeo(GeoBean geo) {
            this.geo = geo;
        }

        public static class GeoBean {
            private int lgn;
            private int lat;

            public GeoBean(int lat, int lgn) {
                this.lat = lat;
                this.lgn = lgn;
            }

            public int getLgn() {
                return lgn;
            }

            public void setLgn(int lgn) {
                this.lgn = lgn;
            }

            public int getLat() {
                return lat;
            }

            public void setLat(int lat) {
                this.lat = lat;
            }
        }
    }
}
