package extra;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import extra.GameActivity.GameActivityEvent.TokenEvent.TokenType;

/**
 * Cache used for caching bitmaps used in the game.
 */
public class BitmapCache {

	private static final String EXTENSION = ".png";
	private final LruCache<String, Bitmap> mMemoryCache;
	private final AssetManager mAssetManager;

	/**
	 * Creates new {@link BitmapCache} with the specified size.
	 * 
	 * @param maxCacheSize
	 *            of the {@link LruCache}
	 * @param assetManager
	 *            manager used for loading bitmaps from assets
	 */
	public BitmapCache(int maxCacheSize, AssetManager assetManager) {
		mMemoryCache = new LruCache<String, Bitmap>(maxCacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getByteCount();
			}
		};
		mAssetManager = assetManager;
	}

	private void addBitmapToCache(String key, Bitmap bitmap) {
		if (getBitmapFromCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	private Bitmap getBitmapFromCache(String key) {
		return mMemoryCache.get(key);
	}

	private Bitmap getBitmapFromAsset(String strName) {
		InputStream istr;
		Bitmap bitmap = null;
		try {
			istr = mAssetManager.open(strName + EXTENSION);
			bitmap = BitmapFactory.decodeStream(istr);
		} catch (IOException e) {
			return null;
		}
		return bitmap;
	}

	/**
	 * 
	 * Returns {@link Bitmap} from the cache. When {@link Bitmap} is not found it tries to load it from the assets.
	 * 
	 * @param bitmapName
	 *            of the {@link Bitmap} to be loaded
	 * @return bitmap with the specified name or null if bitmap is not found
	 */
	public Bitmap getBitmap(String bitmapName) {
		Bitmap bitmap;

		if ((bitmap = getBitmapFromCache(bitmapName)) != null) {
			return bitmap;
		} else {
			bitmap = getBitmapFromAsset(bitmapName);
			Preconditions.checkNotNull(bitmap);
			addBitmapToCache(bitmapName, bitmap);
			return getBitmapFromCache(bitmapName);
		}

	}

	/**
	 * Returns bitmap for the specified {@link Card}.
	 * 
	 * @param card
	 *            whose {@link Bitmap} should be returned
	 * @return {@link Bitmap} of the specified {@link Card}
	 */
	public Bitmap getBitmapForCard(Card card) {
		return getBitmap(card.getColor().name().toLowerCase(Locale.getDefault()) + "_"
				+ card.getRank().name().toLowerCase(Locale.getDefault()));
	}

	/**
	 * Returns {@link Bitmap} for the specified client card.
	 * 
	 * @param card
	 *            whose (@link Bitmap} should be returned
	 * @return {@link Bitmap} of the specified client card
	 */
	public Bitmap getBitmapForClientCard(Card card) {
		// @formatter:off
		final StringBuilder builder = new StringBuilder("client_")
			.append(card.getColor().name().toLowerCase(Locale.getDefault()))
			.append('_')
			.append(card.getRank().name().toLowerCase(Locale.getDefault()));
		// @formatter:on

		return getBitmap(builder.toString());
	}

	/**
	 * Returns {@link Bitmap} for the client token.
	 * 
	 * @param tokenType
	 *            of the token
	 * @return {@link Bitmap} of the token
	 */
	public Bitmap getBitmapForClientToken(TokenType tokenType) {
		final StringBuilder builder = new StringBuilder("client_");

		switch (tokenType) {
		case BIG_BLIND:
			builder.append("big_blind");
			break;
		case SMALL_BLIND:
			builder.append("small_blind");
			break;
		case DEALER:
			builder.append("dealer");
			break;
		case DEALER_WITH_SMALL_BLIND:
			builder.append("dealer_with_small_blind");
			break;
		default:
			throw new IllegalArgumentException();
		}

		return getBitmap(builder.toString());
	}
}
