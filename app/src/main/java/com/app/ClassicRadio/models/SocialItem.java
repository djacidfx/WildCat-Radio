package com.app.ClassicRadio.models;

public class SocialItem {
    private int drawableResId;
    private String url;

    public SocialItem(int drawableResId, String url) {
        this.drawableResId = drawableResId;
        this.url = url;
    }

    public int getDrawableResId() {
        return drawableResId;
    }

    public void setDrawableResId(int drawableResId) {
        this.drawableResId = drawableResId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
