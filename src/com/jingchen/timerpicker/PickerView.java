package com.jingchen.timerpicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 滚动选择器 更多详解见博客http://blog.csdn.net/zhongkejingwang/article/details/38513301
 *
 * @author chenjing
 */
public class PickerView extends View {

    public static final String TAG = "PickerView";
    /**
     * text之间间距和minTextSize之比
     */
    public static final float MARGIN_ALPHA = 3.8f;
    /**
     * 自动回滚到中间的速度
     */
    public static final float SPEED = 2;

    private List<String> mDataList;
    /**
     * 选中的位置，这个位置是mDataList的中心位置，一直不变
     */
    private int mCurrentSelected;
    private Paint mPaint;

    private float mMaxTextSize = 80;
    private float mMinTextSize = 40;

    private float mMaxTextAlpha = 255;
    private float mMinTextAlpha = 120;

    private int mColorText = 0x333333;

    private int mViewHeight;
    private int mViewWidth;

    private float mLastDownY, mLastDownX;
    /**
     * 滑动的距离
     */
    private float mMoveLen = 0;
    private float moveDistance = 0;
    private boolean isInit = false;
    private onSelectListener mSelectListener;
    private Timer timer;
    private MyTimerTask mTask;

    private boolean isHorizontal;//是否是横向
    private boolean isRepeat;//是否是循环滚动

    private float zero;//零点坐标

    private float x, y;//绘制文本时的起始位置
    private FontMetricsInt fmi;
    private float offset;//偏移量
    private float scale;//刻度
    private float textSize;//绘制的字体大小


    Handler updateHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (Math.abs(mMoveLen) < SPEED) {
                mMoveLen = 0;
                if (mTask != null) {
                    mTask.cancel();
                    mTask = null;
                    performSelect();
                }
            } else
                // 这里mMoveLen / Math.abs(mMoveLen)是为了保有mMoveLen的正负号，以实现上滚或下滚
                mMoveLen = mMoveLen - mMoveLen / Math.abs(mMoveLen) * SPEED;
            invalidate();
        }

    };

    public PickerView(Context context) {
        super(context);
        init();
    }

    public PickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setOnSelectListener(onSelectListener listener) {
        mSelectListener = listener;
    }

    private void performSelect() {
        if (mSelectListener != null)
            mSelectListener.onSelect(mCurrentSelected);
    }

    public void setData(List<String> datas) {
        mDataList = datas;
        mCurrentSelected = datas.size() / 2;
        invalidate();
    }

    /**
     * 设置数据
     * @param datas  数据
     * @param currentSelectedValue  当前选中的值
     */
    public void setData(List<String> datas, String currentSelectedValue) {
        mDataList = datas;
        mCurrentSelected = findIndexByValue(currentSelectedValue);
        invalidate();
    }

    private int findIndexByValue(String value) {
        for (int i = 0; i < mDataList.size(); i++) {
            if (mDataList.get(i).equals(value)) {
                return i;
            }
        }
        return mDataList.size() / 2;
    }

    /**
     * 选择选中的item的index
     *
     * @param selected
     */
    public void setSelected(int selected) {
        mCurrentSelected = selected;
        int distance = mDataList.size() / 2 - mCurrentSelected;
        if (distance < 0)
            for (int i = 0; i < -distance; i++) {
                moveHeadToTail();
                mCurrentSelected--;
            }
        else if (distance > 0)
            for (int i = 0; i < distance; i++) {
                moveTailToHead();
                mCurrentSelected++;
            }
        invalidate();
    }

    public void setIsHorizontal(boolean isHorizontal) {
        this.isHorizontal = isHorizontal;
    }

    public void setIsRepeat(boolean isRepeat) {
        this.isRepeat = isRepeat;
    }

    /**
     * 选择选中的内容
     *
     * @param mSelectItem
     */
    public void setSelected(String mSelectItem) {
        for (int i = 0; i < mDataList.size(); i++)
            if (mDataList.get(i).equals(mSelectItem)) {
                setSelected(i);
                break;
            }
    }

    private void moveHeadToTail() {
        if (isRepeat) {
            String head = mDataList.get(0);
            mDataList.remove(0);
            mDataList.add(head);
        } else {
            mCurrentSelected++;
            if (mCurrentSelected >= mDataList.size()) {
                mCurrentSelected = mDataList.size() - 1;
            }
        }
    }

    private void moveTailToHead() {
        if (isRepeat) {
            String tail = mDataList.get(mDataList.size() - 1);
            mDataList.remove(mDataList.size() - 1);
            mDataList.add(0, tail);
        } else {
            mCurrentSelected--;
            if (mCurrentSelected <= 0) {
                mCurrentSelected = 0;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mViewHeight = getMeasuredHeight();
        mViewWidth = getMeasuredWidth();

        // 按照View的高度计算字体大小
//        if (isHorizontal) {
//            mMaxTextSize = mViewHeight / 4.0f;
//            mMinTextSize = mMaxTextSize / 2f;
//        } else {
//            mMaxTextSize = mViewHeight / 4.0f;
//            mMinTextSize = mMaxTextSize / 2f;
//        }
        isInit = true;
        invalidate();
    }

    private void init() {
        timer = new Timer();
        mDataList = new ArrayList<String>();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Style.FILL);
        mPaint.setTextAlign(Align.CENTER);
        mPaint.setColor(mColorText);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 根据index绘制view
        if (isInit && mCurrentSelected < mDataList.size()) {
            //如果不加mCurrentSelected<mDataList.size() 这个判断的话，在布局文件中设置一个该view，却不设置数据，会直接崩溃
            drawData(canvas);
        }
    }


    private void drawData(Canvas canvas) {
        // 先绘制选中的text再往上往下绘制其余的text
        drawSelectedText(canvas);
        // 绘制上方data
        for (int i = 1; (mCurrentSelected - i) >= 0; i++) {
            drawOtherText(canvas, i, -1);
        }
        // 绘制下方data
        for (int i = 1; (mCurrentSelected + i) < mDataList.size(); i++) {
            drawOtherText(canvas, i, 1);
        }
    }

    private void drawSelectedText(Canvas canvas) {
        scale = parabola(mMoveLen);
        setPaint();
        calculateXY(mMoveLen);
        canvas.drawText(mDataList.get(mCurrentSelected), x, y, mPaint);
    }

    private void setPaint() {
        textSize = (mMaxTextSize - mMinTextSize) * scale + mMinTextSize;
        mPaint.setTextSize(textSize);
        mPaint.setAlpha((int) ((mMaxTextAlpha - mMinTextAlpha) * scale + mMinTextAlpha));
    }

    /**
     * @param canvas
     * @param position 距离mCurrentSelected的差值
     * @param type     1表示向下绘制，-1表示向上绘制
     */
    private void drawOtherText(Canvas canvas, int position, int type) {
        offset = (MARGIN_ALPHA * mMinTextSize * position + type * mMoveLen);
        scale = parabola(offset);
        setPaint();
        calculateXY(type * offset);
        canvas.drawText(mDataList.get(mCurrentSelected + type * position), x, y, mPaint);
    }

    private void calculateXY(float offset) {
        // text居中绘制，注意baseline的计算才能达到居中，y值是text中心坐标
        if (isHorizontal) {
            x = (float) (mViewWidth / 2.0 + offset);
            y = (float) (mViewHeight / 2.0) + 25;
        } else {
            x = (float) (mViewWidth / 2.0);
            y = (float) (mViewHeight / 2.0 + offset);
            fmi = mPaint.getFontMetricsInt();
            y = (float) (y - (fmi.bottom / 2.0 + fmi.top / 2.0));
        }
    }

    /**
     * 抛物线
     *
     * @param offset 偏移量
     * @return scale
     */
    private float parabola(float offset) {
        zero = (isHorizontal ? mViewWidth : mViewHeight) / 8.0f;
        offset = (float) (1 - Math.pow(offset / zero, 2));
        return offset < 0 ? 0 : offset;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                doDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                doMove(event);
                break;
            case MotionEvent.ACTION_UP:
                doUp(event);
                break;
        }
        return true;
    }

    private void doDown(MotionEvent event) {
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
        mLastDownY = event.getY();
        mLastDownX = event.getX();
    }

    private void doMove(MotionEvent event) {
        if (isHorizontal) {
            moveDistance = event.getX() - mLastDownX;
        } else {
            moveDistance = event.getY() - mLastDownY;
        }
        if (!isRepeat) {//不是重复的话，在滚动的两边的时候，就不在滚动，重新进行绘制了
            if (mCurrentSelected == 0 && moveDistance > 0) {
                return;
            }
            if (moveDistance < 0 && mCurrentSelected == mDataList.size() - 1) {
                return;
            }
        }
        mMoveLen += moveDistance;
        if (mMoveLen > MARGIN_ALPHA * mMinTextSize / 2) {
            // 往右滑超过离开距离
            moveTailToHead();
            mMoveLen = mMoveLen - MARGIN_ALPHA * mMinTextSize;
        } else if (mMoveLen < -MARGIN_ALPHA * mMinTextSize / 2) {
            // 往左滑超过离开距离
            moveHeadToTail();
            mMoveLen = mMoveLen + MARGIN_ALPHA * mMinTextSize;
        }
        mLastDownX = event.getX();
        mLastDownY = event.getY();
        invalidate();
    }

    private void doUp(MotionEvent event) {
        // 抬起手后mCurrentSelected的位置由当前位置move到中间选中位置
        if (Math.abs(mMoveLen) < 0.0001) {
            mMoveLen = 0;
            return;
        }
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
        mTask = new MyTimerTask(updateHandler);
        timer.schedule(mTask, 0, 10);
    }

    class MyTimerTask extends TimerTask {
        Handler handler;

        public MyTimerTask(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            handler.sendMessage(handler.obtainMessage());
        }

    }

    public interface onSelectListener {
        void onSelect(int currentSelected);
    }

    public String getCurrentValue() {
        return mDataList.get(mCurrentSelected);
    }
}
