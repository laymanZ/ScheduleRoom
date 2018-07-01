package com.example.z.scheduleroom.api;


public interface OnMeetingSelectChange {

    void onTimeAndPlace(String time, int position);

    void onTimeChange(String time);

    void onIsBottomDivVisible(boolean isBottomDivVisible);

}
