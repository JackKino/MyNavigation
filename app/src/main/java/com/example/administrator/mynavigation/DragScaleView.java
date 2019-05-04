package com.example.administrator.mynavigation;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class DragScaleView extends android.support.v7.widget.AppCompatImageView implements View.OnTouchListener  {
    protected int screenWidth;
    protected int screenHeight;
    protected int lastX;
    protected int lastY;
    private int oriLeft;
    private int oriRight;
    private int oriTop;
    private int oriBottom;
    private int dragDirection;
    private static final int TOP = 0x15;
    private static final int LEFT = 0x16;
    private static final int BOTTOM = 0x17;
    private static final int RIGHT = 0x18;
    private static final int LEFT_TOP = 0x11;
    private static final int RIGHT_TOP = 0x12;
    private static final int LEFT_BOTTOM = 0x13;
    private static final int RIGHT_BOTTOM = 0x14;
    private static final int TOUCH_TWO = 0x21;
    private static final int CENTER = 0x19;
    private int offset = 0; //可超出其父控件的偏移量
    protected Paint paint = new Paint();
    private static final int touchDistance = 80; //触摸边界的有效距离

    // 初始的两个手指按下的触摸点的距离
    private float oriDis = 1f;

    private static final String TAG = "CustomImageView";
    //控件是否能平移
    private boolean mCanTranslate = false;
    //控件是否能旋转
    private boolean mCanRotate = false;
    //控件是否能缩放
    private boolean mCanScale = false;
    //控件是否能够平移回弹
    private boolean mCanBackTranslate;
    //控件是否能够旋转回弹
    private boolean mCanBackRotate;
    //控件是否能够缩放回弹
    private boolean mCanBackSale;
    //默认最大缩放比例因子
    public static final float DEFAULT_MAX_SCALE_FACTOR = 3.0f;
    //默认最小缩放比例因子
    public static final float DEFAULT_MIN_SCALE_FACTOR = 0.8f;
    //最大缩放比例因子
    private float mMaxScaleFactor = 3.0f;
    //最小缩放比例因子
    private float mMinScaleFactor = 0.8f;
    //用于平移、缩放、旋转变换图片的矩阵
    private Matrix mCurrentMatrix = new Matrix();
    //上一次单点触控的坐标
    private PointF mLastSinglePoint = new PointF();
    //记录上一次两只手指中点的位置
    private PointF mLastMidPoint = new PointF();
    //记录上一次两只手指之间的距离
    private float mLastDist;
    //图片的边界矩形
    private RectF mBoundRectF = new RectF();
    //记录上一次两只手指构成的一个向量
    private PointF mLastVector = new PointF();
    //记录onLayout之后的初始化缩放因子
    private float mInitialScaleFactor = 1.0f;
    //记录图片总的缩放因子
    private float mTotalScaleFactor = 1.0f;
    //动画开始时的矩阵值
    private float[] mBeginMatrixValues = new float[9];
    //动画结束时的矩阵值
    private float[] mEndMatrixValues = new float[9];
    //动画过程中的矩阵值
    private float[] mTransformMatrixValues = new float[9];
    //属性动画
    private ValueAnimator mAnimator = ValueAnimator.ofFloat(0f, 1f);
    //属性动画默认时间
    public static final int DEFAULT_ANIMATOR_TIME = 300;


    /**
     * 初始化获取屏幕宽高
     */
    protected void initScreenW_H() {
        screenHeight = getResources().getDisplayMetrics().heightPixels - 40;
        screenWidth = getResources().getDisplayMetrics().widthPixels;
    }

    public DragScaleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnTouchListener(this);
        initScreenW_H();
    }

    public DragScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);
        initScreenW_H();
    }

    public DragScaleView(Context context) {
        super(context);
        setOnTouchListener(this);
        initScreenW_H();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(4.0f);
        paint.setStyle(Paint.Style.STROKE);
    }

    /**
     * 当单点触控的时候可以进行平移操作
     * 当多点触控的时候：可以进行图片的缩放、旋转
     * ACTION_DOWN：标记能平移、不能旋转、不能缩放
     * ACTION_POINTER_DOWN：如果手指个数为2,标记不能平移、能旋转、能缩放
     * 记录平移开始时两手指的中点、两只手指形成的向量、两只手指间的距离
     * ACTION_MOVE：进行平移、旋转、缩放的操作。
     * ACTION_POINTER_UP：有一只手指抬起的时候，设置图片不能旋转、不能缩放，可以平移
     *
     * @param event 点击事件
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            //单点触控，设置图片可以平移、不能旋转和缩放
            case MotionEvent.ACTION_DOWN:
                mCanTranslate = true;
                mCanRotate = false;
                mCanScale = false;
                //记录单点触控的上一个单点的坐标
                mLastSinglePoint.set(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
               // animator.cancel();
                //多点触控，设置图片不能平移
                mCanTranslate = false;
                //当手指个数为两个的时候，设置图片能够旋转和缩放
                if (event.getPointerCount() == 2) {
                    mCanRotate = true;
                    mCanScale = true;
                    //记录两手指的中点
                    PointF pointF = midPoint(event);
                    //记录开始滑动前两手指中点的坐标
                    mLastMidPoint.set(pointF.x, pointF.y);
                    //记录开始滑动前两个手指之间的距离
                    mLastDist = distance(event);
                    //设置向量，以便于计算角度
                    mLastVector.set(event.getX(0) - event.getX(1), event.getY(0) - event.getY(1));
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //判断能否平移操作
                if (mCanTranslate) {
                    float dx = event.getX() - mLastSinglePoint.x;
                    float dy = event.getY() - mLastSinglePoint.y;
                    //平移操作
                    translation(dx, dy);
                    //重置上一个单点的坐标
                    mLastSinglePoint.set(event.getX(), event.getY());
                }
                //判断能否缩放操作
                if (mCanScale) {
                    float scaleFactor = distance(event) / mLastDist;
                    scale(scaleFactor);
                    //重置mLastDist，让下次缩放在此基础上进行
                    mLastDist = distance(event);
                }
                //判断能否旋转操作
                if (mCanRotate) {
                    //当前两只手指构成的向量
                    PointF vector = new PointF(event.getX(0) - event.getX(1), event.getY(0) - event.getY(1));
                    //计算本次向量和上一次向量之间的夹角
                    float degree = calculateDeltaDegree(mLastVector, vector);
                    rotation(degree);
                    //更新mLastVector,以便下次旋转计算旋转过的角度
                    mLastVector.set(vector.x, vector.y);
                }
                //图像变换
                transform();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                //当两只手指有一只抬起的时候，设置图片不能缩放和选择，能够进行平移
                if (event.getPointerCount() == 2) {
                    mCanScale = false;
                    mCanRotate = false;
                    mCanTranslate = true;
                    //重置旋转和缩放使用到的中点坐标
                    mLastMidPoint.set(0f, 0f);
                    //重置两只手指的距离
                    mLastDist = 0f;
                    //重置两只手指形成的向量
                    mLastVector.set(0f, 0f);
                }
                //获得开始动画之前的矩阵
                mCurrentMatrix.getValues(mBeginMatrixValues);
                //缩放回弹
                backScale();
                upDateBoundRectF();
                //旋转回弹
                backRotation();
                upDateBoundRectF();
                //获得动画结束之后的矩阵
                mCurrentMatrix.getValues(mEndMatrixValues);
               // animator.start();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                backTranslation();
                upDateBoundRectF();
                mLastSinglePoint.set(0f, 0f);
                mCanTranslate = false;
                mCanScale = false;
                mCanRotate = false;
                break;
        }
        return true;
    }

    /**
     * 平移回弹
     * 平移之后不能出现有白边的情况
     */
    protected void backTranslation() {
        float dx = 0;
        float dy = 0;
        //判断图片的宽度是否大于控件的宽度，若是要进行边界的判断
        if (mBoundRectF.width() >= getWidth()) {
            //左边界在控件范围内，或者图片左边界超出控件范围
            if ((mBoundRectF.left > getLeft() && mBoundRectF.left <= getRight()) || mBoundRectF.left > getRight()) {
                dx = getLeft() - mBoundRectF.left;
            } //图片右边界在控件范围内,或者图片右边界超出控件范围
            else if ((mBoundRectF.right >= getLeft() && mBoundRectF.right < getRight()) || mBoundRectF.right < getLeft()) {
                dx = getRight() - mBoundRectF.right;
            }
        } //如果图片宽度小于控件宽度，移动图片中心x坐标和控件中心x坐标重合
        else {
            dx = getPivotX() - mBoundRectF.centerX();
        }
        //判断图片的高度是否大于控件的高度，若是要进行边界的判断
        if (mBoundRectF.height() >= getHeight()) {
            //图片上边界在控件范围内，或者图片上边界超出控件范围
            if ((mBoundRectF.top > getTop() && mBoundRectF.top <= getBottom()) || mBoundRectF.top > getBottom()) {
                dy = getTop() - mBoundRectF.top;
            } //图片下边界在控件范围内,或者图片下边界超出控件范围
            else if ((mBoundRectF.bottom < getBottom() && mBoundRectF.bottom >= getTop()) || mBoundRectF.bottom < getTop()) {
                dy = getBottom() - mBoundRectF.bottom;
            }
        } //如果图片高度小于控件高度，移动图片中心y坐标和控件中心y坐标重合
        else {
            dy = getPivotY() - mBoundRectF.centerY();
        }
        mCurrentMatrix.postTranslate(dx, dy);
    }



    protected void translation(float dx, float dy) {
        //检查图片边界的平移是否超过控件的边界
        if (mBoundRectF.left + dx > getWidth() - 20 || mBoundRectF.right + dx < 20
                || mBoundRectF.top + dy > getHeight() - 20 || mBoundRectF.bottom + dy < 20) {
            return;
        }
        mCurrentMatrix.postTranslate(dx, dy);
    }

    private void scale(float scaleFactor) {
        //累乘得到总的的缩放因子
        mTotalScaleFactor *= scaleFactor;
        mCurrentMatrix.postScale(scaleFactor, scaleFactor, mBoundRectF.centerX(), mBoundRectF.centerY());
    }

    private void rotation(float degree) {
        //旋转变换
        mCurrentMatrix.postRotate(degree, mBoundRectF.centerX(), mBoundRectF.centerY());

    }

    private void transform() {
        setImageMatrix(mCurrentMatrix);
        upDateBoundRectF();
    }
    private void upDateBoundRectF() {
        if (getDrawable() != null) {
            mBoundRectF.set(getDrawable().getBounds());
            mCurrentMatrix.mapRect(mBoundRectF);
        }
    }

    private void backScale() {
        float scaleFactor = 1.0f;
        //如果总的缩放比例因子比初始化的缩放因子还小，进行回弹
        if (mTotalScaleFactor / mInitialScaleFactor < mMinScaleFactor) {
            //1除以总的缩放因子再乘初始化的缩放因子，求得回弹的缩放因子
            scaleFactor = mInitialScaleFactor / mTotalScaleFactor * mMinScaleFactor;
            //更新总的缩放因子，以便下次在此缩放比例的基础上进行缩放
            mTotalScaleFactor = mInitialScaleFactor * mMinScaleFactor;
        }
        //如果总的缩放比例因子大于最大值，让图片放大到最大倍数
        else if (mTotalScaleFactor / mInitialScaleFactor > mMaxScaleFactor) {
            //求放大到最大倍数，需要的比例因子
            scaleFactor = mInitialScaleFactor / mTotalScaleFactor * mMaxScaleFactor;
            //更新总的缩放因子，以便下次在此缩放比例的基础上进行缩放
            mTotalScaleFactor = mInitialScaleFactor * mMaxScaleFactor;
        }
        mCurrentMatrix.postScale(scaleFactor, scaleFactor, mBoundRectF.centerX(), mBoundRectF.centerY());
    }

    private void backRotation() {
        //x轴方向的单位向量，在极坐标中，角度为0
        float[] x_vector = new float[]{1.0f, 0.0f};
        //映射向量
        mCurrentMatrix.mapVectors(x_vector);
        //计算x轴方向的单位向量转过的角度
        float totalDegree = (float) Math.toDegrees((float) Math.atan2(x_vector[1], x_vector[0]));
        float degree = totalDegree;
        degree = Math.abs(degree);
        //如果旋转角度的绝对值在45-135度之间，让其旋转角度为90度
        if (degree > 45 && degree <= 135) {
            degree = 90;
        } //如果旋转角度的绝对值在135-225之间，让其旋转角度为180度
        else if (degree > 135 && degree <= 225) {
            degree = 180;
        } //如果旋转角度的绝对值在225-315之间，让其旋转角度为270度
        else if (degree > 225 && degree <= 315) {
            degree = 270;
        }//如果旋转角度的绝对值在315-360之间，让其旋转角度为0度
        else {
            degree = 0;
        }
        degree = totalDegree < 0 ? -degree : degree;
        //degree-totalDegree计算达到90的倍数角，所需的差值
        mCurrentMatrix.postRotate(degree - totalDegree, mBoundRectF.centerX(), mBoundRectF.centerY());
    }

    /**
     * 计算两个手指头之间的中心点的位置
     * x = (x1+x2)/2;
     * y = (y1+y2)/2;
     *
     * @param event 触摸事件
     * @return 返回中心点的坐标
     */
    private PointF midPoint(MotionEvent event) {
        float x = (event.getX(0) + event.getX(1)) / 2;
        float y = (event.getY(0) + event.getY(1)) / 2;
        return new PointF(x, y);
    }

    /**
     * 计算两个手指间的距离
     *
     * @param event 触摸事件
     * @return 放回两个手指之间的距离
     */
   /* private float distance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);//两点间距离公式
    }*/

    /**
     * 计算两个向量之间的夹角
     *
     * @param lastVector 上一次两只手指形成的向量
     * @param vector     本次两只手指形成的向量
     * @return 返回手指旋转过的角度
     */
    private float calculateDeltaDegree(PointF lastVector, PointF vector) {
        float lastDegree = (float) Math.atan2(lastVector.y, lastVector.x);
        float degree = (float) Math.atan2(vector.y, vector.x);
        float deltaDegree = degree - lastDegree;
        return (float) Math.toDegrees(deltaDegree);
    }




    @Override
    public boolean onTouch(View v, MotionEvent event) {
      //  setBackgroundResource(R.drawable.bg_dashgap);
        int action = event.getAction()& MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_DOWN) {
            oriLeft = v.getLeft();
            oriRight = v.getRight();
            oriTop = v.getTop();
            oriBottom = v.getBottom();
            lastY = (int) event.getRawY();
            lastX = (int) event.getRawX();
            dragDirection = getDirection(v, (int) event.getX(),
                    (int) event.getY());
        }
        if (action == MotionEvent.ACTION_POINTER_DOWN){
            oriLeft = v.getLeft();
            oriRight = v.getRight();
            oriTop = v.getTop();
            oriBottom = v.getBottom();
            lastY = (int) event.getRawY();
            lastX = (int) event.getRawX();
            dragDirection = TOUCH_TWO;
            oriDis = distance(event);
        }
        // 处理拖动事件
        delDrag(v, event, action);
        invalidate();
        return false;
    }

    /**
     * 处理拖动事件
     *
     * @param v
     * @param event
     * @param action
     */
    protected void delDrag(View v, MotionEvent event, int action) {
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                int dx = (int) event.getRawX() - lastX;
                int dy = (int) event.getRawY() - lastY;
                switch (dragDirection) {
                    case LEFT: // 左边缘
                        left(v, dx);
                        break;
                    case RIGHT: // 右边缘
                        right(v, dx);
                        break;
                    case BOTTOM: // 下边缘
                        bottom(v, dy);
                        break;
                    case TOP: // 上边缘
                        top(v, dy);
                        break;
                    case CENTER: // 点击中心-->>移动
                        center(v, dx, dy);
                        break;
                    case LEFT_BOTTOM: // 左下
                        left(v, dx);
                        bottom(v, dy);
                        break;
                    case LEFT_TOP: // 左上
                        left(v, dx);
                        top(v, dy);
                        break;
                    case RIGHT_BOTTOM: // 右下
                        right(v, dx);
                        bottom(v, dy);
                        break;
                    case RIGHT_TOP: // 右上
                        right(v, dx);
                        top(v, dy);
                        break;
                    case TOUCH_TWO: //双指操控
                        float newDist =distance(event);
                        float scale = newDist / oriDis;
                        //控制双指缩放的敏感度
                        int distX = (int) (scale*(oriRight-oriLeft)-(oriRight-oriLeft))/50;
                        int distY = (int) (scale*(oriBottom-oriTop)-(oriBottom-oriTop))/50;
                        if (newDist>10f){//当双指的距离大于10时，开始相应处理
                            left(v, -distX);
                            top(v, -distY);
                            right(v, distX);
                            bottom(v, distY);
                        }
                        break;

                }
                if (dragDirection != CENTER) {
                    v.layout(oriLeft, oriTop, oriRight, oriBottom);
                }
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                dragDirection = 0;
                break;
        }
    }

    /**
     * 触摸点为中心->>移动
     *
     * @param v
     * @param dx
     * @param dy
     */
    private void center(View v, int dx, int dy) {
        int left = v.getLeft() + dx;
        int top = v.getTop() + dy;
        int right = v.getRight() + dx;
        int bottom = v.getBottom() + dy;
        if (left < -offset) {
            left = -offset;
            right = left + v.getWidth();
        }
        if (right > screenWidth + offset) {
            right = screenWidth + offset;
            left = right - v.getWidth();
        }
        if (top < -offset) {
            top = -offset;
            bottom = top + v.getHeight();
        }
        if (bottom > screenHeight + offset) {
            bottom = screenHeight + offset;
            top = bottom - v.getHeight();
        }
        Log.d("raydrag", left+"  "+top+"  "+right+"  "+bottom+"  "+dx);
        v.layout(left, top, right, bottom);
    }

    /**
     * 触摸点为上边缘
     *
     * @param v
     * @param dy
     */
    private void top(View v, int dy) {
        oriTop += dy;
        if (oriTop < -offset) {
            //对view边界的处理，如果子view达到父控件的边界，offset代表允许超出父控件多少
            oriTop = -offset;
        }
        if (oriBottom - oriTop - 2 * offset < 200) {
            oriTop = oriBottom - 2 * offset - 200;
        }
    }

    /**
     * 触摸点为下边缘
     *
     * @param v
     * @param dy
     */
    private void bottom(View v, int dy) {
        oriBottom += dy;
        if (oriBottom > screenHeight + offset) {
            oriBottom = screenHeight + offset;
        }
        if (oriBottom - oriTop - 2 * offset < 200) {
            oriBottom = 200 + oriTop + 2 * offset;
        }
    }

    /**
     * 触摸点为右边缘
     *
     * @param v
     * @param dx
     */
    private void right(View v, int dx) {
        oriRight += dx;
        if (oriRight > screenWidth + offset) {
            oriRight = screenWidth + offset;
        }
        if (oriRight - oriLeft - 2 * offset < 200) {
            oriRight = oriLeft + 2 * offset + 200;
        }
    }

    /**
     * 触摸点为左边缘
     *
     * @param v
     * @param dx
     */
    private void left(View v, int dx) {
        oriLeft += dx;
        if (oriLeft < -offset) {
            oriLeft = -offset;
        }
        if (oriRight - oriLeft - 2 * offset < 200) {
            oriLeft = oriRight - 2 * offset - 200;
        }
    }

    /**
     * 获取触摸点flag
     *
     * @param v
     * @param x
     * @param y
     * @return
     */
    protected int getDirection(View v, int x, int y) {
        int left = v.getLeft();
        int right = v.getRight();
        int bottom = v.getBottom();
        int top = v.getTop();
        if (x < touchDistance && y < touchDistance) {
            return LEFT_TOP;
        }
        if (y < touchDistance && right - left - x < touchDistance) {
            return RIGHT_TOP;
        }
        if (x < touchDistance && bottom - top - y < touchDistance) {
            return LEFT_BOTTOM;
        }
        if (right - left - x < touchDistance && bottom - top - y < touchDistance) {
            return RIGHT_BOTTOM;
        }
        if (x < touchDistance) {
            return LEFT;
        }
        if (y < touchDistance) {
            return TOP;
        }
        if (right - left - x < touchDistance) {
            return RIGHT;
        }
        if (bottom - top - y < touchDistance) {
            return BOTTOM;
        }
        return CENTER;
    }

    /**
     * 计算两个手指间的距离
     *
     * @param event 触摸事件
     * @return 放回两个手指之间的距离
     */
    private float distance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);//两点间距离公式
    }
}
