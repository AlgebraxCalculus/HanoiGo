package com.example.myapplication.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.example.myapplication.api.UserApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ProgressFragment extends Fragment {
    LinearLayout chipPoints, chipCheckpoints, chipRank;
    List<Float> dataPoints = new ArrayList<>();
    List<Float> dataCheckpoints = new ArrayList<>();
    List<Float> dataRank = new ArrayList<>();
    LineChart chart;
    TextView tvWeekPoints, tvWeekCheckpoints, tvWeekRank, tvPoints, tvCheckpoints, tvRank;
    ImageView imgTrend;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pers_progress, container, false);
        chipPoints = view.findViewById(R.id.chipPoints);
        chipCheckpoints = view.findViewById(R.id.chipCheckpoints);
        chipRank = view.findViewById(R.id.chipRank);
        chart = view.findViewById(R.id.lineChart);
        tvWeekPoints = view.findViewById(R.id.tvWeekPoints);
        tvWeekCheckpoints = view.findViewById(R.id.tvWeekCheckpoints);
        tvWeekRank = view.findViewById(R.id.tvWeekRank);
        tvPoints = view.findViewById(R.id.tvPoints);
        tvCheckpoints = view.findViewById(R.id.tvCheckpoints);
        tvRank = view.findViewById(R.id.tvRank);
        imgTrend = view.findViewById(R.id.imgTrend);

        chipPoints.setOnClickListener(v -> {
            setupLineChart(chart, dataPoints, "Points");
            chipPoints.getBackground().setTint(Color.parseColor("#021526"));
            chipCheckpoints.getBackground().setTint(Color.parseColor("#CCCCCC"));
            chipRank.getBackground().setTint(Color.parseColor("#CCCCCC"));
            tvPoints.setTextColor(Color.parseColor("#FFFFFF"));
            tvCheckpoints.setTextColor(Color.parseColor("#000000"));
            tvRank.setTextColor(Color.parseColor("#000000"));
        });
        chipCheckpoints.setOnClickListener(v -> {
            setupLineChart(chart, dataCheckpoints, "Checkpoints");
            chipPoints.getBackground().setTint(Color.parseColor("#CCCCCC"));
            chipCheckpoints.getBackground().setTint(Color.parseColor("#021526"));
            chipRank.getBackground().setTint(Color.parseColor("#CCCCCC"));
            tvPoints.setTextColor(Color.parseColor("#000000"));
            tvCheckpoints.setTextColor(Color.parseColor("#FFFFFF"));
            tvRank.setTextColor(Color.parseColor("#000000"));
        });
        chipRank.setOnClickListener(v -> {
            setupLineChart(chart, dataRank, "Rank");
            chipPoints.getBackground().setTint(Color.parseColor("#CCCCCC"));
            chipCheckpoints.getBackground().setTint(Color.parseColor("#CCCCCC"));
            chipRank.getBackground().setTint(Color.parseColor("#021526"));
            tvPoints.setTextColor(Color.parseColor("#000000"));
            tvCheckpoints.setTextColor(Color.parseColor("#000000"));
            tvRank.setTextColor(Color.parseColor("#FFFFFF"));
        });

        setupLineChart(chart, dataPoints, "Points");
        return view;
    }

    private void setupLineChart(LineChart chart, List<Float> data, String type) {
        // 1️⃣ Tạo danh sách Entry
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            entries.add(new Entry(i, data.get(i)));
        }

        // 2️⃣ Tạo LineDataSet
        LineDataSet dataSet = new LineDataSet(entries, type);
        dataSet.setColor(Color.parseColor("#00B0FF")); // màu đường
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.parseColor("#00B0FF"));
        dataSet.setCircleRadius(4f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#6600B0FF")); // màu tô dưới đường
        dataSet.setMode(LineDataSet.Mode.LINEAR);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        // 3️⃣ Tùy chỉnh trục X
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7,true);

        List<String> days = new ArrayList<>(List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"));
        String today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .getDayOfWeek().toString().substring(0, 3);
        today = today.substring(0, 1).toUpperCase() + today.substring(1).toLowerCase();
        while (!days.get(6).equalsIgnoreCase(today)) {
            days.add(days.get(0));
            days.remove(0);
        }

        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));

        float min = getMin(data);
        float max = getMax(data);
        float top = max, bot = min;

        //Set up minimum range for chart
        if(type.equals("Points") && max-min < 10){
            top = min+10;
        }else if(max-min < 2){
            top = min+2;
            if(type.equals("Rank") && max < 3){
                top = max+min-1;
                bot = max+min-3;
            }
        }

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(bot);
        leftAxis.setAxisMaximum(top + 0.01f);
        leftAxis.setLabelCount(3, true);
        leftAxis.setDrawGridLines(true);
        if (type.equals("Rank")) {
            leftAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    float displayValue = (max + min - Float.parseFloat(String.format(Locale.US, "%.1f", value)));

                    if (displayValue == Math.floor(displayValue)) {
                        return String.valueOf((int) displayValue);
                    } else {
                        return String.format(Locale.US, "%.1f", displayValue);
                    }
                }
            });
        } else {
            leftAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    if (Float.parseFloat(String.format(Locale.US, "%.1f", value)) == Math.floor(value)) {
                        return String.valueOf((int) value);
                    } else {
                        return String.format(Locale.US, "%.1f", value);
                    }
                }
            });
        }

        // 5️⃣ Tùy chỉnh chung
        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.animateY(500);

        chart.setViewPortOffsets(40f, 20f, 30f, 30f); // left, top, right, bottom (tùy chỉnh cho đẹp)
        chart.setExtraLeftOffset(10f);
        chart.setExtraRightOffset(10f);
        chart.setExtraTopOffset(5f);
        chart.setExtraBottomOffset(5f);
    }

    private float getMin(List<Float> list) {
        float min = Float.MAX_VALUE;
        for (float v : list) min = Math.min(min, v);
        return min;
    }

    private float getMax(List<Float> list) {
        float max = Float.MIN_VALUE;
        for (float v : list) max = Math.max(max, v);
        return max;
    }

    public void setupChartData(String jwtToken) {
        List<Float> points = new ArrayList<>();
        List<Float> checkpoints = new ArrayList<>();
        List<Float> rank = new ArrayList<>();
        UserApi.getChartData(jwtToken, getContext(), new UserApi.UserApiCallback() {
            @Override
            public void onSuccess(ArrayList<JSONObject> dataList) {
                for(JSONObject x:dataList){
                    try {
                        points.add(Float.parseFloat(x.getString("points")));
                        checkpoints.add(Float.parseFloat(x.getString("checkpoints")));
                        rank.add(Float.parseFloat(x.getString("rank")));
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
                requireActivity().runOnUiThread(() -> {
                    dataPoints = new ArrayList<>(points);
                    dataCheckpoints = new ArrayList<>(checkpoints);
                    dataRank = new ArrayList<>(rank);
                    float minRank = getMin(dataRank);
                    float maxRank = getMax(dataRank);
                    for(int i = 0;i<dataRank.size();i++){
                        float num = minRank+maxRank-dataRank.get(i);
                        dataRank.set(i,num);
                    }

                    int gapPoints = dataPoints.get(6).intValue() - dataPoints.get(0).intValue();
                    int gapCheckpoints = dataCheckpoints.get(6).intValue() - dataCheckpoints.get(0).intValue();
                    int gapRank = dataRank.get(6).intValue() - dataRank.get(0).intValue();
                    tvWeekPoints.setText(String.valueOf(gapPoints));
                    tvWeekCheckpoints.setText(String.valueOf(gapCheckpoints));
                    imgTrend.setImageResource(gapRank >= 0 ? R.drawable.ic_increase : R.drawable.ic_decrease);
                    tvWeekRank.setText(String.valueOf(Math.abs(gapRank)));

                    setupLineChart(chart, dataPoints, "Points");
                });
            }

            @Override
            public void onSuccess(JSONObject userObj) {

            }

            @Override
            public void onFailure(String errorMessage) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "fetch chart data failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
}
