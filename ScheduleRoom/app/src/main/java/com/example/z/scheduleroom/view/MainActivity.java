package com.example.z.scheduleroom.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.TextView;

import com.example.z.scheduleroom.R;
import com.example.z.scheduleroom.adapter.ScheduleMajorAdapter;
import com.example.z.scheduleroom.adapter.ScheduleTopAdapter;
import com.example.z.scheduleroom.api.OnMeetingSelectChange;
import com.example.z.scheduleroom.bean.CourseBean;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<CourseBean> topData;
    private List<String> contentData;

    String[] courseArr = {"语文", "数学", "地理", "化学", "农药", "吃鸡", "LOL", "DOTA"};

    private ScheduleRoom scheduleRoom;
    private TextView mScheduleTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scheduleRoom = findViewById(R.id.content_container);
        mScheduleTv = findViewById(R.id.tv_schedule);

        topData = new ArrayList<>();
        contentData = new ArrayList<>();

        int len = courseArr.length;
        for (int i = 0; i < 30; i++) {
            CourseBean courseBean = new CourseBean();
            courseBean.setCourseName(courseArr[(int) (Math.random() * len)]);
            courseBean.setCourseId("id:" + i);
            topData.add(courseBean);
            contentData.add(i + " haha");
        }

        scheduleRoom.getTopRecyclerView().setAdapter(new ScheduleTopAdapter(topData, this));
        scheduleRoom.getTopRecyclerView().setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        ScheduleMajorAdapter majorAdapter = new ScheduleMajorAdapter(contentData, this);
        scheduleRoom.getMajorRecyclerView().setAdapter(majorAdapter);
        ScheduleLayoutManager scheduleLayoutManager = new ScheduleLayoutManager();
        scheduleLayoutManager.setTotalColumnCount(topData.size());
        scheduleRoom.getMajorRecyclerView().setLayoutManager(scheduleLayoutManager);

        majorAdapter.setOnMeetingSelectChange(new OnMeetingSelectChange() {
            @Override
            public void onTimeAndPlace(final String time, final int position) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (position != -1)
                            mScheduleTv.setText(topData.get(position).getCourseName() + ":" + time);
                        else
                            mScheduleTv.setText("no select");
                    }
                });
            }

            @Override
            public void onTimeChange(String time) {

            }

            @Override
            public void onIsBottomDivVisible(boolean isBottomDivVisible) {

            }
        });
    }

}
