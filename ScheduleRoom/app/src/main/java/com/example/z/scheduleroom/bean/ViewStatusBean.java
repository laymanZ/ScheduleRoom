package com.example.z.scheduleroom.bean;

import android.view.View;

/**
 * 保存view的状态
 * 可以参考 saveHierarchyState 是一个DFS
 */
public class ViewStatusBean {

    private int status;
    private boolean hadColor;
    private View container;


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isHadColor() {
        return hadColor;
    }

    public void setHadColor(boolean hadColor) {
        this.hadColor = hadColor;
    }

    public View getContainer() {
        return container;
    }

    public void setContainer(View container) {
        this.container = container;
    }

}
