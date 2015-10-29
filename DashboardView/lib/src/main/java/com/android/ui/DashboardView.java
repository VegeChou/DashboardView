package com.android.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

public class DashboardView extends View {
    private static final int POINTER_MIN_DEGREES = -60;
    private static final int POINTER_MAX_DEGREES = 60;

    // 表盘渐变色起始颜色
    private int dialColorStart = 0xFFE3170D;
    // 表盘渐变色结束颜色
    private int dialColorEnd = 0xFFFF7F50;
    // 表盘宽度
    private int dialWidth;
    // 表盘所在正方形位置
    private int dialLeft;
    private int dialTop;
    private int dialRight;
    private int dialBottom;
    // 表针颜色
    private int pointerColor = Color.WHITE;
    // 表针圆弧半径
    private int pointerCycleRadius;
    // 表针当前所在位置
    private float pointerCurrentDegrees = POINTER_MIN_DEGREES;
    // 表针目标位置
    private float pointerToDegress = POINTER_MAX_DEGREES;

    // 初始百分比
    private int percent;

    private MyHandler myHandler;

    private PointerAnimationRunnable pointerAnimationRunnable;

    public DashboardView(Context context) {
        super(context);
        init(context,null);
    }

    public DashboardView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context,attributeSet);
    }

    private void init(Context context,AttributeSet attributeSet) {
        if (attributeSet!=null){
            TypedArray typedArray = context.obtainStyledAttributes(R.styleable.DashboardView);

            dialColorStart = typedArray.getColor(R.styleable.DashboardView_dialColor,111);
            percent = typedArray.getInt(R.styleable.DashboardView_percent,0);
            pointerToDegress = POINTER_MIN_DEGREES + (POINTER_MAX_DEGREES - POINTER_MIN_DEGREES) * percent / 100;
        }
        myHandler = new MyHandler();
        pointerAnimationRunnable = new PointerAnimationRunnable();
        if (!pointerAnimationRunnable.animationIsStarted) {
            new Thread(pointerAnimationRunnable).start();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawDashboard(canvas);
    }

    public void drawDashboard(Canvas canvas) {
        // 获取画布大小
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        // View 整体向下偏移量
//        int increment = 0;
        int increment = canvasWidth / 8;
        //获取画布中心
        int canvasCenterX = canvasWidth / 2;
        int canvasCenterY = canvasHeight / 2 + increment;
        // 获取表盘正方形所在位置
        dialLeft = canvasWidth / 10;
        dialTop = canvasHeight / 10 + increment;
        dialRight = canvasWidth - dialLeft;
        dialBottom = dialTop + (dialRight - dialLeft);
        // 获取表盘宽度
        dialWidth = canvasWidth / 8;
        // 获取指针圆弧半径
        pointerCycleRadius = dialWidth * 2 / 3;


        /**
         * 表盘由一个弧线构成，当弧线足够宽，显示就是表盘
         */
        Paint paint = new Paint();
        // 画笔抗锯齿
        paint.setAntiAlias(true);
        // 画笔颜色
        Shader shader = new LinearGradient(0, 0, 100, 100, new int[]{
                dialColorStart, dialColorEnd}, null, Shader.TileMode.CLAMP);
        paint.setShader(shader);
        // 画笔宽度
        paint.setStrokeWidth(dialWidth);
        // 画笔类型为描边
        paint.setStyle(Paint.Style.STROKE);

        // 以整个画布构造一个正方形，用于画圆形使用
        RectF rectF = new RectF(dialLeft, dialTop, dialRight, dialBottom);
        /**
         * 函数功能：画弧线
         * 参数1：弧线所在矩形范围内。如果为矩形，完整画出来是椭圆；如果为正方形，完整画出来是圆形。
         * 参数2：画弧线起始位置。0为3点钟位置，顺时针方向开始画。
         * 参数3：画弧线的角度。
         * 参数4：
         * 参数5：设置画笔，包括颜色，是否填充，描边粗细等。
         *
         */
        canvas.drawArc(rectF, 210, 120, false, paint);

        /**
         * 指针可有两条直线和一个弧线构成闭环填充画出
         */
        // 重置画笔
        paint.reset();
        // 画笔抗锯齿
        paint.setAntiAlias(true);
        // 画笔类型为填充
        paint.setStyle(Paint.Style.FILL);
        // 画笔颜色
        paint.setColor(pointerColor);

        // 由线和弧构造指针
        Path path = new Path();
        path.moveTo(canvasCenterX - pointerCycleRadius, canvasCenterY);
        path.lineTo(canvasCenterX, 0 + increment);
        path.lineTo(canvasCenterX + pointerCycleRadius, canvasCenterY);
        path.lineTo(canvasCenterX - pointerCycleRadius, canvasCenterY);
        // 构成弧的正方形
        rectF.set(canvasCenterX - pointerCycleRadius, canvasCenterY - pointerCycleRadius, canvasCenterX + pointerCycleRadius, canvasCenterY + pointerCycleRadius);
        path.arcTo(rectF, 0, 180);

        // 旋转画布是指针初始位置在表盘起点
        canvas.rotate(pointerCurrentDegrees, canvasCenterX, canvasCenterY);
        canvas.drawPath(path, paint);
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1000:
                    invalidate();
                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    public void setPercent(int percent) {
        if (percent < 0) {
            percent = 0;
        } else if (percent > 100) {
            percent = 100;
        }
        if (!pointerAnimationRunnable.animationIsStarted) {
            pointerToDegress = POINTER_MIN_DEGREES + (POINTER_MAX_DEGREES - POINTER_MIN_DEGREES) * percent / 100;
            new Thread(pointerAnimationRunnable).start();
        }
    }

    class PointerAnimationRunnable implements Runnable {
        private static final int ONE_SECOND = 1000;
        private static final int FRAME_PER_SECOND = 50;

        // 动画是否正在执行，如果快速连续设置两个值，第二个值可能无响应
        private boolean animationIsStarted = false;

        @Override
        public void run() {
            animationIsStarted = true;
            float incrementDegrees = (pointerToDegress - pointerCurrentDegrees) / FRAME_PER_SECOND;
            for (int i = 0; i < FRAME_PER_SECOND; i++) {
                pointerCurrentDegrees = pointerCurrentDegrees + incrementDegrees;
                myHandler.sendEmptyMessage(1000);
                try {
                    Thread.sleep(ONE_SECOND / FRAME_PER_SECOND);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            animationIsStarted = false;
        }
    }
}