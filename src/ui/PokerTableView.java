package ui;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.LinearLayout;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.example.android.wifidirect.R;
import events.BusEvent;
import logic.Card;
import logic.CommunicationBus;
import logic.CommunicationBus.BusManager;
import logic.ServerModel;
import utils.BitmapCache;

/**
 * Represents poker table which is displayed on the TV screen.
 */
@SuppressLint("ViewConstructor")
public class PokerTableView extends SurfaceView implements SurfaceHolder.Callback, BusManager {

	private final Bus mBus;
	private final SurfaceHolder mHolder;
	private final Paint mNormalPlayerBackgroundColor;
	private final Paint mBorderColor;
	private final Paint mActivePlayerBorderColor;
	private final Paint mActivePlayerBackgroundColor;
	private final Paint mNormalPlayerFontColor;
	private final Paint mTablePotFont;
	private final Paint mTableNameFont;
	private final Paint mActivePlayerFont;
	private final Paint mNormalPlayerPointsFont;
	private final Paint mActivePlayerPointsFont;
	private final Paint mCurrentBidFont;
	private final Paint mBitmapPaint;

	private final BitmapCache mMemoryCache;
	private final String mTableName;

	public PokerTableView(Context context, BitmapCache memoryCache,
			String tableName) {
		super(context);
		mBus = CommunicationBus.getInstance();
		final Resources resources = context.getResources();
		setLayoutParams(new LinearLayout.LayoutParams(Const.TABLE_WIDTH, Const.TABLE_HEIGHT));
		mHolder = getHolder();
		mHolder.setFormat(PixelFormat.RGBA_8888);
		mHolder.addCallback(this);

		mTableName = tableName;
		mMemoryCache = memoryCache;

		mTablePotFont = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTablePotFont.setColor(Color.WHITE);
		mTablePotFont.setTextSize(Const.POT_FONT_SIZE);
		mTablePotFont.setFakeBoldText(true);

		mTableNameFont = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTableNameFont.setColor(Color.WHITE);
		mTableNameFont.setTextSize(Const.TABLE_NAME_FONT_SIZE);
		mTableNameFont.setFakeBoldText(true);

		mBorderColor = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBorderColor.setColor(resources.getColor(R.color.normal_player_border));
		mBorderColor.setStyle(Style.STROKE);
		mBorderColor.setStrokeWidth(Const.DEFAULT_BORDER_WIDTH);

		mActivePlayerBorderColor = new Paint(Paint.ANTI_ALIAS_FLAG);
		mActivePlayerBorderColor.setColor(resources.getColor(R.color.active_player_border));
		mActivePlayerBorderColor.setStyle(Style.STROKE);
		mActivePlayerBorderColor.setStrokeWidth(Const.ACTIVE_PLAYER_BORDER_WIDTH);

		mNormalPlayerBackgroundColor = new Paint(Paint.ANTI_ALIAS_FLAG);
		mNormalPlayerBackgroundColor.setColor(resources.getColor(R.color.normal_player_background));

		mActivePlayerBackgroundColor = new Paint(Paint.ANTI_ALIAS_FLAG);
		mActivePlayerBackgroundColor.setColor(resources.getColor(R.color.dealer_background));

		mNormalPlayerFontColor = new Paint(Paint.ANTI_ALIAS_FLAG);
		mNormalPlayerFontColor.setColor(resources.getColor(R.color.normal_player_font));
		mNormalPlayerFontColor.setTextSize(Const.DEFAULT_FONT_SIZE);

		mNormalPlayerPointsFont = new Paint(Paint.ANTI_ALIAS_FLAG);
		mNormalPlayerPointsFont.setColor(resources.getColor(R.color.normal_player_points_font));
		mNormalPlayerPointsFont.setTextSize(Const.DEFAULT_FONT_SIZE);
		mNormalPlayerPointsFont.setFakeBoldText(true);

		mActivePlayerFont = new Paint(Paint.ANTI_ALIAS_FLAG);
		mActivePlayerFont.setColor(resources.getColor(R.color.dealer_font));
		mActivePlayerFont.setTextSize(Const.DEFAULT_FONT_SIZE);

		mActivePlayerPointsFont = new Paint(Paint.ANTI_ALIAS_FLAG);
		mActivePlayerPointsFont.setColor(resources.getColor(R.color.dealer_points_font));
		mActivePlayerPointsFont.setTextSize(Const.DEFAULT_FONT_SIZE);
		mActivePlayerPointsFont.setFakeBoldText(true);

		mCurrentBidFont = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCurrentBidFont.setColor(resources.getColor(R.color.current_bid_font));
		mCurrentBidFont.setTextSize(Const.CURRENT_BID_FONT_SIZE);
		mCurrentBidFont.setFakeBoldText(true);

		mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

		setFocusable(true);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		final Canvas c = holder.lockCanvas(null);
		c.drawBitmap(mMemoryCache.getBitmap(Const.TABLE_BITMAP_NAME), 0, 0, null);

		Point namePosition;
		for (int i = 0; i < ServerModel.MAX_PLAYER_NUMBER; i++) {
			namePosition = Const.getTextBoxPosition(i + 1);
			c.drawRect(namePosition.x, namePosition.y, namePosition.x + Const.PLAYER_NAME_WIDTH, namePosition.y
					+ Const.PLAYER_NAME_HEIGHT, mBorderColor);
			c.drawRect(namePosition.x, namePosition.y, namePosition.x + Const.PLAYER_NAME_WIDTH, namePosition.y
					+ Const.PLAYER_NAME_HEIGHT, mNormalPlayerBackgroundColor);
		}

		c.drawText(mTableName, Const.TABLE_NAME_X, Const.TABLE_NAME_Y, mTableNameFont);

		holder.unlockCanvasAndPost(c);
	}

	private final Matrix mMatrix = new Matrix();
	private final Matrix mCardsMatrix = new Matrix();

	/**
	 * Refreshes poker table displayed on the TV. Triggered by the {@link PokerTableEvent}.
	 * 
	 * @param event
	 *            that contains information to be displayed on the table.
	 */
	@Subscribe
	public void refresh(PokerTableEvent event) {
		final Canvas c = mHolder.lockCanvas();
		if (c != null) {
			// Draw table background.
			c.drawBitmap(mMemoryCache.getBitmap(Const.TABLE_BITMAP_NAME), 0, 0, null);
			// Draw common cards.
			final List<Card> commonCards = event.getCommonCards();
			for (int i = 0; i < commonCards.size(); i++) {
				c.drawBitmap(mMemoryCache.getBitmapForCard(commonCards.get(i)), i * Const.CARDS_SPACE
						+ Const.COMMON_CARDS_X + i * Const.CARD_WIDTH, Const.COMMON_CARDS_Y, null);
			}
			// Draw players.
			PlayerInfo info;
			Point namePosition;
			int x;
			int y;

			for (int i = 0; i < ServerModel.MAX_PLAYER_NUMBER; i++) {
				namePosition = Const.getTextBoxPosition(i + 1);
				x = Const.getBoxX(i + 1);
				y = Const.getBoxY(i + 1);
				mMatrix.reset();
				mMatrix.setRotate(Const.getBoxDegree(i + 1));
				mMatrix.postTranslate(x, y);

				if (i < event.getPlayers().size()) {
					info = event.getPlayers().get(i);

					if (info.isTurn()) {
						c.drawRect(namePosition.x, namePosition.y, namePosition.x + Const.PLAYER_NAME_WIDTH,
								namePosition.y + Const.PLAYER_NAME_HEIGHT, mActivePlayerBorderColor);
					} else {
						c.drawRect(namePosition.x, namePosition.y, namePosition.x + Const.PLAYER_NAME_WIDTH,
								namePosition.y + Const.PLAYER_NAME_HEIGHT, mBorderColor);
					}

					Paint playerBoxBackground;
					Paint playerNameFont;
					Paint playerPointsFont;
					if (info.isTurn()) {
						playerBoxBackground = mActivePlayerBackgroundColor;
						playerNameFont = mActivePlayerFont;
						playerPointsFont = mActivePlayerPointsFont;
					} else {
						playerBoxBackground = mNormalPlayerBackgroundColor;
						playerNameFont = mNormalPlayerFontColor;
						playerPointsFont = mNormalPlayerPointsFont;
					}

					c.drawRect(namePosition.x, namePosition.y, namePosition.x + Const.PLAYER_NAME_WIDTH, namePosition.y
							+ Const.PLAYER_NAME_HEIGHT, playerBoxBackground);
					String text = info.getPlayerName();
					float centeredPosition = namePosition.x
							+ (Const.PLAYER_NAME_WIDTH - mNormalPlayerFontColor.measureText(text)) / 2;
					c.drawText(text, centeredPosition, namePosition.y + 30, playerNameFont);

					text = info.getPlayerDescription();
					centeredPosition = namePosition.x
							+ (Const.PLAYER_NAME_WIDTH - mNormalPlayerFontColor.measureText(text)) / 2;
					c.drawText(text, centeredPosition, namePosition.y + 60, playerPointsFont);

					if (!info.getShouldShowCards()) {
						if (info.isSmallBlind() && !info.isDealer()) {
							c.drawBitmap(mMemoryCache.getBitmap(Const.SMALL_BLIND_BITMAP_NAME), mMatrix, mBitmapPaint);
						} else if (info.isBigBlind()) {
							c.drawBitmap(mMemoryCache.getBitmap(Const.BIG_BLIND_BITMAP_NAME), mMatrix, mBitmapPaint);
						} else if (info.isSmallBlind() && info.isDealer()) {
							c.drawBitmap(mMemoryCache.getBitmap(Const.DEALER_WITH_SMALL_BLIND_BITMAP_NAME), mMatrix,
									mBitmapPaint);
						} else if (info.isDealer()) {
							c.drawBitmap(mMemoryCache.getBitmap(Const.DEALER_BITMAP_NAME), mMatrix, mBitmapPaint);
						}
					}

					final List<Card> cards = info.getCards();
					if (info.getShouldShowCards()) {
						mCardsMatrix.reset();
						mCardsMatrix.setRotate(Const.getBoxDegree(i + 1));
						mCardsMatrix.postTranslate(Const.getCardXAtShowdown(i + 1, 0),
								Const.getCardYAtShowdown(i + 1, 0));
						c.drawBitmap(mMemoryCache.getBitmapForCard(cards.get(0)), mCardsMatrix, null);
						mCardsMatrix.reset();
						mCardsMatrix.setRotate(Const.getBoxDegree(i + 1));
						mCardsMatrix.postTranslate(Const.getCardXAtShowdown(i + 1, 1),
								Const.getCardYAtShowdown(i + 1, 1));
						c.drawBitmap(mMemoryCache.getBitmapForCard(cards.get(1)), mCardsMatrix, null);
					} else {
						if (info.isPlaying()) {
							c.drawBitmap(mMemoryCache.getBitmap(Const.CARDS_BITMAP_NAME), mMatrix, null);
							float currentBidPosition = namePosition.x
									+ (Const.PLAYER_NAME_WIDTH - mCurrentBidFont.measureText(Integer.toString(info
											.getCurrentBid()))) / 2;
							if (!info.getShouldShowCards()) {
								if (i == 0 || i == 7) {
									c.drawText(Integer.toString(info.getCurrentBid()), namePosition.x + 230,
											namePosition.y + 40, mCurrentBidFont);
								} else if (i == 3 || i == 4) {
									c.drawText(Integer.toString(info.getCurrentBid()), namePosition.x - 120,
											namePosition.y + 30, mCurrentBidFont);
								} else if (i == 1 || i == 2 || i == 5 || i == 6) {
									c.drawText(Integer.toString(info.getCurrentBid()), currentBidPosition,
											namePosition.y + 120, mCurrentBidFont);
								}
							}
						}

						final int tokens = info.getTokens();
						if (info.isSitting()) {
							if (tokens > 4500) {
								c.drawBitmap(mMemoryCache.getBitmap(Const.BIG_CHIPS_BITMAP_NAME), mMatrix, null);
							} else if (tokens > 1500) {
								c.drawBitmap(mMemoryCache.getBitmap(Const.MEDIUM_CHIPS_BITMAP_NAME), mMatrix, null);
							} else if (tokens > 0) {
								c.drawBitmap(mMemoryCache.getBitmap(Const.SMALL_CHIPS_BITMAP_NAME), mMatrix, null);
							}
						}
					}

				} else {
					c.drawRect(namePosition.x, namePosition.y, namePosition.x + Const.PLAYER_NAME_WIDTH, namePosition.y
							+ Const.PLAYER_NAME_HEIGHT, mBorderColor);
					c.drawRect(namePosition.x, namePosition.y, namePosition.x + Const.PLAYER_NAME_WIDTH, namePosition.y
							+ Const.PLAYER_NAME_HEIGHT, mNormalPlayerBackgroundColor);
				}
			}

			// Draw rest.
			c.drawText(Integer.toString(event.getTablePool()), Const.TABLE_POT_X, Const.TABLE_POT_Y, mTablePotFont);
			final String message = event.getMessage();
			if (message != null && !message.isEmpty()) {
				final float centeredPosition = Const.COMMON_CARDS_X
						+ (Const.CARD_WIDTH * 5 + Const.CARDS_SPACE * 4 - mTablePotFont.measureText(message)) / 2;
				c.drawText(message, centeredPosition, Const.COMMON_CARDS_Y + Const.CARD_HEIGHT + 40, mTablePotFont);
			}
			c.drawText(mTableName, Const.TABLE_NAME_X, Const.TABLE_NAME_Y, mTableNameFont);

			mHolder.unlockCanvasAndPost(c);
		}
	}

	@Override
	public void startBus() {
		mBus.register(this);
	}

	@Override
	public void stopBus() {
		mBus.unregister(this);
	}

	public static class PokerTableEvent extends BusEvent {

		private static final long serialVersionUID = 20130322L;

		private final transient List<PlayerInfo> mPlayers;
		private final List<Card> mCommonCards;
		private final int mTablePool;
		private final String mMessage;

		public PokerTableEvent(List<PlayerInfo> players, List<Card> commonCards, int tablePool, String message) {
			super();
			mPlayers = players;
			mCommonCards = commonCards;
			mTablePool = tablePool;
			mMessage = message;
		}

		public List<PlayerInfo> getPlayers() {
			return mPlayers;
		}

		public List<Card> getCommonCards() {
			return mCommonCards;
		}

		public int getTablePool() {
			return mTablePool;
		}

		public String getMessage() {
			return mMessage;
		}

		public void changePlayerState(String playerName, boolean isSitting) {
			for (PlayerInfo player : mPlayers) {
				if (player.getPlayerName().equals(playerName)) {
					player.setSitting(isSitting);
				}
			}
		}

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// Does nothing intentionally.
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// Does nothing intentionally.
	}

}
