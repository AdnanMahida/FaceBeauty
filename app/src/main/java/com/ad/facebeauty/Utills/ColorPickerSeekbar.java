package com.ad.facebeauty.Utills;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.widget.SeekBar;


public class ColorPickerSeekbar extends androidx.appcompat.widget.AppCompatSeekBar implements SeekBar.OnSeekBarChangeListener {
    private OnColorSeekBarChangeListener mOnColorSeekbarChangeListener;

    public void setOnColorSeekbarChangeListener(OnColorSeekBarChangeListener listener) {
        this.mOnColorSeekbarChangeListener = listener;
    }

    public ColorPickerSeekbar(Context context) {
        super(context);
        setOnSeekBarChangeListener(this);
    }


    public ColorPickerSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnSeekBarChangeListener(this);
    }

    public ColorPickerSeekbar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnSeekBarChangeListener(this);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        init();
    }

    /**
     * Initializes the color seekbar with the gradient
     */
    public void init() {
        LinearGradient colorGradient = new LinearGradient(0.f, 0.f, this.getMeasuredWidth() - this.getThumb().getIntrinsicWidth(), 0.f,
                new int[]{0xFF000000, 0xFF0000FF, 0xFF00FF00, 0xFF00FFFF,
                        0xFFFF0000, 0xFFFF00FF, 0xFFFFFF00, 0xFFFFFFFF},
                null, Shader.TileMode.CLAMP
        );
        ShapeDrawable shape = new ShapeDrawable(new RectShape());
        shape.getPaint().setShader(colorGradient);
        this.setProgressDrawable(shape);
        this.setMax(256 * 7 - 1);
    }

    /**
     * A callback that notifies clients when the color has been changed.
     * This includes changes that were initiated by the user through a
     * touch gesture or arrow key/trackball as well as changes that were initiated programmatically.
     */
    public interface OnColorSeekBarChangeListener {

        /**
         * Notification that the color has changed. Clients can use the fromUser parameter
         * to distinguish user-initiated changes from those that occurred programmatically.
         * Parameters:
         *
         * @param seekBar The SeekBar whose progress has changed
         * @param color   The current color-int from alpha, red, green, blue components.
         * @param b       True if the progress change was initiated by the user.
         */
        void onColorChanged(SeekBar seekBar, int color, boolean b);

        /**
         * Notification that the user has started a touch gesture.
         * Clients may want to use this to disable advancing the seekbar.
         *
         * @param seekBar The SeekBar in which the touch gesture began
         */
        void onStartTrackingTouch(SeekBar seekBar);

        /**
         * Notification that the user has finished a touch gesture.
         * Clients may want to use this to re-enable advancing the seekbar.
         *
         * @param seekBar The SeekBar in which the touch gesture finished
         */
        void onStopTrackingTouch(SeekBar seekBar);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (null == mOnColorSeekbarChangeListener) {
            return;
        }

        int r = 0;
        int g = 0;
        int b = 0;

        if (progress < 256) {
            b = progress;
        } else if (progress < 256 * 2) {
            g = progress % 256;
            b = 256 - progress % 256;
        } else if (progress < 256 * 3) {
            g = 255;
            b = progress % 256;
        } else if (progress < 256 * 4) {
            r = progress % 256;
            g = 256 - progress % 256;
            b = 256 - progress % 256;
        } else if (progress < 256 * 5) {
            r = 255;
            g = 0;
            b = progress % 256;
        } else if (progress < 256 * 6) {
            r = 255;
            g = progress % 256;
            b = 256 - progress % 256;
        } else if (progress < 256 * 7) {
            r = 255;
            g = 255;
            b = progress % 256;
        }
        mOnColorSeekbarChangeListener.onColorChanged(seekBar, Color.argb(255, r, g, b), fromUser);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (null == mOnColorSeekbarChangeListener) {
            return;
        }
        mOnColorSeekbarChangeListener.onStartTrackingTouch(seekBar);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (null == mOnColorSeekbarChangeListener) {
            return;
        }
        mOnColorSeekbarChangeListener.onStopTrackingTouch(seekBar);
    }

}
