package com.example.z.scheduleroom;

import android.content.Context;

public final class ScreenUtils {

     /*
    * 定义一些距离常量,单位为dp,计算的时候需要转换为px
    * */

    public static final int LEFT_CELL_WIDTH = 18;
    public static final int NORMAL_CELL_HEIGHT = 80;
    public static final int RIGHT_SHOW_LENGTH = 57;
    public static final int NORMAL_CELL_ITEM_HEIGHT = 20;
    public static final int OUTDATED_DIVIDER = 1;


    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int dp2px(Context context, int dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static int dp2px(Context context, double dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static int getTopMargin(Context context, int row) {
        return dp2px(context, NORMAL_CELL_ITEM_HEIGHT * row);
    }

}