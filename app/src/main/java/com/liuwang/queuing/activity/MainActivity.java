package com.liuwang.queuing.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.liuwang.queuing.R;
import com.liuwang.queuing.util.DataCache;

import org.json.JSONException;
import org.json.JSONObject;

import cn.com.newland.nle_sdk.responseEntity.SensorInfo;
import cn.com.newland.nle_sdk.responseEntity.User;
import cn.com.newland.nle_sdk.responseEntity.base.BaseResponseEntity;
import cn.com.newland.nle_sdk.util.NCallBack;
import cn.com.newland.nle_sdk.util.NetWorkBusiness;
import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private Context mContext;
    private NetWorkBusiness mNetWorkBusiness;
    private TextView boolWork,waitingTime;
    private Button queueUp,MPAndroid,PieChartAndroid,histogramChart;
    private int upValue=0,downValue=0,all;
    private boolean flag=true;
    private Thread th=new Thread(new Mythread());

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what==1){
                getNumberDownSensorData();
                getBoolWorkSensorData();
            }
        }
    };

    private Handler mHandler=new Handler();
    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            //要做的事情
            Message msg=new Message();
            msg.what=1;
            handler.sendMessage(msg);
            handler.postDelayed(this,5000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mNetWorkBusiness = new NetWorkBusiness(DataCache.getAccessToken(getApplicationContext()),
                DataCache.getBaseUrl(getApplicationContext()));

        initView();
    }

    private void initView(){
        boolWork=findViewById(R.id.bool_work);
        MPAndroid=findViewById(R.id.MP_Android);
        PieChartAndroid=findViewById(R.id.PieChart_Android);
        histogramChart=findViewById(R.id.Histogram_Chart);
        waitingTime=findViewById(R.id.waiting_time);

        queueUp=findViewById(R.id.queuing_up);
        queueUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ChartActivity.class));
            }
        });
        MPAndroid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, MPAndroidChartActivity.class));
            }
        });
        PieChartAndroid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, PieChartActivity.class));
            }
        });
        histogramChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, BarChartActivity.class));
            }
        });
        th.start();
    }


    public void onChartClick(View v){
        startActivity(new Intent(MainActivity.this, HelloActivity.class));
    }

    private void getNumberDownSensorData(){
        final Gson gson = new Gson();
        mNetWorkBusiness.getSensor("43904", "number_down", new NCallBack<BaseResponseEntity<SensorInfo>>(mContext) {
            @Override
            protected void onResponse(BaseResponseEntity<SensorInfo> response) {

            }

            @Override
            public void onResponse(Call<BaseResponseEntity<SensorInfo>> call, Response<BaseResponseEntity<SensorInfo>> response) {
                BaseResponseEntity baseResponseEntity = response.body();
                try {
                    JSONObject jsonObject = new JSONObject(gson.toJson(baseResponseEntity));
                    JSONObject resultObj = (JSONObject) jsonObject.get("ResultObj");
                    int value =resultObj.getInt("Value");
                    downValue=value;
                    Log.d("value:", " "+value);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (baseResponseEntity != null) {
                    Log.d("gson.toJson", "(baseResponseEntity): "+gson.toJson(baseResponseEntity));
                } else {
                    Toast.makeText(mContext,"请求出错 : 请求参数不合法或者服务出错",Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<BaseResponseEntity<SensorInfo>> call, Throwable t) {
                Toast.makeText(mContext,"请求出错",Toast.LENGTH_SHORT).show();
                Log.d("onFailure：", "请求出错："+t.getMessage());
            }
        });
    }

    private void getBoolWorkSensorData(){
        final Gson gson = new Gson();
        mNetWorkBusiness.getSensor("43904", "number_up", new NCallBack<BaseResponseEntity<SensorInfo>>(mContext) {
            @Override
            protected void onResponse(BaseResponseEntity<SensorInfo> response) {

            }

            @Override
            public void onResponse(Call<BaseResponseEntity<SensorInfo>> call, Response<BaseResponseEntity<SensorInfo>> response) {
                BaseResponseEntity baseResponseEntity = response.body();
                try {
                    JSONObject jsonObject = new JSONObject(gson.toJson(baseResponseEntity));
                    JSONObject resultObj = (JSONObject) jsonObject.get("ResultObj");
                    int value =resultObj.getInt("Value");
                    upValue=value;
                    if (flag){
                        all=+upValue+downValue+1;
                        boolWork.setText("您当前是第"+(upValue+downValue+1)+"位");
                        flag=false;
                    }
                    waitingTime.setText("当前需要等待"+upValue*15+"分钟");
                    if (all-downValue<=3){
                        //创建对话框对象
                        AlertDialog alertDialog=new AlertDialog.Builder(MainActivity.this).create();
                        alertDialog.setIcon(R.mipmap.ic_launcher);
                        alertDialog.setTitle("警告：");
                        //设置要显示的内容
                        alertDialog.setMessage("在您前面排队的人数不足3人，请及时关注排队信息，避免错过叫号，点击取消号码将作废");
                        //添加确定按钮
                        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "是",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        handler.removeCallbacks(runnable);
                                    }
                                });
                        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //号码作废
                                        control("43904","number_up",upValue-1);
//                                        handlerFlag=false;
                                        handler.removeCallbacks(runnable);
                                    }
                                });
                        //添加取消号码
                        alertDialog.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (baseResponseEntity != null) {
                    Log.d("gson.toJson", "(baseResponseEntity): "+gson.toJson(baseResponseEntity));
                } else {
                    Toast.makeText(mContext,"请求出错 : 请求参数不合法或者服务出错",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponseEntity<SensorInfo>> call, Throwable t) {
                Toast.makeText(mContext,"请求出错",Toast.LENGTH_SHORT).show();
                Log.d("onFailure：", "请求出错："+t.getMessage());
            }
        });
    }

    private void control(String id,String apiTag,Object value){
        //设备id,标识符,值.
        mNetWorkBusiness.control(id, apiTag, value, new NCallBack<BaseResponseEntity>(MainActivity.this) {
            @Override
            protected void onResponse(BaseResponseEntity response) {

            }

            @Override
            public void onResponse(Call<BaseResponseEntity> call, Response<BaseResponseEntity> response) {
                BaseResponseEntity<User> baseResponseEntity = response.body();  //获得返回体
                if (baseResponseEntity==null){
                    Toast.makeText(MainActivity.this,"请求内容为空",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponseEntity> call, Throwable t) {
                Toast.makeText(MainActivity.this,"请求出错 " + t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }


    class Mythread implements Runnable{

        @Override
        public void run() {
            Looper.prepare();
            mHandler.postDelayed(runnable,3000);
        }
    }



}
