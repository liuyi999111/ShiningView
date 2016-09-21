package com.compilesense.liuyi.shiningview;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

/**
 * Created by shenjingyuan002 on 16/9/20.
 */

public class ShiningLayout extends FrameLayout {
    public static final String TAG = "ShiningView";
    private static final PorterDuffXfermode DST_IN = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);

    private int[] colors = new int[]{Color.TRANSPARENT, Color.WHITE, Color.TRANSPARENT};
    private float[] positions = new float[]{0.25f, 0.5f, 0.75f};
    private int[] radialColors = new int[]{Color.WHITE, Color.TRANSPARENT};
    private float[] radialPositions = new float[]{0f, 0.75f};

    private ValueAnimator mAnimator;
    private float xOffSet = 0;
    private float yOffSet = 0;

    private Paint mAlphaPaint;
    private Paint mMaskPaint;
    private Bitmap mRenderMaskBitmap;
    private Bitmap mRenderUnmaskBitmap;
    private Bitmap mMaskBitmap;
    private int maskWidth = -1,maskHeight = -1;

    boolean showShininess = true;
    private int mDuration = 1000;
    private DirectionOfMovement mDirectionOfMovement = DirectionOfMovement.left2right;
    private ShininessType mShininessType = ShininessType.linearity;

    public enum DirectionOfMovement{
        left2right, top2bottom, right2left, bottom2top
    }

    public enum ShininessType{
        linearity, spotlight
    }

    private float rotationAngle;
    private float shininessSize;


    public ShiningLayout(Context context) {
        super(context);
    }

    public ShiningLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShiningLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ShiningLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    public void start(){
        getShiningAnimator().start();
    }
    public void stop(){
        getShiningAnimator().cancel();
    }
    public void cancel(){
        getShiningAnimator().cancel();
        showShininess = false;
        mMaskBitmap.recycle();
        mRenderMaskBitmap.recycle();
        mRenderUnmaskBitmap.recycle();
        invalidate();
    }

    public void setDuration(int mDuration) {
        this.mDuration = mDuration;
    }

    public void setAlpha(float alphaF){
        int alphaI =(int) (Math.min(1,Math.max(alphaF,0)) * 0xFF);
        Log.d(TAG,"alphaI:"+alphaI);
        mAlphaPaint.setAlpha(alphaI);
    }

    public void setDirectionOfMovement(DirectionOfMovement mDirectionOfMovement) {
        this.mDirectionOfMovement = mDirectionOfMovement;
        if (mMaskBitmap != null){
            mMaskBitmap.recycle();
        }
        invalidate();
    }

    /**
     * 设置闪光的大小
     * @param shininessSize 相对大小 0-1,在线性模式下为闪光的宽度,在聚光灯模式下为闪光的半径
     */
    public void setShininessSize(float shininessSize) {
        shininessSize = Math.min(Math.max(shininessSize,0),1);
        this.shininessSize = shininessSize;
        if (mMaskBitmap != null){
            mMaskBitmap.recycle();
        }
        invalidate();
    }

    public void setShininessType(ShininessType mShininessType) {
        this.mShininessType = mShininessType;
    }

    private void init(){
        mAlphaPaint = new Paint();
        setAlpha(0.3f);
        mMaskPaint = new Paint();
        mMaskPaint.setAntiAlias(true);
        mMaskPaint.setDither(true);
        mMaskPaint.setFilterBitmap(true);
        mMaskPaint.setXfermode(DST_IN);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        init();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (!showShininess){
            super.dispatchDraw(canvas);
            return;
        }
        dispatchDrawUsingBitmap(canvas);
    }

    private void dispatchDrawUsingBitmap(Canvas canvas){
        Bitmap unmaskBitmap = getRenderUnmaskBitmap();
        Bitmap maskBitmap = getRenderMaskBitmap();

        if (mShininessType == ShininessType.linearity){
            drawUnmaskBitmap(new Canvas(unmaskBitmap));
            canvas.drawBitmap(unmaskBitmap, 0, 0, mAlphaPaint);
        }

        drawMaskBitmap(new Canvas(maskBitmap));
        canvas.drawBitmap(maskBitmap, 0, 0, null);
    }

    private void drawMaskBitmap(Canvas mbCanvas){
        Bitmap mask = getMaskBitmap();
        if (mask == null){
            Log.e(TAG,"maskBitmap == null");
            return;
        }
        mbCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        super.dispatchDraw(mbCanvas);

        mbCanvas.drawBitmap(mask, xOffSet, yOffSet, mMaskPaint);
    }

    private void drawUnmaskBitmap(Canvas ubCanvas){
        ubCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        super.dispatchDraw(ubCanvas);
    }

    private Bitmap getRenderMaskBitmap(){
        if (mRenderMaskBitmap == null){
            mRenderMaskBitmap = createBitmap(getWidth(),getHeight());
        }
        return mRenderMaskBitmap;
    }

    private Bitmap getRenderUnmaskBitmap(){
        if (mRenderUnmaskBitmap == null){
            mRenderUnmaskBitmap = createBitmap(getWidth(),getHeight());
        }
        return mRenderUnmaskBitmap;
    }

    private Bitmap getMaskBitmap(){
        if (mMaskBitmap != null && !mMaskBitmap.isRecycled()){
            return mMaskBitmap;
        }
        setMaskSize(mDirectionOfMovement);
        Shader shader;
        try {
            shader = createShader(mDirectionOfMovement);
            mMaskBitmap = createBitmap(maskWidth, maskHeight);
            Canvas canvas = new Canvas(mMaskBitmap);
            Paint paint = new Paint();
            paint.setShader(shader);
            canvas.drawRect(0, 0, maskWidth, maskHeight, paint);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return mMaskBitmap;
    }

    private void setMaskSize(DirectionOfMovement direction){
        if (direction == DirectionOfMovement.left2right || direction == DirectionOfMovement.right2left){
            maskWidth = 3 * getWidth();
            maskHeight = getHeight();
        }else if (direction == DirectionOfMovement.top2bottom  || direction == DirectionOfMovement.bottom2top){
            maskWidth = getWidth();
            maskHeight = 3 * getHeight();
        }
    }

    private Shader createShader(DirectionOfMovement direction) throws Exception{
        if (maskHeight < 0 || maskWidth < 0){
            throw new Exception("createShader,mask size error");
        }

        Shader shader = null;

        if (mShininessType == ShininessType.linearity){
            float startPointX, startPointY, endPointX, endPointY;
            float sw = shininessSize /2;

            if ( direction == DirectionOfMovement.left2right || direction == DirectionOfMovement.right2left){
                startPointX = maskWidth * (1f/2f - sw);
                startPointY = 0;
                endPointX = maskWidth * (1f/2f + sw);
                endPointY = maskHeight;
            }else if (direction == DirectionOfMovement.top2bottom || direction == DirectionOfMovement.bottom2top){
                startPointX = 0;
                startPointY = maskHeight * (1f/2f - sw);
                endPointX = 0;
                endPointY = maskHeight * (1f/2f + sw);
            }else {
                return null;
            }

            shader = new LinearGradient(startPointX,startPointY,endPointX,endPointY,
                    colors,
                    positions,
                    Shader.TileMode.CLAMP);
        }else if (mShininessType == ShininessType.spotlight){

            float centerX = maskWidth/2;
            float centerY = maskHeight/2;
            float sr = Math.max(getWidth(),getHeight());

            if ( direction == DirectionOfMovement.left2right || direction == DirectionOfMovement.right2left){
                sr = sr * shininessSize;

            }else if (direction == DirectionOfMovement.top2bottom || direction == DirectionOfMovement.bottom2top){
                sr = sr * shininessSize;
            }

            shader = new RadialGradient(centerX, centerY, sr, radialColors, radialPositions, Shader.TileMode.CLAMP);
        }

        return shader;
    }

    private Bitmap createBitmap(int width, int height){
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    }

    private Animator getShiningAnimator(){
        if (mAnimator != null){
            return mAnimator;
        }
        mAnimator = ValueAnimator.ofFloat(0f, 1f);
        mAnimator.setDuration(mDuration);
        mAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        mAnimator.setRepeatMode(ObjectAnimator.RESTART);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = Math.max(0.0f, Math.min(1.0f, (Float) animation.getAnimatedValue()));
                handOffSetValue(value);
                invalidate();
            }
        });
        return mAnimator;
    }

    private void handOffSetValue(float value){
        int w = getWidth();
        int h = getHeight();
        switch (mDirectionOfMovement){
            case left2right:
                xOffSet = 2 * w * (value - 1f);
                break;

            case right2left:
                xOffSet = -2 * w * value;
                break;

            case top2bottom:
                yOffSet = 2 * h * (value - 1f);
                break;

            case bottom2top:
                yOffSet = -2 * h * value;
                break;
        }
    }
}
