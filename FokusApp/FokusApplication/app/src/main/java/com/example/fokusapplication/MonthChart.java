package com.example.fokusapplication;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class MonthChart extends Fragment  {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.statistics_month_chart, container, false);

        BarChart chart = v.findViewById(R.id.chart);
        StatisticsActivity.setData(chart, 31, 16);
        XAxis xAxis = chart.getXAxis();
        final String[] daysOfMonth = new String[31];
        for(int i = 1; i < 31; i++)
        {
            if (i % 3 != 0)
            {
                daysOfMonth[i-1] = Integer.toString(i);
            }
            else
            {
                daysOfMonth[i-1] = "";
            }
        }
        ValueFormatter formatterDaysOfMonth = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return daysOfMonth[((int) value)-1];
            }
        };
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(formatterDaysOfMonth);
        return v;
    }

    public static MonthChart newInstance() {
        MonthChart f = new MonthChart();
        return f;
    }
}