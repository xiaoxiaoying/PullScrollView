package com.markmao.pullscrollview.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ScrollView;

import com.markmao.pullscrollview.R;

/**
 * 自定义ScrollView
 *
 * @author markmjw
 * @date 2013-09-13
 */
public class PullScrollView extends ScrollView {
    private static final String LOG_TAG = "PullScrollView";
    /**
     * 阻尼系数,越小阻力就越大.
     */
    private static final float SCROLL_RATIO = 0.5f;

    /**
     * 滑动至翻转的距离.
     */
    private static final int TURN_DISTANCE = 100;

    /**
     * 头部view.
     */
    private View mHeader;

    /**
     * 头部view高度.
     */
    private int mHeaderHeight;

    /**
     * 头部view显示高度.
     */
    private int mHeaderVisibleHeight;

    /**
     * ScrollView的content view.
     */
    private View mContentView;

    /**
     * ScrollView的content view矩形.
     */
    private Rect mContentRect = new Rect();

    /**
     * 首次点击的Y坐标.
     */
    private PointF mStartPoint = new PointF();

    /**
     * 是否开始移动.
     */
    private boolean isMoving = false;

    /**
     * 是否移动到顶部位置.
     */
    private boolean isTop = false;

    /**
     * 头部图片初始顶部和底部.
     */
    private int mInitTop, mInitBottom;

    /**
     * 头部图片拖动时顶部和底部.
     */
    private int mCurrentTop, mCurrentBottom;

    /**
     * 状态变化时的监听器.
     */
    private OnTurnListener mOnTurnListener;

    private enum State {
        /**
         * 顶部
         */
        UP,
        /**
         * 底部
         */
        DOWN,
        /**
         * 正常
         */
        NORMAL
    }

    /**
     * 状态.
     */
    private State mState = State.NORMAL;

    public PullScrollView(Context context) {
        super(context);
        init(context, null);
    }

    public PullScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PullScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        // set scroll mode
        mSmoothInterpolator = new AccelerateDecelerateInterpolator();
        setOverScrollMode(OVER_SCROLL_NEVER);

        if (null != attrs) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PullScrollView);

            if (ta != null) {
                mHeaderHeight = (int) ta.getDimension(R.styleable.PullScrollView_headerHeight, -1);
                mHeaderVisibleHeight = (int) ta.getDimension(R.styleable
                        .PullScrollView_headerVisibleHeight, -1);
                ta.recycle();
            }
        }
    }

    /**
     * 设置Header
     *
     * @param view
     */
    public void setHeader(View view) {
        mHeader = view;
    }

    /**
     * 设置状态改变时的监听器
     *
     * @param turnListener
     */
    public void setOnTurnListener(OnTurnListener turnListener) {
        mOnTurnListener = turnListener;
    }

    @Override
    protected void onFinishInflate() {
        if (getChildCount() > 0) {
            mContentView = getChildAt(0);
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        Log.i("888", "171 getScrollY == " + getScrollY());
        if (getScrollY() == 0) {
            isTop = true;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return onTouchEvent(ev) || super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mContentView != null) {
            int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mStartPoint.set(ev.getX(), ev.getY());
                    mCurrentTop = mInitTop = mHeader.getTop();
                    mCurrentBottom = mInitBottom = mHeader.getBottom();
                    return super.onTouchEvent(ev);
                case MotionEvent.ACTION_MOVE:
                    float deltaY = Math.abs(ev.getY() - mStartPoint.y);
                    if (deltaY > 10 && deltaY > Math.abs(ev.getX() - mStartPoint.x)) {
                        mHeader.clearAnimation();
                        mContentView.clearAnimation();
                        doActionMove(ev);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    // 回滚动画
                    if (isNeedAnimation()) {
                        rollBackAnimation();
                    }

                    if (getScrollY() == 0) {
                        mState = State.NORMAL;
                    }

                    isMoving = false;
                    break;
                default:
                    break;

            }
        }

        // 禁止控件本身的滑动.
        boolean isHandle = isMoving;
        if (!isMoving) {
            try {
                isHandle = super.onTouchEvent(ev);
            } catch (Exception e) {
                Log.w(LOG_TAG, e);
            }
        }
        return isHandle;
    }

    /**
     * 执行移动动画
     *
     * @param event
     */
    private void doActionMove(MotionEvent event) {
        // 当滚动到顶部时，将状态设置为正常，避免先向上拖动再向下拖动到顶端后首次触摸不响应的问题
        if (getScrollY() == 0) {
            mState = State.NORMAL;

            // 滑动经过顶部初始位置时，修正Touch down的坐标为当前Touch点的坐标
            if (isTop) {
                isTop = false;
                mStartPoint.y = event.getY();
            }
        }

        float deltaY = event.getY() - mStartPoint.y;
        Log.i("888", "248 deltaY == " + deltaY);
        // 对于首次Touch操作要判断方位：UP OR DOWN
        if (deltaY < 0 && mState == State.NORMAL) {
            mState = State.UP;
        } else if (deltaY >= 0 && mState == State.NORMAL) {
            mState = State.DOWN;
        }

        if (mState == State.UP) {
            deltaY = deltaY < 0 ? deltaY : 0;

            isMoving = false;

        } else if (mState == State.DOWN) {
            if (getScrollY() <= deltaY) {
                isMoving = true;
            }
            deltaY = deltaY < 0 ? 0 : (deltaY > mHeaderHeight ? mHeaderHeight : deltaY);
        }

        Log.i("888", "269 isMoving == " + isMoving);

        if (isMoving) {
            // 初始化content view矩形
            if (mContentRect.isEmpty()) {
                // 保存正常的布局位置
                mContentRect.set(mContentView.getLeft(), mContentView.getTop(), mContentView.getRight(),
                        mContentView.getBottom());
            }

            // 计算header移动距离(手势移动的距离*阻尼系数*0.5)
            float headerMoveHeight = deltaY * 0.5f * SCROLL_RATIO;
            mCurrentTop = (int) (mInitTop + headerMoveHeight);
            mCurrentBottom = (int) (mInitBottom + headerMoveHeight);

            // 计算content移动距离(手势移动的距离*阻尼系数)
            float contentMoveHeight = deltaY * SCROLL_RATIO;

            // 修正content移动的距离，避免超过header的底边缘
            int headerBottom = mCurrentBottom - mHeaderVisibleHeight;
            int top = (int) (mContentRect.top + contentMoveHeight);
            int bottom = (int) (mContentRect.bottom + contentMoveHeight);

            if (top <= headerBottom) {
                // 移动content view
                mContentView.layout(mContentRect.left, top, mContentRect.right, bottom);
                Log.i("888", "mContentRect.left === " + mContentRect.left + "  top == " + top + "   right == " + mContentRect.right
                        + " bottom == " + bottom);
                Log.i("888", "mHeader.getLeft() === " + mHeader.getLeft() + "  mCurrentTop == " + mCurrentTop + " " +
                        "  mHeader.getRight() == " + mHeader.getRight()
                        + " mCurrentBottom == " + mCurrentBottom);
                // 移动header view
//                mHeader.layout(mHeader.getLeft(), mCurrentTop, mHeader.getRight(), mCurrentBottom);
                interpolate(mHeader, mContentView, top);
            }
        }
    }

    private RectF mRect1 = new RectF();
    private RectF mRect2 = new RectF();
    private AccelerateDecelerateInterpolator mSmoothInterpolator;


    private void interpolate(View view1, View view2, float y) {
        float ratio = clamp(y / mHeaderHeight, 0.0f, 1.0f);
        float interpolation = mSmoothInterpolator.getInterpolation(ratio);
        getOnScreenRect(mRect1, view1);
        getOnScreenRect(mRect2, view2);
        float scaleX = 1.0F + interpolation * (mRect2.width() / mRect1.width() - 1.0F);
        float scaleY = 1.0F + interpolation * (mRect2.height() / mRect1.height() - 1.0F);
        float translationX = 0.5F * (interpolation * (mRect2.left + mRect2.right - mRect1.left - mRect1.right));
        float translationY = 0.5F * (interpolation * (mRect2.top + mRect2.bottom - mRect1.top - mRect1.bottom));

        Log.i("888", "scaleX === " + scaleX + "  scaleY == " + scaleY + "  translationX == " + translationX + " translationY === " + translationY + "  y  ==  " + y);

//        view1.setTranslationX(translationY);
        view1.setTranslationY(translationY - y);
//        view1.setPivotX(translationY);
//        view1.setPivotY(translationY - y);
        view1.setScaleX(scaleY);
        view1.setScaleY(scaleY);
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }

    private RectF getOnScreenRect(RectF rect, View view) {
        rect.set(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        return rect;
    }


    private void rollBackAnimation() {
        TranslateAnimation tranAnim = new TranslateAnimation(0, 0,
                Math.abs(mInitTop - mCurrentTop), 0);
        tranAnim.setDuration(200);
//        mHeader.startAnimation(tranAnim);
//
//        mHeader.layout(mHeader.getLeft(), mInitTop, mHeader.getRight(), mInitBottom);

        // 开启移动动画
        TranslateAnimation innerAnim = new TranslateAnimation(0, 0, mContentView.getTop(), mContentRect.top);
        innerAnim.setDuration(100);
        mContentView.startAnimation(innerAnim);
        mContentView.layout(mContentRect.left, mContentRect.top, mContentRect.right, mContentRect.bottom);

        mContentRect.setEmpty();

        interpolate(mHeader, mContentView, mContentRect.top);

        // 回调监听器
        if (mCurrentTop > mInitTop + TURN_DISTANCE && mOnTurnListener != null) {
            mOnTurnListener.onTurn();
        }
    }

    /**
     * 是否需要开启动画
     */
    private boolean isNeedAnimation() {
        return !mContentRect.isEmpty() && isMoving;
    }

    /**
     * 翻转事件监听器
     *
     * @author markmjw
     */
    public interface OnTurnListener {
        /**
         * 翻转回调方法
         */
        void onTurn();
    }
}
