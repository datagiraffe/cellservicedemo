package de.tu_berlin.snet.cellactivitydemo;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.tu_berlin.snet.cellservice.model.database.GeoDatabaseHelper;
import de.tu_berlin.snet.cellservice.model.record.Data;


public class TimeLineFragment extends Fragment {
    private final static String LOG_TAG = TimeLineFragment.class.getSimpleName();

    @Bind(R.id.event_list)
    LinearLayout eventListLayout;

    private Activity context;

    public static TimeLineFragment newInstance(Date date) {
        Log.e(LOG_TAG, "TimeLineFragment constructor");
        TimeLineFragment aFragment = new TimeLineFragment();

        Bundle args = new Bundle();
        args.putLong("date", date.getTime());
        aFragment.setArguments(args);

        return aFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        View view = inflater.inflate(R.layout.timeline_container, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rebuildDataTable();
    }

    private void rebuildDataTable() {
        Log.e(LOG_TAG, "rebuildDataTable");
        // get all the database entries
        GeoDatabaseHelper myDb = GeoDatabaseHelper.getInstance(context);
        Log.e(LOG_TAG, "the date: "+new Date(getArguments().getLong("date")));
        ArrayList<Data> databaseEntries = myDb.getDataRecords(new Date(getArguments().getLong("date")));

        // get the timeline container and inflater to add event items to it
        LayoutInflater li = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int durationHeight = 100; // default duration height

        // variables for sigmoid based function determining time interval drawing length
        double a = 1.902, b = 2.024, c = 13618, d = 1408;

        for (int i = 0; i < databaseEntries.size(); i++) {
            View eventView = li.inflate(R.layout.timeline_event, (ViewGroup) null);
            TextView eventText = (TextView) eventView.findViewById(R.id.eventText);
            TextView eventTime = (TextView) eventView.findViewById(R.id.eventTime);
            View eventTimeBar = (View) eventView.findViewById(R.id.eventTimeBar);

            // extract color from string that contains cellid + lac and set it as the background
            String cellidLAC = databaseEntries.get(i).getCell().toString();
            String opacity = "#FF";
            String hexColor = String.format(
                    opacity + "%06X", (0xFFFFFF & cellidLAC.hashCode()));
            eventTimeBar.setBackgroundColor(Color.parseColor(hexColor));

            // Display the date as only hours and minutes
            DateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());
            eventTime.setText(df.format(new java.util.Date(databaseEntries.get(i).getSessionStart() * 1000)));

            String eventTextBuilder = databaseEntries.get(i).toString();
            eventText.setText(eventTextBuilder);

            // Set the length of the time intervals with a sigmoid based function
            if (i != 0) {
                double secondDiff = (double) (databaseEntries.get(i).getSessionEnd() - databaseEntries.get(i).getSessionStart());
                durationHeight = (int) (100 + d + ((a - d) / (1 + Math.pow((secondDiff / c), b))));
            }
            eventListLayout.addView(eventView, ViewGroup.LayoutParams.WRAP_CONTENT, durationHeight);
        }

    }
}