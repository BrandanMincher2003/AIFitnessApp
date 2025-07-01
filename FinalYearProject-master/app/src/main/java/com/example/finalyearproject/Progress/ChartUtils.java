package com.example.finalyearproject.Progress;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChartUtils {

    public static Bitmap createProgressChart(Context context, List<Entry> rawEntries, float goalValue, List<Date> dates) {
        LineChart chart = new LineChart(context);

        // Progress Line
        LineDataSet dataSet = new LineDataSet(rawEntries, "Progress");
        dataSet.setColor(Color.parseColor("#38B6FF"));
        dataSet.setCircleColor(Color.parseColor("#38B6FF"));
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.setDescription(null);
        chart.setDrawGridBackground(false);
        chart.setBackgroundColor(Color.WHITE);
        chart.getLegend().setEnabled(false);

        // X-Axis Setup
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(true);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setTextSize(12f);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(0);

        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.UK);
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return (index >= 0 && index < dates.size()) ? sdf.format(dates.get(index)) : "";
            }
        });

        xAxis.setAxisMinimum(-0.5f);
        xAxis.setAxisMaximum(rawEntries.size() - 0.5f);

        // Y-Axis Setup
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setTextColor(Color.BLACK);
        yAxis.setTextSize(12f);
        chart.getAxisRight().setEnabled(false);

        float firstValue = rawEntries.get(0).getY();
        float minValue = Math.min(goalValue, firstValue);
        float maxValue = Math.max(goalValue, firstValue);

        for (Entry e : rawEntries) {
            float v = e.getY();
            if (v < minValue) minValue = v;
            if (v > maxValue) maxValue = v;
        }

        float padding = Math.max(5f, (maxValue - minValue) * 0.15f);
        yAxis.setAxisMinimum(minValue - padding);
        yAxis.setAxisMaximum(maxValue + padding);

        // Green Dashed Target Line
        LimitLine targetLine = new LimitLine(goalValue, "Target Goal");
        targetLine.setLineColor(Color.parseColor("#66BB6A")); // Light Green
        targetLine.setLineWidth(2f);
        targetLine.enableDashedLine(10f, 10f, 0f);
        targetLine.setTextColor(Color.parseColor("#66BB6A")); // Light Green Text
        targetLine.setTextSize(12f);
        yAxis.removeAllLimitLines();
        yAxis.addLimitLine(targetLine);

        // Render chart to bitmap
        chart.layout(0, 0, 800, 400);
        chart.measure(
                View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY)
        );
        chart.layout(0, 0, chart.getMeasuredWidth(), chart.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(800, 400, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        chart.draw(canvas);

        return bitmap;
    }
}
