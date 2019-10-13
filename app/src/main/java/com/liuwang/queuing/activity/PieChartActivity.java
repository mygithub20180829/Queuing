package com.liuwang.queuing.activity;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.gson.Gson;
import com.liuwang.queuing.R;
import com.liuwang.queuing.util.DataCache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.com.newland.nle_sdk.responseEntity.SensorDataInfoDTO;
import cn.com.newland.nle_sdk.responseEntity.base.BaseResponseEntity;
import cn.com.newland.nle_sdk.util.NCallBack;
import cn.com.newland.nle_sdk.util.NetWorkBusiness;
import retrofit2.Call;
import retrofit2.Response;

public class PieChartActivity extends AppCompatActivity {
    private PieChart pieChart;
    private ArrayList<PieEntry> entries=new ArrayList<>();
    private NetWorkBusiness netWorkBusiness;
    private Context mContext;
    private List<Integer> count=new ArrayList<>();
    private List<String> recordTime=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pie_chart);

        mContext=this;
        netWorkBusiness = new NetWorkBusiness(DataCache.getAccessToken(getApplicationContext()),
                DataCache.getBaseUrl(getApplicationContext()));

        pieChart=findViewById(R.id.pieChart);
        getHistorySensorDataGroup();
    }

    public void getHistorySensorDataGroup() {
        netWorkBusiness.getSensorDataGrouping("43904", "number_down", "1", "MAX",
                "2019-09-28 20:15:30", "", new NCallBack<BaseResponseEntity<SensorDataInfoDTO>>(mContext) {
                    @Override
                    protected void onResponse(BaseResponseEntity<SensorDataInfoDTO> response) {
                        Toast.makeText(mContext,"我是响应",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(Call<BaseResponseEntity<SensorDataInfoDTO>> call, Response<BaseResponseEntity<SensorDataInfoDTO>> response) {
                        BaseResponseEntity baseResponseEntity = response.body();
                        if (baseResponseEntity != null) {
                            final Gson gson = new Gson();
                            JSONObject jsonObject = null;
                            String msg = gson.toJson(baseResponseEntity);
                            Log.d("msg_________________", "onResponse________msg:"+msg);
                            try {
                                jsonObject = new JSONObject(msg);   //解析数据
                                JSONObject resultObj = (JSONObject) jsonObject.get("ResultObj");
                                Log.d("MPAndroidChartActivity", "___onResponse________resultObj:" + resultObj);
                                JSONArray dataPoints = resultObj.optJSONArray("DataPoints");
                                Log.d("MPAndroidChartActivity", "___onResponse________dataPoints:" + dataPoints);
                                for (int i = 0; i < dataPoints.length(); i++) {
                                    JSONObject jsonObject1 = dataPoints.optJSONObject(i);
                                    Log.d("MPAndroidChartActivity", "___onResponse________jsonObject1:" + jsonObject1);
                                    JSONArray pointDTO = jsonObject1.optJSONArray("PointDTO");
                                    Log.d("MPAndroidChartActivity", "___onResponse________pointDTO:" + pointDTO);
                                    for (int j = 0; j < pointDTO.length(); j++) {
                                        JSONObject jsonObject2 = pointDTO.optJSONObject(j);
                                        Log.d("MPAndroidChartActivity", "onResponse________jsonObject2:" + jsonObject2);
                                        int value = jsonObject2.optInt("Value");
                                        String time=jsonObject2.optString("RecordTime");
                                        count.add(value);
                                        String Time=time.substring(11);
                                        recordTime.add(Time);
                                        Log.d("MPAndroidChartActivity", "___value:接收到的Value的数据：" + value);
                                        Log.d("TAG_TAG_TAG_TAG", "接收到的RecordTime的数据：" + time);
                                    }
                                }
                                Collections.reverse(count);
                                Log.d("-----------------", "count集合中的数据：" + count);
                                Log.d("-----------------", "recordTime集合中的数据：" + recordTime);
                                initPieChart();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponseEntity<SensorDataInfoDTO>> call, Throwable t) {
                        Toast.makeText(mContext,"出错啦",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void initPieChart(){
        pieChart.setNoDataText("暂时没有数据");

        entries.clear();
//        entries.add(new PieEntry(10,"0-10"));
//        entries.add(new PieEntry(12,"10-20"));
//        entries.add(new PieEntry(17,"20-30"));
//        entries.add(new PieEntry(20,"30-40"));
//        entries.add(new PieEntry(22,"40-50"));
//        entries.add(new PieEntry(25,"50-60"));

        for (int i=0;i<count.size();i++){
            entries.add(new PieEntry(count.get(i),recordTime.get(i)));
        }

        pieChart.setUsePercentValues(false);//设置是否显示数据实体(百分比，true:以下属性才有意义)
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5,5,5,5);//饼状图上下左右边距
        pieChart.setDragDecelerationFrictionCoef(0.95f);//设置PieChart图表转动阻力摩擦系数[0,1]
        pieChart.setCenterText("饼状图");//设置PieChart内部圆的内容
        pieChart.setDrawHoleEnabled(true);//是否显示PieChart内部圆环
        pieChart.setHoleColor(Color.WHITE);//当上面一行代码为true的时候，此行代码才有意义
        pieChart.setTransparentCircleColor(Color.WHITE);//设置PieChart内部透明圆与内部圆间距(31f-28f)填充颜色
        pieChart.setTransparentCircleAlpha(110);//设置PieChart内部透明圆与内部圆间距(31f-28f)透明度[0~255]数值越小越透明
        pieChart.setHoleRadius(28f);//设置PieChart内部圆的半径
        pieChart.setTransparentCircleRadius(31f);//设置PieChart内部透明圆的半径
        pieChart.setDrawCenterText(true);//是否绘制PieChart内部中心文本（true：下面属性才有意义）
        pieChart.setRotationAngle(0);//设置pieChart图表起始角度
        // enable rotation of the chart by touch
        pieChart.setRotationEnabled(true);//设置pieChart图表是否可以手动旋转
        pieChart.setHighlightPerTapEnabled(true);//设置piecahrt图表点击Item高亮是否可用

        pieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);//Easing.EaseInOutQuad
        // mChart.spin(2000, 0, 360);

        // 获取pieCahrt图列
        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f); //设置图例实体之间延X轴的间距（setOrientation = HORIZONTAL有效）
        l.setYEntrySpace(0f); //设置图例实体之间延Y轴的间距（setOrientation = VERTICAL 有效）
        l.setYOffset(0f);//设置比例块Y轴偏移量

        // entry label styling
        pieChart.setEntryLabelColor(Color.BLACK);//设置pieChart图表文本字体颜色
//        mChart.setEntryLabelTypeface(mTfRegular);//设置pieChart图表文本字体样式
        pieChart.setEntryLabelTextSize(12f);//设置pieChart图表文本字体大小

        PieDataSet dataSet = new PieDataSet(entries, "数据说明");

        dataSet.setDrawIcons(false);

        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<Integer>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);/*WHITE*/
//        data.setValueTypeface(mTfLight);
        pieChart.setData(data);

        // undo all highlights
        pieChart.highlightValues(null);

        pieChart.invalidate();
    }
}
