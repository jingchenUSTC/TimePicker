package com.jingchen.timerpicker;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

/**
 * 滚动选择器 更多详解见博客http://blog.csdn.net/zhongkejingwang/article/details/38513301
 * 
 * @author chenjing
 * 
 */
public class PickerView extends View {

	public static final String TAG = "PickerView";
	/**
	 * text之间间距和minTextSize之比
	 */
	public static final float MARGIN_ALPHA = 2.5f;//大于2保证在间距外的text是最小size，最小size是mViewHight/8.0f
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
	/**
	 * 最大字体
	 */
	private float mMaxTextSize = 0;//80
	/**
	 * 最小字体
	 */
	private float mMinTextSize = 0;//40
	
	private float spacing = 0;

	private float mMaxTextAlpha = 255;
	private float mMinTextAlpha = 150;

	private int mColorText = 0x333333;

	private int mViewHeight;
	private int mViewWidth;
	//手指按下的坐标
	private float mLastDownY;
	/**
	 * 滑动的距离
	 */
	private float mMoveLen = 0;
	private boolean isInit = false;
	private onSelectListener mSelectListener;
	private Timer timer;
	private MyTimerTask mTask;
	
	private boolean useDefault = false;

	Handler updateHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (Math.abs(mMoveLen) < SPEED)// 根据速度判定是否已经回滚到中间
			{
				mMoveLen = 0;
				if (mTask != null) {
					mTask.cancel();
					mTask = null;
					performSelect();
				}
			} else
				// 这里mMoveLen / Math.abs(mMoveLen)是为了保有mMoveLen的正负号，以实现上滚或下滚
				mMoveLen = mMoveLen - mMoveLen / Math.abs(mMoveLen) * SPEED;//以2的速度向中间滚动
			invalidate();
		}

	};
	private String width;
	private String height;

	public PickerView(Context context) {
		super(context);
		init(context,null);
	}

	public PickerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context,attrs);
	}

	public void setOnSelectListener(onSelectListener listener) {
		mSelectListener = listener;
	}

	private void performSelect() {
		if (mSelectListener != null)
			mSelectListener.onSelect(mDataList.get(mCurrentSelected));
	}

	public void setData(List<String> datas) {
		mDataList = datas;
		mCurrentSelected = datas.size() / 2;
		invalidate();
	}

	/**
	 * 选择选中的item的index
	 * @param selected
	 */
	public void setSelected(int selected)
	{
		mCurrentSelected = selected;
		int distance = mDataList.size() / 2 - mCurrentSelected;
		if (distance < 0)
			for (int i = 0; i < -distance; i++)
			{
				moveHeadToTail();
				mCurrentSelected--;
			}
		else if (distance > 0)
			for (int i = 0; i < distance; i++)
			{
				moveTailToHead();
				mCurrentSelected++;
			}
		invalidate();
	}
	
	/**
	 * 选择选中的内容
	 * @param mSelectItem
	 */
	public void setSelected(String mSelectItem){
		for(int i = 0; i < mDataList.size(); i++)
			if(mDataList.get(i).equals(mSelectItem)){
				setSelected(i);
				break;
			}
	}

	// 将第一个数据移到最后一个
	private void moveHeadToTail() {
		String head = mDataList.get(0);
		mDataList.remove(0);
		mDataList.add(head);
	}

	// 与上面的方法相反
	private void moveTailToHead() {
		String tail = mDataList.get(mDataList.size() - 1);
		mDataList.remove(mDataList.size() - 1);
		mDataList.add(0, tail);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (useDefault) {
			setMeasuredDimension(mViewWidth, mViewHeight);
		}else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			mViewHeight = getMeasuredHeight();
			mViewWidth = getMeasuredWidth();
		}
		if ( mMinTextSize == 0) {
			if (mMaxTextSize == 0 ) {
				// 按照View的高度计算字体大小
				mMaxTextSize = mViewHeight / 4.0f;
				mMinTextSize = mMaxTextSize / 2f;
			}else {
				mMinTextSize = mMaxTextSize / 2f;
			}
		}
		
		if (spacing == 0 || spacing < 2.5*mMaxTextSize) {
			spacing = MARGIN_ALPHA * mMinTextSize;
		}
//		mMaxTextSize = mViewHeight / 4.0f;
//		mMinTextSize = mMaxTextSize / 2f;
		isInit = true;
		//invalidate();//这句貌似没什么用
	}
	/**
	 * 初始化定时器和画笔
	 */
	private void init(Context context, AttributeSet attrs) {
		timer = new Timer();
		mDataList = new ArrayList<String>();
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		if (attrs != null) {
			TypedArray tArray = context.obtainStyledAttributes(attrs, R.styleable.PickerView);
			mColorText = tArray.getColor(R.styleable.PickerView_textColor, mColorText);
			spacing = tArray.getFloat(R.styleable.PickerView_spacing, spacing);
			mMaxTextAlpha = tArray.getFloat(R.styleable.PickerView_maxAlpha, mMaxTextAlpha);
			mMinTextAlpha = tArray.getFloat(R.styleable.PickerView_minAlpha, mMinTextAlpha);
			mMaxTextSize = tArray.getFloat(R.styleable.PickerView_maxTextSize, mMaxTextSize);
			mMinTextSize = tArray.getFloat(R.styleable.PickerView_minTextSize, mMinTextSize);
			tArray.recycle();
		}
		
		width = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "layout_width");
		height = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "layout_height");
		
		final float scale = context.getResources().getDisplayMetrics().density; 
		if (width.equals("-2")) {
			if (mMaxTextSize == 0) {
				mViewHeight = (int) (160 * scale + 0.5f);
				mViewWidth = (int) (80 * scale + 0.5f);
			}else {
				mViewHeight = (int)mMaxTextSize * 4;
				mViewWidth = (int)mMaxTextSize * 2;
			}
			useDefault = true;
		}else if(width.equals("-1")){
			useDefault = false;
		}else {
			String[] split = width.split("d");
			String[] split2 = height.split("d");
			mViewHeight = (int) (Float.valueOf(split2[0]) * scale + 0.5f);
			mViewWidth = (int) (Float.valueOf(split[0]) * scale + 0.5f);
			useDefault = true;
		}
		
		mPaint.setStyle(Style.FILL);
		mPaint.setTextAlign(Align.CENTER);
		mPaint.setColor(mColorText);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// 根据index绘制view
		if (isInit)
			drawData(canvas);
	}
	/**
	 * 画文字
	 * @param canvas
	 */
	private void drawData(Canvas canvas) {
		// 先绘制选中的text再往上往下绘制其余的text
		float scale = parabola(mViewHeight / 4.0f, mMoveLen);//初始值为1
		float size = (mMaxTextSize - mMinTextSize) * scale + mMinTextSize;
		mPaint.setTextSize(size);
		mPaint.setAlpha((int) ((mMaxTextAlpha - mMinTextAlpha) * scale + mMinTextAlpha));//同样通过scale控制alpha的变化
		// text居中绘制，注意baseline的计算才能达到居中，y值是text中心坐标
		float x = (float) (mViewWidth / 2.0);
		float y = (float) (mViewHeight / 2.0 + mMoveLen);
		FontMetricsInt fmi = mPaint.getFontMetricsInt();//用来计算paint的baseline
		float baseline = (float) (y - (fmi.bottom / 2.0 + fmi.top / 2.0));//计算baseline，fmi的bottom+top是这段text所占的相对位移，/2是一半，y是中心，减去一半是最底下，然后text就在这个baseline之上绘制。
//		canvas.drawLine(0, baseline, x+mViewWidth/2, baseline, mPaint);
		canvas.drawText(mDataList.get(mCurrentSelected), x, baseline, mPaint);//如果这里不用baseline而是用y+mViewHeight/8会靠下，不准确。
		// 绘制上方data
		for (int i = 1; (mCurrentSelected - i) >= 0; i++) {
			drawOtherText(canvas, i, -1);
		}
		// 绘制下方data
		for (int i = 1; (mCurrentSelected + i) < mDataList.size(); i++) {
			drawOtherText(canvas, i, 1);
		}
	}

	/**
	 * @param canvas
	 * @param position
	 *            距离mCurrentSelected的差值
	 * @param type
	 *            1表示向下绘制，-1表示向上绘制
	 */
	private void drawOtherText(Canvas canvas, int position, int type) {
//		float d = (float) (MARGIN_ALPHA * mMinTextSize * position + type * mMoveLen);//MARGIN_ALPHA * mMinTextSize就是两个text之间的间距
		float d = (float) (spacing * position + type * mMoveLen);//MARGIN_ALPHA * mMinTextSize就是两个text之间的间距
		float scale = parabola(mViewHeight / 4.0f, d);//0
		float size = (mMaxTextSize - mMinTextSize) * scale + mMinTextSize;
		mPaint.setTextSize(size);
		mPaint.setAlpha((int) ((mMaxTextAlpha - mMinTextAlpha) * scale + mMinTextAlpha));
		float y = (float) (mViewHeight / 2.0 + type * d);
		FontMetricsInt fmi = mPaint.getFontMetricsInt();
		float baseline = (float) (y - (fmi.bottom / 2.0 + fmi.top / 2.0));
		canvas.drawText(mDataList.get(mCurrentSelected + type * position),(float) (mViewWidth / 2.0), baseline, mPaint);
	}

	/**
	 * 抛物线
	 * 主要是根据偏移量计算缩放大小从而确定字体大小和透明度
	 * @param zero
	 *            零点坐标
	 * @param x
	 *            偏移量
	 * @return scale
	 */
	private float parabola(float zero, float x) {
		float f = (float) (1 - Math.pow(x / zero, 2));//1-(x/zero)^2
		return f < 0 ? 0 : f;
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
	}

	private void doMove(MotionEvent event) {

		mMoveLen += (event.getY() - mLastDownY);

		if (mMoveLen > MARGIN_ALPHA * mMinTextSize / 2) {
			// 往下滑超过离开距离
			moveTailToHead();
			mMoveLen = mMoveLen - MARGIN_ALPHA * mMinTextSize;
		} else if (mMoveLen < -MARGIN_ALPHA * mMinTextSize / 2) {
			// 往上滑超过离开距离
			moveHeadToTail();
			mMoveLen = mMoveLen + MARGIN_ALPHA * mMinTextSize;
		}

		mLastDownY = event.getY();
		invalidate();//这里重绘界面保持实时更新界面，重走onMesure和onDraw
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
		void onSelect(String text);
	}
	
}