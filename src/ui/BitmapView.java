package ui;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * View with a bitmap.
 */
public class BitmapView extends View {

	private Bitmap mBitmap;
	private RectF mBitmapRect;

	/**
	 * Constructs the bitmap view.
	 * 
	 * @param context
	 *            the application context
	 * @param attrs
	 *            the view's attributes
	 */
	public BitmapView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private static final Paint BITMAP_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG
			| Paint.DITHER_FLAG);

	@Override
	protected void onDraw(Canvas canvas) {
		if (mBitmap != null) {
			canvas.drawBitmap(mBitmap, null, mBitmapRect, BITMAP_PAINT);
		}
	}

	public void setBitmap(Bitmap bitmap) {
		mBitmap = bitmap;

		if (mBitmap != null) {
			final float bitmapRatio = (float) mBitmap.getWidth() / mBitmap.getHeight();
			final float viewRatio = (float) getWidth() / getHeight();

			final float width;
			final float height;
			final float minSize = Math.min(getWidth(), getHeight());

			if (Float.compare(viewRatio, 1.0f) < 0) {
				if (Float.compare(bitmapRatio, 1.0f) < 0) {
					width = minSize;
					height = minSize / bitmapRatio;
				} else {
					width = minSize * bitmapRatio;
					height = minSize;
				}
			} else {
				if (Float.compare(bitmapRatio, 1.0f) < 0) {
					width = minSize * bitmapRatio;
					height = minSize;
				} else {
					width = minSize;
					height = minSize / bitmapRatio;
				}
			}

			mBitmapRect = new RectF(0, 0, width, height);
		}
		invalidate();
	}
}
