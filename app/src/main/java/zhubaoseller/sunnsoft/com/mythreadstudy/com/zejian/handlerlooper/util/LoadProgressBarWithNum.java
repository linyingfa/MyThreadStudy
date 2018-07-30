package zhubaoseller.sunnsoft.com.mythreadstudy.com.zejian.handlerlooper.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import zhubaoseller.sunnsoft.com.mythreadstudy.R;


/**
 * Created by zejian
 * Time 16/9/4.
 * Description:
 */
public class LoadProgressBarWithNum extends View {
    /** 默认文本size */
    private static final int DEFAULT_TEXT_SIZE = 100;
    /** 默认文本颜色 */
    private static final int DEFAULT_TEXT_COLOR = 0xFF66C796;
    /** 默认进度环颜色 */
    private static final int DEFAULT_COLOR_RING_COLOR = 0xA3A0E3;
    /** 默认进度动条颜色 */
    private static final int DEFAULT_COLOR_REACHED_COLOR = 0x448AFF;
    /** black进度环圈画笔 **/
    private Paint mPaintInnerCircle;
    /** front进度环圈画笔 **/
    private Paint mPaintFrontCircle;
    /** 字体画笔 **/
    private Paint mPaintText;
    /** 字体大小 **/
    private int mTextSize = DEFAULT_TEXT_SIZE;
    /** 字体颜色 **/
    private int mTextColor = DEFAULT_TEXT_COLOR;
    /** 进度环颜色 */
    private int mProgressRingColor = DEFAULT_COLOR_RING_COLOR;
    /** 进度已运转颜色 */
    private int mProgressReachedColor = DEFAULT_COLOR_REACHED_COLOR;
    /** 进度环半径 */
    private float mRadius = 200;
    /** 进度环画笔宽度 */
    private float mStrokeWidth = 25;
    /** 判断文本是否可见 */
    protected boolean mIfDrawText = true;
    private int mProgress = 0;
    private int mMax = 100;
    /**
     * @return 获取当前进度
     */
    public int getmProgress() {
        return mProgress;
    }
    /**
     * @param mProgress 设置当前进度
     */
    public void setmProgress(int mProgress) {
        if(mProgress <=mMax){
            this.mProgress = mProgress;
            invalidate();
        }
    }
    public LoadProgressBarWithNum(Context context) {
        this(context, null);
        initPaint();
    }
    public LoadProgressBarWithNum(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        initPaint();
    }
    public LoadProgressBarWithNum(Context context, AttributeSet attrs,
                                  int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
        // 获取自定义属性的值
        TypedArray ta = context.obtainStyledAttributes(attrs,
                R.styleable.LoadProgressBarWithNum);
        mRadius = ta.getDimension(
                R.styleable.LoadProgressBarWithNum_progress_radius, 50);
        mStrokeWidth = ta.getDimension(
                R.styleable.LoadProgressBarWithNum_progress_strokeWidth, 20);
        mTextSize = (int) ta.getDimension(
                R.styleable.LoadProgressBarWithNum_progress_text_size,
                DEFAULT_TEXT_SIZE);
        mProgressRingColor = ta.getColor(
                R.styleable.LoadProgressBarWithNum_progress_ring_color,
                DEFAULT_COLOR_RING_COLOR);
        mProgressReachedColor = ta.getColor(
                R.styleable.LoadProgressBarWithNum_progress_reached_color,
                DEFAULT_COLOR_REACHED_COLOR);
        int textVisible = ta.getInt(
                R.styleable.LoadProgressBarWithNum_progress_text_visibility,
                VISIBLE);
        //获取当前配置进度
        int progressValue=ta.getInt(R.styleable.LoadProgressBarWithNum_progress_value,0);
        if(progressValue<mMax){
            mProgress=progressValue;
        }else{
            mProgress=mMax;
        }
        if (textVisible != VISIBLE) {
            mIfDrawText = false;
        }
        ta.recycle();
    }
    /**
     * 初始化画笔
     */
    private void initPaint() {
        // int colorInt= Color.parseColor("#A3A0E3") ;
        mPaintInnerCircle = getPaint(mProgressRingColor, Paint.Style.STROKE,
                (int) mStrokeWidth, 255);
        mPaintFrontCircle = getPaint(mProgressReachedColor, Paint.Style.STROKE,
                (int) mStrokeWidth, 255);
        // 设置文字画笔
        mPaintText = new Paint();
        mPaintText.setColor(mTextColor);
        mPaintText.setTextSize(mTextSize);
        mPaintText.setAntiAlias(true); // 设置抗锯齿
        mPaintText.setDither(true); // 防抖动
        mPaintText.setStrokeCap(Paint.Cap.ROUND); // 设置画笔转弯去的连接风格
        // mPaintText.setTextAlign(Paint.Align.CENTER);
    }
    /**
     * 测量方法，测量控件的大小，事实上在onDraw上已经可以完成相应的效果，为什么还要测量呢？这主要是我们提供给外使用人员设置控件的大小的属性，
     * 所以某些情况还是得测量的
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 获取宽高的测量模式
        int mWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        int mHeightMode = MeasureSpec.getMode(heightMeasureSpec);
        // 判断是否为精确模式，否则重新测量宽高
        if (mWidthMode != MeasureSpec.EXACTLY) {
            int exceptWidth = (int) (getPaddingLeft() + getPaddingRight() + 2
                    * mRadius + mStrokeWidth);
            // 重新合成测量大小与模式
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(exceptWidth,
                    MeasureSpec.EXACTLY);
        }
        if (mHeightMode != MeasureSpec.EXACTLY) {
            int excepHeight = (int) (getPaddingBottom() + getPaddingTop() + 2
                    * mRadius + mStrokeWidth);
            // 重新合成测量大小与模式
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(excepHeight,
                    MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    /**
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//		// 获取控件大小
//		int mWidth = getMeasuredWidth();
//		int mHeight = getMeasuredHeight();
        // 保存当前画布
        canvas.save();
        // 平移，防止环形被剪切
        canvas.translate(getPaddingLeft() + mStrokeWidth / 2, getPaddingTop()
                + mStrokeWidth / 2);
        // 获取文字的宽高
        String text = mProgress + "%";
        float textWidth = mPaintText.measureText(text);
        float textHeight = mPaintText.descent() + mPaintText.ascent();
        float angle = mProgress / (float) mMax * 360;
        canvas.drawCircle(mRadius, mRadius, mRadius, mPaintInnerCircle);
        // canvas.drawArc(mRectF, -90,angle, false, mPaintFrontCircle);
        canvas.drawArc(new RectF(0, 0, mRadius * 2, mRadius * 2), 0, angle,
                false, mPaintFrontCircle);
        if (mIfDrawText) {// 判断是否可绘制文本
            canvas.drawText(mProgress + "%", mRadius - textWidth / 2, mRadius
                    - textHeight / 2, mPaintText);
        }

        // 还原当前画布
        canvas.restore();
    }
    /**
     * 获取画笔的通用方法
     *
     * @param color
     *            画笔的颜色
     * @param style
     *            画笔的样式
     *            Paint.Style.FILL:填充内部;Paint.Style.FILL_AND_STROKE：填充内部和描边
     *            ;Paint.Style.STROKE：仅描边
     * @param strokeWidth
     *            画笔的宽度
     * @param alpha
     *            画笔的透明度 取值范围为0~255，数值越小越透明
     * @return
     */
    public Paint getPaint(int color, Paint.Style style, int strokeWidth,
                          int alpha) {
        Paint newPaint = new Paint();
        newPaint.setColor(color);// 设置画笔文件
        newPaint.setAntiAlias(true);// 设置抗锯齿
        newPaint.setStyle(style);// 设置画笔的样式
        newPaint.setStrokeWidth(strokeWidth);// 设置画笔的宽度
        newPaint.setAlpha(alpha);// 设置透明度参数a为透明度，取值范围为0~255，数值越小越透明
        return newPaint;
    }
}
