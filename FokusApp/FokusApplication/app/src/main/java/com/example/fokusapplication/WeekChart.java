package com.example.fokusapplication;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class WeekChart extends Fragment  {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.statistics_week_chart, container, false);

        BarChart chart = v.findViewById(R.id.chart);
        StatisticsActivity.setData(chart, 7, 16);
        XAxis xAxis = chart.getXAxis();
        final String[] days = new String[] { "Mo", "Tu", "We", "Th", "Fr", "Sa", "Su" };
        ValueFormatter formatterDays = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return days[((int) value)-1];
            }
        };
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(formatterDays);
        return v;
    }

    public static WeekChart newInstance() {
        WeekChart f = new WeekChart();
        return f;
    }
}