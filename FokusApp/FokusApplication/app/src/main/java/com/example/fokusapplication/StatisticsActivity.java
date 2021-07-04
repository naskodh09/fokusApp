package com.example.fokusapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.lang.reflect.Array;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity
{
    private BarChart chart1;
    private BarChart chart2;
    private BarChart chart3;
    private static final int NUM_PAGES = 3;
    //The pager widget, which handles animation and allows swiping horizontally to access previous and next wizard steps.
    public static ViewPager2 viewPager;
    // The pager adapter, which provides the pages to the view pager widget.
    private FragmentStateAdapter pagerAdapter;
    // Array of strings FOR TABS TITLES
    private String[] titles = new String[]{"Week", "Month", "Year"};
    // tab titles

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        viewPager = findViewById(R.id.viewPager);
        pagerAdapter = new MyPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        //inflating tab layout
        TabLayout tabLayout =( TabLayout) findViewById(R.id.tabLayout);
        //displaying tabs
        new TabLayoutMediator(tabLayout, viewPager,(tab, position) -> tab.setText(titles[position])).attach();


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.statistics);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
            {
                switch (menuItem.getItemId())
                {
                    case R.id.statistics:
                        return true;

                    case R.id.schedule:
                        startActivity(new Intent(getApplicationContext(), CalendarActivity.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.timer:
                        startActivity(new Intent(getApplicationContext(), TimerActivity.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.notes:
                        startActivity(new Intent(getApplicationContext(), NotesActivity.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.settings:
                        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
    }

    private class MyPagerAdapter extends FragmentStateAdapter {

        public MyPagerAdapter(FragmentActivity fa) {
            super(fa);
        }


        @Override
        public Fragment createFragment(int pos) {
            switch (pos) {
                case 0: {
                    return WeekChart.newInstance();
                }
                case 1: {

                    return MonthChart.newInstance();
                }
                case 2: {
                    return YearChart.newInstance();
                }
                default:
                    return WeekChart.newInstance();
            }
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }


    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.d
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    public static void setData(BarChart chart, int count, float range){
        float start = 1f;
        float max = 0;

        //customizations for bars
        chart.setDrawValueAboveBar(true);
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        YAxis left = chart.getAxisLeft();
        YAxis right = chart.getAxisRight();
        left.setDrawGridLines(true);
        left.setDrawLabels(false);
        left.setLabelCount(3);
        right.setLabelCount(3);
        left.setDrawAxisLine(false);
        right.setDrawAxisLine(false);

        ArrayList<BarEntry> values = new ArrayList<>();

        for (int i = (int) start; i < start + count; i++) {
            float val = (float) ((Math.random()*range) + 1);
            values.add(new BarEntry(i, val));
            if (val > max)
            {
                max = val;
            }
        }

        int maxRound = (int) Math.ceil(max);
        maxRound--;
        String maxString = Integer.toString(maxRound);
        float length = maxString.length();
        int limitTenth = (int) Math.pow(10f, length-1);
        int limit = limitTenth;
        while (limit < max)
        {
            limit += limitTenth;
        }
        right.setAxisMaximum(limit);
        left.setAxisMaximum(limit);
        right.setAxisMinimum(0);
        left.setAxisMinimum(0);

        BarDataSet set1;

        set1 = new BarDataSet(values, null);
        set1.setColors(ColorTemplate.rgb("386cb8"));
        set1.setDrawIcons(false);
        set1.setDrawValues(false);


        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);
        data.setBarWidth(0.7f);

        chart.setData(data);
        chart.setFitBars(false);
    }
}