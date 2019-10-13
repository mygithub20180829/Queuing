package com.liuwang.queuing.activity;

import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import com.liuwang.queuing.MyLineChart;
import com.liuwang.queuing.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;


/**
 * 简单折线线条的绘制
 */
public class HelloActivity extends Activity {
    private LineChartView chart,lineChartView;        //显示线条的自定义View
    private LineChartData data,lineChartData;          // 折线图封装的数据类

    private List<AxisValue> values=new ArrayList<>();
//    private List<Line> lines;
    private List<Line> linesList;
    private List<PointValue> pointValueList;
    private List<PointValue> points;
    private int position = 0;
    private Timer timer;
//    private boolean isFinish = true;
    private Axis axisY, axisX;
    private Random random = new Random();

    private int numberOfLines = 3;         //线条的数量
    private int maxNumberOfLines = 4;     //最大的线条数据
    private int numberOfPoints = 12;     //点的数量

    float[][] randomNumbersTab = new float[maxNumberOfLines][numberOfPoints]; //二维数组，线的数量和点的数量

    private boolean hasAxes = true;       //是否有轴，x和y轴
    private boolean hasAxesNames = true;   //是否有轴的名字
    private boolean hasLines = true;       //是否有线（点和点连接的线）
    private boolean hasPoints = true;       //是否有点（每个值的点）
    private ValueShape shape = ValueShape.CIRCLE;    //点显示的形式，圆形，正方向，菱形
    private boolean isFilled = false;                //是否是填充
    private boolean hasLabels = false;               //每个点是否有名字
    private boolean isCubic = false;                 //是否是立方的，线条是直线还是弧线
    private boolean hasLabelForSelected = false;       //每个点是否可以选择（点击效果）
    private boolean pointsHaveDifferentColor;           //线条的颜色变换
    private boolean hasGradientToTransparent = false;      //是否有梯度的透明


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);

        /*动态图代码*/
        lineChartView = findViewById(R.id.dynamic_chart);
        initDynamicView();
        timer = new Timer();

        /*静态图代码*/
        initView();
        initData();
        initEvent();
    }


    @Override
    protected void onResume() {
        super.onResume();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //实时添加新的点
                PointValue value1 = new PointValue(position * 2, random.nextInt(150));
//                value1.setLabel("00:00");
                pointValueList.add(value1);

                float x = value1.getX();
                //根据新的点的集合画出新的线
                Line line = new Line(pointValueList);
                line.setColor(Color.RED);
                line.setShape(ValueShape.CIRCLE);
                line.setHasLabels(true);
                line.setCubic(true);//曲线是否平滑，即是曲线还是折线

                linesList.clear();
                linesList.add(line);
                lineChartData = initDatas(linesList);
                lineChartView.setLineChartData(lineChartData);
                //根据点的横坐实时变幻坐标的视图范围
                Viewport port;
                if (x > 50) {
                    port = initViewPort(x - 50, x);
                } else {
                    port = initViewPort(0, 50);
                }
                lineChartView.setCurrentViewport(port);//当前窗口

                Viewport maPort = initMaxViewPort(x);
                lineChartView.setMaximumViewport(maPort);//最大窗口
                position++;
            }
        }, 1000, 1000);
    }

    private void initDynamicView() {
        pointValueList = new ArrayList<>();
        linesList = new ArrayList<>();
        //初始化坐标轴
        axisY = new Axis();
        //添加坐标轴的名称
        axisY.setLineColor(Color.parseColor("#aab2bd"));
        axisY.setTextColor(Color.parseColor("#000000"));
        axisX = new Axis();
        axisX.setLineColor(Color.parseColor("#aab2bd"));
        axisX.setTextColor(Color.BLACK);
//        axisX.setValues(values);
        lineChartData = initDatas(null);
        lineChartView.setLineChartData(lineChartData);

        Viewport port = initViewPort(0, 50);
        lineChartView.setCurrentViewportWithAnimation(port);
        lineChartView.setInteractive(false);
        lineChartView.setScrollEnabled(true);
        lineChartView.setValueTouchEnabled(true);
        lineChartView.setFocusableInTouchMode(true);
        lineChartView.setViewportCalculationEnabled(false);
        lineChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        lineChartView.startDataAnimation();
        points = new ArrayList<>();
    }


    private LineChartData initDatas(List<Line> lines) {
        LineChartData data = new LineChartData(lines);
        data.setAxisYLeft(axisY);
        data.setAxisXBottom(axisX);
        Log.d("TAG:", "initDatas: 一下是data的数据"+data);
        return data;
    }

    /**
     * 当前显示区域
     *
     * @param left
     * @param right
     * @return
     */
    private Viewport initViewPort(float left, float right) {
        Viewport port = new Viewport();
        port.top = 150;
        port.bottom = 0;
        port.left = left;
        port.right = right;
        return port;
    }

    /**
     * 最大显示区域
     *
     * @param right
     * @return
     */
    private Viewport initMaxViewPort(float right) {
        Viewport port = new Viewport();
        port.top = 150;
        port.bottom = 0;
        port.left = 0;
        port.right = right + 10;//X轴上数据的显示
        return port;
    }


    private void initView() {
        //实例化
        chart = findViewById(R.id.chart);
    }

    private void initData() {
        generateValues();   //设置四条线的值数据
        generateData();    //设置数据

        // Disable viewport recalculations, see toggleCubic() method for more info.
        chart.setViewportCalculationEnabled(false);

        chart.setZoomType(ZoomType.HORIZONTAL);//设置线条可以水平方向收缩，默认是全方位缩放
        resetViewport();   //设置折线图的显示大小
    }

    private void initEvent() {
        chart.setOnValueTouchListener(new ValueTouchListener());

    }

    /**
     * 图像显示大小
     */
    private void resetViewport() {
        // Reset viewport height range to (0,100)
        final Viewport v = new Viewport(chart.getMaximumViewport());
        v.bottom = 0;
        v.top = 100;
        v.left = 0;
        v.right = numberOfPoints - 1;
        chart.setMaximumViewport(v);
        chart.setCurrentViewport(v);
    }

    /**
     * 设置四条线条的数据
     */
    private void generateValues() {
        for (int i = 0; i < maxNumberOfLines; ++i) {
            for (int j = 0; j < numberOfPoints; ++j) {
                randomNumbersTab[i][j] = (float) Math.random() * 100f;
            }
        }
    }

    /**
     * 配置数据
     */
    private void generateData() {
        //存放线条对象的集合
        List<Line> lines = new ArrayList<Line>();
        //把数据设置到线条上面去
        for (int i = 0; i < numberOfLines; ++i) {

            List<PointValue> values = new ArrayList<PointValue>();
            for (int j = 0; j < numberOfPoints; ++j) {
                values.add(new PointValue(j, randomNumbersTab[i][j]));
            }

            Line line = new Line(values);
            line.setColor(ChartUtils.COLORS[i]);
            line.setShape(shape);
            line.setCubic(isCubic);
            line.setFilled(isFilled);
            line.setHasLabels(hasLabels);
            line.setHasLabelsOnlyForSelected(hasLabelForSelected);
            line.setHasLines(hasLines);
            line.setHasPoints(hasPoints);
//            line.setHasGradientToTransparent(hasGradientToTransparent);
            if (pointsHaveDifferentColor) {
                line.setPointColor(ChartUtils.COLORS[(i + 1) % ChartUtils.COLORS.length]);
            }
            lines.add(line);
        }

        data = new LineChartData(lines);

        if (hasAxes) {
            Axis axisX = new Axis();
            Axis axisY = new Axis().setHasLines(true);
            if (hasAxesNames) {
                axisX.setTextColor(Color.BLACK);//设置x轴字体的颜色
                axisY.setTextColor(Color.BLACK);//设置y轴字体的颜色
                axisX.setName("Axis X");
                axisY.setName("Axis Y");
            }
            data.setAxisXBottom(axisX);
            data.setAxisYLeft(axisY);
        } else {
            data.setAxisXBottom(null);
            data.setAxisYLeft(null);
        }

        data.setBaseValue(Float.NEGATIVE_INFINITY);
        chart.setLineChartData(data);

    }

    /**
     * 触摸监听类
     */
    private class ValueTouchListener implements LineChartOnValueSelectListener {

        @Override
        public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
            Toast.makeText(HelloActivity.this, "Selected: " + value, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onValueDeselected() {


        }

    }

}




