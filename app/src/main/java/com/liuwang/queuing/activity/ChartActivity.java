package com.liuwang.queuing.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.liuwang.queuing.R;
import com.liuwang.queuing.util.DataCache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.com.newland.nle_sdk.responseEntity.SensorDataPageDTO;
import cn.com.newland.nle_sdk.responseEntity.base.BaseResponseEntity;
import cn.com.newland.nle_sdk.util.NCallBack;
import cn.com.newland.nle_sdk.util.NetWorkBusiness;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import retrofit2.Call;
import retrofit2.Response;

public class ChartActivity extends AppCompatActivity {
    private LineChartView lineChart;
    private Context mContext;
    private List<Integer> count=new ArrayList<>();
    private List<String> countTime=new ArrayList<>();

    private String[] date={"0","3","6","9","12","15","18","22","25","28","30"};
    private List<PointValue> mPointValues = new ArrayList<>();//X轴对应的Y值
    private List<AxisValue> mAxisXValues = new ArrayList<>();//X轴的值
    private NetWorkBusiness netWorkBusiness;
//    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private Timer timer;

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what==0){
                getHistorySensorData();
                count.clear();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        mContext = this;
        netWorkBusiness = new NetWorkBusiness(DataCache.getAccessToken(getApplicationContext()),
                DataCache.getBaseUrl(getApplicationContext()));
        lineChart=findViewById(R.id.line_chart);

        //只查询一次
//        getHistorySensorData();

        //每5秒更新一次数据
        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message message=new Message();
                message.what=0;
                mPointValues.clear();//解决重新画图残留的问题
                mAxisXValues.clear();
                mHandler.sendMessage(message);
            }
        },0,15000);
    }

    /**
     * 设置X轴的显示
     */
    private void getAxisXLables() {
        for (int i=0;i<countTime.size();i++){
            //为每个x轴的标注填充数据
            mAxisXValues.add(new AxisValue(i).setLabel(countTime.get(i)));//date[i]
        }
    }

    /**
     * 图表每个点的显示
     */
    private void getAxisPoints() {
        for (int i=0;i<count.size()/*10*/;i++){
            mPointValues.add(new PointValue(i, count.get(i)));
        }
    }

    private void initLineChart() {
        Line line=new Line(mPointValues).setColor(Color.parseColor("#FFCD41"));//折线的颜色（橙色）
        List<Line> lines=new ArrayList<>();
        /**
         * 折线图上每个数据点的形状  这里是圆形
         * （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
         */
        line.setShape(ValueShape.CIRCLE);
        line.setCubic(false);//曲线是否平滑，即是曲线还是折线
        line.setFilled(false);//是否填充曲线的面积
        line.setHasLabels(true);//曲线的数据坐标是否加上备注
//      line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
        line.setHasLines(true);//是否用线显示。如果为false 则没有曲线只有点显示
        line.setHasPoints(true);//是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);

        //坐标轴
        Axis axisX = new Axis(); //X轴
        axisX.setHasTiltedLabels(true);  //X坐标轴字体是斜的显示还是直的，true是斜的显示
        axisX.setTextColor(Color.GRAY);  //设置字体颜色
        //axisX.setName("date");  //表格名称
        axisX.setTextSize(10);//设置字体大小
        axisX.setMaxLabelChars(3); //最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisXValues.length
        axisX.setValues(mAxisXValues);  //填充X轴的坐标名称
        data.setAxisXBottom(axisX); //x 轴在底部
        //data.setAxisXTop(axisX);  //x 轴在顶部
        axisX.setHasLines(true); //x 轴分割线

        // Y轴是根据数据的大小自动设置Y轴上限(在下面我会给出固定Y轴数据个数的解决方案)
        Axis axisY = new Axis();  //Y轴
//        axisY.setName("");//y轴标注
        axisY.setTextSize(10);//设置字体大小
        data.setAxisYLeft(axisY);  //Y轴设置在左边
        axisY.setMaxLabelChars(10);
        //data.setAxisYRight(axisY);  //y轴设置在右边


        //设置行为属性，支持缩放、滑动以及平移
        lineChart.setInteractive(true);
        lineChart.setZoomType(ZoomType.HORIZONTAL);
        lineChart.setMaxZoom((float) 2);//最大方法比例
        lineChart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        lineChart.setLineChartData(data);
        lineChart.setVisibility(View.VISIBLE);
        /**注：下面的7，10只是代表一个数字去类比而已
         * 当时是为了解决X轴固定数据个数。见（http://forum.xda-developers.com/tools/programming/library-hellocharts-charting-library-t2904456/page2）;
         */
        Viewport v = new Viewport(lineChart.getMaximumViewport());
        v.left = 0;
        v.right = 10;//7
        lineChart.setCurrentViewport(v);
    }

    public void getHistorySensorData() {
        netWorkBusiness.getSensorData("43904", "number_down", "3", "30",
                "", "", "DESC", "", "", new NCallBack<BaseResponseEntity<SensorDataPageDTO>>(mContext) {
                    @Override
                    protected void onResponse(BaseResponseEntity<SensorDataPageDTO> response) {

                    }

                    @Override
                    public void onResponse(Call<BaseResponseEntity<SensorDataPageDTO>> call, Response<BaseResponseEntity<SensorDataPageDTO>> response) {
                        BaseResponseEntity baseResponseEntity=response.body();
                        if (baseResponseEntity!=null){
                            final Gson gson=new Gson();
                            JSONObject jsonObject=null;
                            String msg=gson.toJson(baseResponseEntity);
//                            Log.d("msg", "onResponse________msg:"+msg);
                            try {
                                jsonObject = new JSONObject(msg);   //解析数据
                                JSONObject resultObj = (JSONObject) jsonObject.get("ResultObj");
                                Log.d("resultObj", "onResponse________resultObj:"+resultObj);
                                JSONArray dataPoints = resultObj.optJSONArray("DataPoints");
                                Log.d("dataPoints", "onResponse________dataPoints:"+dataPoints);
                                for (int i=0;i<dataPoints.length();i++){
                                    JSONObject jsonObject1 = dataPoints.optJSONObject(i);
                                    Log.d("jsonObject1", "onResponse________jsonObject1:"+jsonObject1);
                                    JSONArray pointDTO = jsonObject1.optJSONArray("PointDTO");
                                    Log.d("pointDTO", "onResponse________pointDTO:"+pointDTO);
                                    for (int j=0;j<pointDTO.length();j++){
                                        JSONObject jsonObject2 = pointDTO.optJSONObject(j);
                                        Log.d("jsonObject2", "onResponse________jsonObject2:"+jsonObject2);
                                        int value = jsonObject2.optInt("Value");
                                        String recordTime=jsonObject2.optString("RecordTime");
                                        count.add(value);
                                        countTime.add(recordTime);
                                        Log.d("-----------------", "接收到的Value的数据："+value);
                                    }
                                }
                                Collections.reverse(count);
                                Collections.reverse(countTime);
                                Log.d("-----------------", "count集合中的数据："+count);
                                getAxisXLables();//获取x轴的标注
                                getAxisPoints();//获取坐标点
                                initLineChart();//初始化

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponseEntity<SensorDataPageDTO>> call, Throwable t) {
                        Toast.makeText(ChartActivity.this,"出错了",Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
