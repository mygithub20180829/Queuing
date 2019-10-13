package com.liuwang.queuing.activity;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.google.gson.Gson;
import com.liuwang.queuing.BarChartActivityOnlyUtils;
import com.liuwang.queuing.BarChartActivityUtils;
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

public class BarChartActivity extends AppCompatActivity {
    private BarChart chart1,chart2;
    private NetWorkBusiness netWorkBusiness;
    private Context mContext;
    private List<Integer> count=new ArrayList<>();
    private List<String> recordTime=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_chart);

        mContext=this;
        netWorkBusiness = new NetWorkBusiness(DataCache.getAccessToken(getApplicationContext()),
                DataCache.getBaseUrl(getApplicationContext()));
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
                                initView();

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

    private void initView(){
        chart1=findViewById(R.id.chart1);
        chart2=findViewById(R.id.chart2);
        List<Float> x_number=new ArrayList<>();
        for (int i=0;i<10;i++){
            x_number.add((float)i);
        }
        List<Float> y_value=new ArrayList<>();
        for (int i=0;i<count.size();i++){
            y_value.add((float) count.get(i));
        }
//        y_value.add((float)34);
//        y_value.add((float)74);
//        y_value.add((float)61);
//        y_value.add((float)19);
//        y_value.add((float)73);
//        y_value.add((float)23);
//        y_value.add((float)52);
//        y_value.add((float)83);
//        y_value.add((float)6);
//        y_value.add((float)47);

//        String[] x_value={"A","B","C","D","E","F","G","H","I"};
        String[] x_value=new String[recordTime.size()];
        for (int i=0;i<recordTime.size();i++){
            x_value[i]=recordTime.get(i);
        }
        BarChartActivityOnlyUtils.init(BarChartActivity.this,chart1,x_number,y_value,recordTime.size(),x_value);
        BarChartActivityUtils.init(BarChartActivity.this, chart2, x_number, y_value, recordTime.size(), x_value);
    }
}
