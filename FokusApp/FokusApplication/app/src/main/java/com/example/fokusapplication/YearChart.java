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

public class YearChart extends Fragment  {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.statistics_year_chart, container, false);

        BarChart chart = v.findViewById(R.id.chart);
        StatisticsActivity.setData(chart, 12, 200);
        XAxis xAxis = chart.getXAxis();
        final String[] months = new String[] { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
        ValueFormatter formatterMonths = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return months[((int) value)-1];
            }
        };
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(formatterMonths);
        return v;
    }

    public static YearChart newInstance() {
        YearChart f = new YearChart();
        return f;
    }
}