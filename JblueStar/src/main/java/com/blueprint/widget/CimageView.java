package com.blueprint.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 制作圆形图片类
 * android:textSize="1dp"  ===  mBorderWidth
 * android:textColor="@color/colorAccent"  ===  mBorderColor
 */
@SuppressLint("AppCompatCustomView")
public class CimageView extends ImageView {

    private static final int[] ATTRS = new int[]{android.R.attr.strokeWidth, android.R.attr.strokeColor};
//    private static final int[] ATTRS2 = new int[]{android.R.attr.maxLines, android.R.attr.textColor};//取不到color
    private static final int[] ATTRS2 = new int[]{android.R.attr.textSize, android.R.attr.textColor};

    private static final ScaleType SCALE_TYPE = ScaleType.CENTER_CROP;
    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final int COLORDRAWABLE_DIMENSION = 1;

    private static final int DEFAULT_BORDER_WIDTH = 0;
    private static final int DEFAULT_BORDER_COLOR = Color.DKGRAY;

    private final RectF mDrawableRect = new RectF();
    private final RectF mBorderRect = new RectF();

    private final Matrix mShaderMatrix = new Matrix();
    private final Paint mBitmapPaint = new Paint();
    private final Paint mBorderPaint = new Paint();

    private int mBorderColor = DEFAULT_BORDER_COLOR;
    private int mBorderWidth = DEFAULT_BORDER_WIDTH;

    private Bitmap mBitmap;
    private BitmapShader mBitmapShader;
    private int mBitmapWidth;
    private int mBitmapHeight;

    private float mDrawableRadius;
    private float mBorderRadius;

    private boolean mReady;
    private boolean mSetupPending;

    public CimageView(Context context){
        super(context);
    }

    public CimageView(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }

    public CimageView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
//        setLayerType(LAYER_TYPE_SOFTWARE, null);
        TypedArray sa2 = context.obtainStyledAttributes(attrs, ATTRS2);
        mBorderWidth = sa2.getDimensionPixelSize(0,DEFAULT_BORDER_WIDTH);
        mBorderColor = sa2.getColor(1, DEFAULT_BORDER_COLOR);
        sa2.recycle();

        super.setScaleType(SCALE_TYPE);
        mReady = true;
        if(mSetupPending) {
            setup();
            mSetupPending = false;
        }
    }

    @Override
    public ScaleType getScaleType(){
        return SCALE_TYPE;
    }

    @Override
    public void setScaleType(ScaleType scaleType){
        if(scaleType != SCALE_TYPE) {
            throw new IllegalArgumentException(String.format("ScaleType %s not supported.", scaleType));
        }
    }

    @Override
    protected void onDraw(Canvas canvas){
        if(getDrawable() == null) {
            return;
        }
        mBitmapPaint.setMaskFilter(new BlurMaskFilter(20, BlurMaskFilter.Blur.OUTER));
        canvas.drawCircle(getWidth()/2, getHeight()/2, mDrawableRadius, mBitmapPaint);
        if(mBorderWidth != 0) {
            canvas.drawCircle(getWidth()/2, getHeight()/2, mBorderRadius, mBorderPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);
        setup();
    }

    public int getBorderColor(){
        return mBorderColor;
    }

    public void setBorderColor(int borderColor){
        if(borderColor == mBorderColor) {
            return;
        }

        mBorderColor = borderColor;
        mBorderPaint.setColor(mBorderColor);
        invalidate();
    }

    public int getBorderWidth(){
        return mBorderWidth;
    }

    public void setBorderWidth(int borderWidth){
        if(borderWidth == mBorderWidth) {
            return;
        }

        mBorderWidth = borderWidth;
        setup();
    }

    @Override
    public void setImageBitmap(Bitmap bm){
        super.setImageBitmap(bm);
        mBitmap = bm;
        setup();
    }

    @Override
    public void setImageDrawable(Drawable drawable){
        super.setImageDrawable(drawable);
        mBitmap = getBitmapFromDrawable(drawable);
        setup();
    }

    @Override
    public void setImageResource(int resId){
        super.setImageResource(resId);
        mBitmap = getBitmapFromDrawable(getDrawable());
        setup();
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable){
        if(drawable == null) {
            return null;
        }

        if(drawable instanceof BitmapDrawable) {
            return ( (BitmapDrawable)drawable ).getBitmap();
        }

        try {
            Bitmap bitmap;

            if(drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG);
            }else {
                bitmap = Bitmap
                        .createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
            }
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }catch(OutOfMemoryError e) {
            return null;
        }
    }

    private void setup(){
        if(!mReady) {
            mSetupPending = true;
            return;
        }

        if(mBitmap == null) {
            return;
        }

        mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setShader(mBitmapShader);

        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStrokeWidth(mBorderWidth);

        mBitmapHeight = mBitmap.getHeight();
        mBitmapWidth = mBitmap.getWidth();

        mBorderRect.set(0, 0, getWidth(), getHeight());
        mBorderRadius = Math.min(( mBorderRect.height()-mBorderWidth )/2, ( mBorderRect.width()-mBorderWidth )/2);

        mDrawableRect
                .set(mBorderWidth, mBorderWidth, mBorderRect.width()-mBorderWidth, mBorderRect.height()-mBorderWidth);
        mDrawableRadius = Math.min(mDrawableRect.height()/2, mDrawableRect.width()/2);

        updateShaderMatrix();
        invalidate();
    }

    private void updateShaderMatrix(){
        float scale;
        float dx = 0;
        float dy = 0;

        mShaderMatrix.set(null);

        if(mBitmapWidth*mDrawableRect.height()>mDrawableRect.width()*mBitmapHeight) {
            scale = mDrawableRect.height()/(float)mBitmapHeight;
            dx = ( mDrawableRect.width()-mBitmapWidth*scale )*0.5f;
        }else {
            scale = mDrawableRect.width()/(float)mBitmapWidth;
            dy = ( mDrawableRect.height()-mBitmapHeight*scale )*0.5f;
        }

        mShaderMatrix.setScale(scale, scale);
        mShaderMatrix.postTranslate((int)( dx+0.5f )+mBorderWidth, (int)( dy+0.5f )+mBorderWidth);
        mBitmapShader.setLocalMatrix(mShaderMatrix);
    }

}