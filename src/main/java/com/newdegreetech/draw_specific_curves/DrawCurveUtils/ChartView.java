package com.newdegreetech.draw_specific_curves.DrawCurveUtils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

//import com.ndt.forcesensor.demo.R;

import com.newdegreetech.draw_specific_curves.R;

import java.util.LinkedList;

// 用于以折线图的形式展示 rawData 或 forceData
public class ChartView extends View {

    private int ch_num;
    private float originX;
    private float originY;
    private float xLen;
    private float yLen;
    private float xUnitLen;
    private float yUnitLen;
    private int xCnt;
    private int yCnt;
    private float x;
    private short max = 1000;
    private short min = -1000;
    private short MAX = 30000;
    private short MIN = -30000;
    private short yUnitNum;
    private float textOffset;

    private Paint paint = new Paint();
    private float textSize = 30.0f;
    private int[] colors = {
            Color.parseColor("#ff4b3d"),
            Color.parseColor("#fbd03b"),
            Color.parseColor("#06d2a6"),
            Color.parseColor("#ff8a7e"),
            Color.parseColor("#157f9e"),
            Color.parseColor("#98afb2")
    };
    private int colorText = Color.parseColor("#444444");
    private int colorAxis = Color.parseColor("#AAAAAA");
    private int colorDot = Color.parseColor("#fc757c");

    private LinkedList<Short[]> pointQueue = new LinkedList();
    private int pointQueueSize = 0;
    private boolean isInit = false;

    public ChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setBackgroundColor(Color.TRANSPARENT);

//        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ChartView);
//        ch_num = ta.getInteger(R.styleable.ChartView_ch_num, 4);    // default: 4
//        ta.recycle();

        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.RIGHT);
        Paint.FontMetrics fm = paint.getFontMetrics();
        textOffset = 0 - (fm.ascent + fm.descent) / 2;
    }

    private float numPerPixel;
    private float touchY1;
    private float touchY2;
    private float lastY1;
    private float lastY2;
    private int touchCnt = 0;
    private int lastTouchCnt = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        numPerPixel = (max - min) / yLen;
        numPerPixel = numPerPixel <= 1 ? 1 : numPerPixel;

        if (event.getAction() == event.ACTION_UP)
            touchCnt = 0;
        else
            touchCnt = event.getPointerCount();

        if (touchCnt > 0)
            touchY1 = event.getY();

        if (touchCnt > 1)
            touchY2 = event.getY(1);

        if (touchCnt == 1 && lastTouchCnt == 1) {
            float offset = (touchY1 - lastY1) * numPerPixel;
            int tempMax = (int) (max + offset);
            int tempMin = (int) (min + offset);
            setLimit(tempMax, tempMin);
            invalidate();
        }

        if (touchCnt == 2 && lastTouchCnt == 2) {
            float offset1 = (touchY1 - lastY1) * numPerPixel;
            float offset2 = (touchY2 - lastY2) * numPerPixel;
            int tempMax, tempMin;
            if (lastY1 < lastY2) {
                tempMax = (int) (max + offset1);
                tempMin = (int) (min + offset2);
            } else {
                tempMin = (int) (min + offset1);
                tempMax = (int) (max + offset2);
            }
            setLimit(tempMax, tempMin);
            invalidate();
        }

        lastY1 = touchY1;
        lastY2 = touchY2;
        lastTouchCnt = touchCnt;

        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        float height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        if (!isInit) {
            originX = 0.12f * width;
            originY = 0.90f * height;
            xLen = 0.80f * width;
            yLen = 0.80f * height;
            xUnitLen = 10;
            x = xLen;
            xCnt = (int) (xLen / xUnitLen - 1);
            yCnt = 10;
            yUnitLen = yLen / yCnt;
            setLimit(max, min);
            isInit = true;
        }
    }

    public boolean setLimit(int max, int min) {
        if (min < max && min > MIN && max < MAX && isInit) {
            this.max = (short) max;
            this.min = (short) min;
            yUnitNum = (short) ((this.max - this.min) / yCnt);
            return true;
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawAxis(canvas, paint);
        drawChText(canvas, paint, ch_num);

        if (pointQueueSize != 0) {
            drawData(canvas, paint, 0, ch_num);
        }
    }

    private void drawAxis(Canvas c, Paint p) {
        //draw y axis
        p.setStrokeWidth(2.5f);
        p.setColor(colorAxis);
        for (int i = 0; i < yCnt; i++) {
            float y = originY - i * yUnitLen;
            c.drawLine(originX, y, originX + xLen, y, p);
        }

        //draw y axis text
        paint.setTextAlign(Paint.Align.RIGHT);
        p.setColor(colorText);
        for (int i = 0; i < yCnt; i++) {
            float y = originY - i * yUnitLen;
            c.drawText("" + (i * yUnitNum + min),
                    originX - textSize,
                    y + textOffset, p);
        }
    }

    private void drawChText(Canvas c, Paint p, int CH_NUM) {
        paint.setTextAlign(Paint.Align.LEFT);
        for (int i = 0; i < CH_NUM; i++) {
            p.setColor(colors[i]);
            c.drawText("CH" + (i + 1),
                    originX + i * textSize * 3.5f,
                    originY + textSize * 1.1f, p);
        }
    }

    private void drawData(Canvas c, Paint p, int start, int end) {
        int i = 0;
        float rate, y1, y2;
        Short[] lastPoints = pointQueue.getFirst();
        for (Short[] points : pointQueue) {

            //draw point
            p.setStrokeWidth(5.0f);
            p.setColor(colorDot);
            for (int j = start; j < end; j++) {
                rate = (float) ((lastPoints[j] - min) / (1.0 * (max - min)));
                y1 = originY - yLen * rate;
//                c.drawPoint(originX + x + i * xUnitLen, y1, p);
            }

            //draw line
            p.setStrokeWidth(3.0f);
            for (int j = start; j < end; j++) {
                rate = (float) ((lastPoints[j] - min) / (1.0 * (max - min)));
                y1 = originY - yLen * rate;
                rate = (float) ((points[j] - min) / (1.0 * (max - min)));
                y2 = originY - yLen * rate;
                p.setColor(colors[j]);
                float tempX = originX + x + i * xUnitLen;
                c.drawLine(tempX, y1, tempX + xUnitLen, y2, p);
            }
            lastPoints = points;
            i++;
        }
    }

    // 增加一组数据
    public void addPoint(short[] data) {
        if (data[0] == (short)0xFFFF && data[1] == (short)0xFFFF) {
            return;
        }
        if (pointQueueSize > xCnt) {
            pointQueue.removeFirst();
            pointQueueSize--;
        } else {
            x -= xUnitLen;
        }
        Short[] Data = new Short[ch_num];
        for (int i = 0; i < ch_num; i++) {
            Data[i] = data[i];
        }
        pointQueue.add(Data);
        pointQueueSize++;
    }

}
