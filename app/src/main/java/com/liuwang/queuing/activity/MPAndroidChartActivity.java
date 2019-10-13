package com.liuwang.queuing.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.gson.Gson;
import com.liuwang.queuing.MyMarkerView;
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

public class MPAndroidChartActivity extends AppCompatActivity {
    LineChart mLineChart;
    private Context mContext;
    private List<Integer> count=new ArrayList<>();
    private List<String> recordTime=new ArrayList<>();
    private NetWorkBusiness netWorkBusiness;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mpandroid_chart);

        mContext=this;

        netWorkBusiness = new NetWorkBusiness(DataCache.getAccessToken(getApplicationContext()),
                DataCache.getBaseUrl(getApplicationContext()));

//        description.setText("时间");
//        description.setTextColor(Color.RED);
//        description.setPosition(50,50);
//        description.setTypeface();
        mLineChart=findViewById(R.id.mLineChar);
//        mLineChart.invalidate();//重新刷新屏幕
//        mLineChart.setLogEnabled(false);//设为真将激活图表项目中的Logcat输出
//        mLineChart.setBackgroundColor(Color.GRAY);//设置背景颜色这将覆盖整个图表项目视图。另外，一个背景颜色能被设置在.xml的布局文件中
//        mLineChart.setDescription(description);//设置一个描述文本出现在图表的右下角。

//
//        for (int i=0;i<10;i++){
//            count.add(i);
//        }


        //解决滑动冲突
//        mLineChart.setOnTouchListener(new View.OnTouchListener()
//        {
//            @Override
//            public boolean onTouch(View v, MotionEvent event)
//            {
//                switch (event.getAction())
//                {
//                    case MotionEvent.ACTION_DOWN:
//                    {
//                        scrollview.requestDisallowInterceptTouchEvent(true);
//                        break;
//                    }
//                    case MotionEvent.ACTION_CANCEL:
//                    case MotionEvent.ACTION_UP:
//                    {
//                        scrollview.requestDisallowInterceptTouchEvent(false);
//                        break;
//                    }
//                }
//                return false;
//            }
//        });

        getHistorySensorDataGroup();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //要延时的程序
                getHistorySensorDataGroup();
            }
        },5000); //5000为毫秒单位

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
                                initLineChart(count);

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

    private void initLineChart(final List<Integer> list){
        //不显示边界
        mLineChart.setDrawBorders(false);
        //设置折线上的数据
        List<Entry> entries=new ArrayList<>();
        for (int i=0;i<list.size();i++){
            entries.add(new Entry(i,list.get(i)));
        }
        //一个LineDataSet就是一条线
        LineDataSet lineDataSet=new LineDataSet(entries,"排队人数图");
        //线颜色
        lineDataSet.setColor(Color.parseColor("#32CD32"));
        //线宽度
        lineDataSet.setLineWidth(1.6f);
        //不显示圆点
        lineDataSet.setDrawCircles(false);
        //线条平滑
        lineDataSet.setMode(LineDataSet.Mode.LINEAR);
        //设置折线图填充
//        lineDataSet.setDrawFilled(true);
        LineData data=new LineData(lineDataSet);
        //无数据时显示的文字
        mLineChart.setNoDataText("暂无数据");
        //折线图显示数值
        data.setDrawValues(true);
        //得到X轴
        XAxis xAxis=mLineChart.getXAxis();
        //设置X轴的位置（默认在上方）
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //设置X轴坐标之间的最小间隔
        xAxis.setGranularity(1f);
        //设置X轴的刻度数量，第二个参数为true,将会画出明确数量（带有小数点），但是可能值导致不均匀，默认（6，false）
//        xAxis.setLabelCount(list.size(),true);/*list.size()/6,false*/
        //设置X轴的值（最小值、最大值、然后会根据设置的刻度数量自动分配刻度显示）
//        xAxis.setAxisMinimum(0f);
//        xAxis.setAxisMaximum(30);/*list.size()*/
        //不显示网格线
        xAxis.setDrawGridLines(false);
        //标签倾斜
        xAxis.setLabelRotationAngle(45);
        //设置X轴值为字符串
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
//                int Ivalue=(int)value;
//                String iValue = Ivalue+"";

//                flag-=2;
//                CharSequence format= DateFormat.format("HH:mm",
//                        System.currentTimeMillis()-(long)(list.size()-flag)*24*60*60);
//                Toast.makeText(mContext,""+Ivalue,Toast.LENGTH_SHORT).show();
                String iValue=recordTime.get((int)value);
                return iValue;/*recordTime.get(Ivalue<9?Ivalue:0)*/
            }
        });
        //得到Y轴
        YAxis yAxis = mLineChart.getAxisLeft();
        YAxis rightYAxis = mLineChart.getAxisRight();
        //设置Y轴是否显示
        rightYAxis.setEnabled(false); //右侧Y轴不显示
        //设置y轴坐标之间的最小间隔
        //不显示网格线
        yAxis.setDrawGridLines(false);
        //设置Y轴坐标之间的最小间隔
        yAxis.setGranularity(1);
        //设置y轴的刻度数量
        //+2：最大值n就有n+1个刻度，在加上y轴多一个单位长度，为了好看，so+2
        yAxis.setLabelCount(Collections.max(list) + 2, false);
        //设置从Y轴值
        yAxis.setAxisMinimum(0f);
        //+1:y轴多一个单位长度，为了好看
        yAxis.setAxisMaximum(Collections.max(list) + 1);

        //y轴
        yAxis.setValueFormatter(new IAxisValueFormatter()
        {
            @Override
            public String getFormattedValue(float value, AxisBase axis)
            {
                int IValue = (int) value;
                return String.valueOf(IValue);
            }
        });
        //图例：得到Lengend
        Legend legend = mLineChart.getLegend();
        //隐藏Lengend
        legend.setEnabled(false);
        //隐藏描述
        Description description = new Description();
        description.setEnabled(false);
        mLineChart.setDescription(description);
        //折线图点的标记
        MyMarkerView mv = new MyMarkerView(this);
        mLineChart.setMarker(mv);
        //设置数据
        mLineChart.setData(data);
        //设置X轴最多10个数据
//        mLineChart.setVisibleXRangeMaximum(30f);
        mLineChart.setVisibleXRangeMinimum(10f);
        //图标刷新
        mLineChart.invalidate();
    }

}
