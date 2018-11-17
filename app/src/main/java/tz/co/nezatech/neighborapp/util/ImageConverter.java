package tz.co.nezatech.neighborapp.util;

import android.graphics.*;

public class ImageConverter {
    public static Bitmap getRoundedCorner(Bitmap in, int px) {
        Bitmap output = Bitmap.createBitmap(in.getWidth(), in.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        int color = 0xff424242;
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, in.getWidth(), in.getHeight());
        RectF rectF = new RectF(rect);
        float roundPx = px;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(in, rect, rect, paint);

        return output;
    }
}
