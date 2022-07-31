package com.ad.facebeauty.utills;

import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;

import java.util.List;
import java.util.Objects;

public class LipDraw {
    private static int alphaColor(int color, int alpha) {
        return (color & 0x00FFFFFF) | alpha;
    }

    private static void drawLipPerfect(Canvas canvas, Path lipPath, int color, int alpha) {
        //most 70% alpha
        if (alpha > 80) {
            alpha = (int) (alpha * 0.9f + 0.5f);
        }

        alpha = (int) (Color.alpha(color) * ((float) alpha / 255)) << 24;
        color = alphaColor(color, alpha);
        final PointF position = new PointF();
        float blur_radius = 5;

        Bitmap mask = createMask(lipPath, color, blur_radius, position);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        canvas.drawBitmap(mask, position.x, position.y, paint);
    }

    private static Bitmap createMask(final Path path, int color, float blur_radius, PointF position) {
        if (path == null || path.isEmpty())
            return null;

        RectF bounds = new RectF();
        path.computeBounds(bounds, true);
        bounds.inset(-blur_radius, -blur_radius);

        int width = (int) bounds.width();
        int height = (int) bounds.height();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);  // mutable
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setMaskFilter(new BlurMaskFilter(blur_radius, BlurMaskFilter.Blur.NORMAL));
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        path.offset(-bounds.left, -bounds.top);

        canvas.drawPath(path, paint);

        if (position != null) {
            position.x = bounds.left;
            position.y = bounds.top;
        }
        return bitmap;
    }

    public Bitmap drawFace(Bitmap bitmap, Face face, int color, int alpha) {
        Bitmap tempBitmap = null;
        try {
            tempBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(tempBitmap);
            canvas.drawBitmap(bitmap, 0f, 0f, null);

            Path mainPath = new android.graphics.Path();

            List<PointF> pointsUpperTop = Objects.requireNonNull(face.getContour(FaceContour.UPPER_LIP_TOP)).getPoints();
            mainPath.moveTo(pointsUpperTop.get(0).x, pointsUpperTop.get(0).y);
            for (PointF pointF : pointsUpperTop) {
                float px = pointF.x;
                float py = pointF.y;
                mainPath.lineTo(px, py);
            }
            List<PointF> pointBottmTop = Objects.requireNonNull(face.getContour(FaceContour.UPPER_LIP_BOTTOM)).getPoints();
            mainPath.moveTo(pointBottmTop.get(0).x, pointBottmTop.get(0).y);
            for (PointF pointF : pointBottmTop) {
                float px = pointF.x;
                float py = pointF.y;
                mainPath.lineTo(px, py);
            }
            List<PointF> pointBottomTop = Objects.requireNonNull(face.getContour(FaceContour.LOWER_LIP_TOP)).getPoints();
            mainPath.moveTo(pointBottomTop.get(0).x, pointBottomTop.get(0).y);
            for (PointF pointF : pointBottomTop) {
                float px = pointF.x;
                float py = pointF.y;
                mainPath.lineTo(px, py);
            }
            List<PointF> pointBottomLower = Objects.requireNonNull(face.getContour(FaceContour.LOWER_LIP_BOTTOM)).getPoints();
            mainPath.moveTo(pointBottomLower.get(0).x, pointBottomLower.get(0).y);
            for (PointF pointF : pointBottomLower) {
                float px = pointF.x;
                float py = pointF.y;
                mainPath.lineTo(px, py);
            }
            LipDraw.drawLipPerfect(canvas, mainPath, color, alpha);

//            newTempBitmap = tempBitmap;
//            imageView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tempBitmap;
    }
}
//        for draw line
//        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        paint.setColor(Color.RED);
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth(15.0f);