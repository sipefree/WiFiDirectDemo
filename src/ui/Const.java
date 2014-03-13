package ui;

import android.graphics.Point;
import android.util.Pair;
import android.util.SparseArray;

import logic.ServerModel;

/**
 * Contains different constant values used across the application.
 */
public class Const {

	// Table dimensions.
	public static final int TABLE_WIDTH = 1920;
	public static final int TABLE_HEIGHT = 1080;
	// Position of the jackpot.
	public static final int TABLE_POT_X = 950;
	public static final int TABLE_POT_Y = 450;
	// Position of the table name.
	public static final int TABLE_NAME_X = 10;
	public static final int TABLE_NAME_Y = 40;
	// Size and positions of common cards.
	public static final int CARD_WIDTH = 150;
	public static final int CARD_HEIGHT = 225;
	public static final int COMMON_CARDS_X = 537;
	public static final int COMMON_CARDS_Y = 478;
	public static final int CARDS_SPACE = 24;
	// Size and position fo player box and its elements.
	public static final int PLAYER_BOX_HEIGHT = 200;
	public static final int PLAYER_BOX_WIDTH = 350;
	public static final int PLAYER_BOX_CARDS_X = 0;
	public static final int PLAYER_BOX_CARDS_Y = 0;
	public static final int PLAYER_BOX_DEALER_X = 0;
	public static final int PLAYER_BOX_DEALER_Y = 210;
	public static final int PLAYER_BOX_BLINDS_X = 280;
	public static final int PLAYER_BOX_BLINDS_Y = 130;
	public static final int PLAYER_BOX_TOKENS_X = 0;
	public static final int PLAYER_BOX_TOKENS_Y = 0;
	// Position and angle of consecutive player boxes.
	private static final int PLAYER_BOX_FIRST_X = 1000;
	private static final int PLAYER_BOX_FIRST_Y = 90;
	private static final int PLAYER_BOX_FIRST_DEGREES = 0;
	private static final int PLAYER_BOX_SECOND_X = 1600;
	private static final int PLAYER_BOX_SECOND_Y = 150;
	private static final int PLAYER_BOX_SECOND_DEGREES = 45;
	private static final int PLAYER_BOX_THIRD_X = 1845;
	private static final int PLAYER_BOX_THIRD_Y = 684;
	private static final int PLAYER_BOX_THIRD_DEGREES = 135;
	private static final int PLAYER_BOX_FOURTH_X = 1355;
	private static final int PLAYER_BOX_FOURTH_Y = 990;
	private static final int PLAYER_BOX_FOURTH_DEGREES = 180;
	private static final int PLAYER_BOX_FIFTH_X = 915;
	private static final int PLAYER_BOX_FIFTH_Y = 990;
	private static final int PLAYER_BOX_FIFTH_DEGREES = 180;
	private static final int PLAYER_BOX_SIXTH_X = 320;
	private static final int PLAYER_BOX_SIXTH_Y = 930;
	private static final int PLAYER_BOX_SIXTH_DEGREES = 225;
	private static final int PLAYER_BOX_SEVENTH_X = 74;
	private static final int PLAYER_BOX_SEVENTH_Y = 395;
	private static final int PLAYER_BOX_SEVENTH_DEGREES = 315;
	private static final int PLAYER_BOX_EIGHT_X = 565;
	private static final int PLAYER_BOX_EIGHT_Y = 90;
	private static final int PLAYER_BOX_EIGHT_DEGREES = 0;
	// Position and angle of consecutive player boxes.
	private static final int PLAYER_CARDS_FIRST_X = 1000;
	private static final int PLAYER_CARDS_FIRST_Y = 90;
	private static final int PLAYER_CARDS_SECOND_X = 1500;
	private static final int PLAYER_CARDS_SECOND_Y = 250;
	private static final int PLAYER_CARDS_THIRD_X = 1500;
	private static final int PLAYER_CARDS_THIRD_Y = 620;
	private static final int PLAYER_CARDS_FOURTH_X = 1000;
	private static final int PLAYER_CARDS_FOURTH_Y = 760;
	private static final int PLAYER_CARDS_FIFTH_X = 565;
	private static final int PLAYER_CARDS_FIFTH_Y = 760;
	private static final int PLAYER_CARDS_SIXTH_X = 75;
	private static final int PLAYER_CARDS_SIXTH_Y = 610;
	private static final int PLAYER_CARDS_SEVENTH_X = 75;
	private static final int PLAYER_CARDS_SEVENTH_Y = 250;
	private static final int PLAYER_CARDS_EIGHT_X = 565;
	private static final int PLAYER_CARDS_EIGHT_Y = 90;
	// Size and position of player's name boxes
	public static final int PLAYER_NAME_HEIGHT = 67;
	public static final int PLAYER_NAME_WIDTH = 205;
	private static final int PLAYER_NAME_FIRST_X = 1065;
	private static final int PLAYER_NAME_FIRST_Y = 12;
	private static final int PLAYER_NAME_SECOND_X = 1690;
	private static final int PLAYER_NAME_SECOND_Y = 150;
	private static final int PLAYER_NAME_THIRD_X = 1690;
	private static final int PLAYER_NAME_THIRD_Y = 865;
	private static final int PLAYER_NAME_FOURTH_X = 1060;
	private static final int PLAYER_NAME_FOURTH_Y = 1000;
	private static final int PLAYER_NAME_FIFTH_X = 630;
	private static final int PLAYER_NAME_FIFTH_Y = 1000;
	private static final int PLAYER_NAME_SIXTH_X = 25;
	private static final int PLAYER_NAME_SIXTH_Y = 865;
	private static final int PLAYER_NAME_SEVENTH_X = 25;
	private static final int PLAYER_NAME_SEVENTH_Y = 150;
	private static final int PLAYER_NAME_EIGHT_X = 625;
	private static final int PLAYER_NAME_EIGHT_Y = 12;
	// Font and border sizes.
	public static final int DEFAULT_FONT_SIZE = 25;
	public static final int POT_FONT_SIZE = 35;
	public static final int CURRENT_BID_FONT_SIZE = 35;
	public static final int TABLE_NAME_FONT_SIZE = 40;
	public static final float DEFAULT_BORDER_WIDTH = 2.0f;
	public static final float ACTIVE_PLAYER_BORDER_WIDTH = 10.0f;
	// Bitmap names
	public static final String TABLE_BITMAP_NAME = "table";
	public static final String SMALL_BLIND_BITMAP_NAME = "small_blind";
	public static final String BIG_BLIND_BITMAP_NAME = "big_blind";
	public static final String DEALER_BITMAP_NAME = "dealer";
	public static final String DEALER_WITH_SMALL_BLIND_BITMAP_NAME = "dealer_with_small_blind";
	public static final String CARDS_BITMAP_NAME = "cards";
	public static final String SMALL_CHIPS_BITMAP_NAME = "chips1";
	public static final String MEDIUM_CHIPS_BITMAP_NAME = "chips2";
	public static final String BIG_CHIPS_BITMAP_NAME = "chips3";

	private Const() {
		// No. op.
	}

	/**
	 * Returns X coordinate of player's box.
	 * 
	 * @param playerPosition
	 *            of the player at the table
	 * @return X coordinate of player's box
	 */
	public static int getBoxX(int playerPosition) {
		switch (playerPosition) {
		case 1:
			return PLAYER_BOX_FIRST_X;
		case 2:
			return PLAYER_BOX_SECOND_X;
		case 3:
			return PLAYER_BOX_THIRD_X;
		case 4:
			return PLAYER_BOX_FOURTH_X;
		case 5:
			return PLAYER_BOX_FIFTH_X;
		case 6:
			return PLAYER_BOX_SIXTH_X;
		case 7:
			return PLAYER_BOX_SEVENTH_X;
		case 8:
			return PLAYER_BOX_EIGHT_X;
		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Returns Y coordinate of player's box.
	 * 
	 * @param playerPosition
	 *            of the player at the table
	 * @return Y coordinate of player's box
	 */
	public static int getBoxY(int playerPosition) {
		switch (playerPosition) {
		case 1:
			return PLAYER_BOX_FIRST_Y;
		case 2:
			return PLAYER_BOX_SECOND_Y;
		case 3:
			return PLAYER_BOX_THIRD_Y;
		case 4:
			return PLAYER_BOX_FOURTH_Y;
		case 5:
			return PLAYER_BOX_FIFTH_Y;
		case 6:
			return PLAYER_BOX_SIXTH_Y;
		case 7:
			return PLAYER_BOX_SEVENTH_Y;
		case 8:
			return PLAYER_BOX_EIGHT_Y;
		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Returns degree of player's box.
	 * 
	 * @param playerPosition
	 *            of the player at the table
	 * @return degree of player's box
	 */
	public static int getBoxDegree(int playerPosition) {
		switch (playerPosition) {
		case 1:
			return PLAYER_BOX_FIRST_DEGREES;
		case 2:
			return PLAYER_BOX_SECOND_DEGREES;
		case 3:
			return PLAYER_BOX_THIRD_DEGREES;
		case 4:
			return PLAYER_BOX_FOURTH_DEGREES;
		case 5:
			return PLAYER_BOX_FIFTH_DEGREES;
		case 6:
			return PLAYER_BOX_SIXTH_DEGREES;
		case 7:
			return PLAYER_BOX_SEVENTH_DEGREES;
		case 8:
			return PLAYER_BOX_EIGHT_DEGREES;
		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Returns {@link Point} indication coordinate of player's text box.
	 * 
	 * @param playerPosition
	 *            on the table.
	 * @return
	 */
	public static Point getTextBoxPosition(int playerPosition) {
		switch (playerPosition) {
		case 1:
			return new Point(PLAYER_NAME_FIRST_X, PLAYER_NAME_FIRST_Y);
		case 2:
			return new Point(PLAYER_NAME_SECOND_X, PLAYER_NAME_SECOND_Y);
		case 3:
			return new Point(PLAYER_NAME_THIRD_X, PLAYER_NAME_THIRD_Y);
		case 4:
			return new Point(PLAYER_NAME_FOURTH_X, PLAYER_NAME_FOURTH_Y);
		case 5:
			return new Point(PLAYER_NAME_FIFTH_X, PLAYER_NAME_FIFTH_Y);
		case 6:
			return new Point(PLAYER_NAME_SIXTH_X, PLAYER_NAME_SIXTH_Y);
		case 7:
			return new Point(PLAYER_NAME_SEVENTH_X, PLAYER_NAME_SEVENTH_Y);
		case 8:
			return new Point(PLAYER_NAME_EIGHT_X, PLAYER_NAME_EIGHT_Y);
		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Returns X coordinate of player's cards.
	 * 
	 * @param playerPosition
	 *            on the table
	 * @return
	 */
	public static int getCardsX(int playerPosition) {
		switch (playerPosition) {
		case 1:
			return PLAYER_CARDS_FIRST_X;
		case 2:
			return PLAYER_CARDS_SECOND_X;
		case 3:
			return PLAYER_CARDS_THIRD_X;
		case 4:
			return PLAYER_CARDS_FOURTH_X;
		case 5:
			return PLAYER_CARDS_FIFTH_X;
		case 6:
			return PLAYER_CARDS_SIXTH_X;
		case 7:
			return PLAYER_CARDS_SEVENTH_X;
		case 8:
			return PLAYER_CARDS_EIGHT_X;
		default:
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Returns Y coordinate of player's cards.
	 * 
	 * @param playerPosition
	 *            on the table
	 * @return
	 */
	public static int getCardsY(int playerPosition) {
		switch (playerPosition) {
		case 1:
			return PLAYER_CARDS_FIRST_Y;
		case 2:
			return PLAYER_CARDS_SECOND_Y;
		case 3:
			return PLAYER_CARDS_THIRD_Y;
		case 4:
			return PLAYER_CARDS_FOURTH_Y;
		case 5:
			return PLAYER_CARDS_FIFTH_Y;
		case 6:
			return PLAYER_CARDS_SIXTH_Y;
		case 7:
			return PLAYER_CARDS_SEVENTH_Y;
		case 8:
			return PLAYER_CARDS_EIGHT_Y;
		default:
			throw new IllegalArgumentException();
		}
	}

	private static final SparseArray<Pair<Integer, Integer>> CARD_X_AT_SHOWDOWN = new SparseArray<Pair<Integer, Integer>>(
			ServerModel.MAX_PLAYER_NUMBER);
	private static final SparseArray<Pair<Integer, Integer>> CARD_Y_AT_SHOWDOWN = new SparseArray<Pair<Integer, Integer>>(
			ServerModel.MAX_PLAYER_NUMBER);

	static {
		final int aSqrtTwo = Const.CARD_WIDTH + Const.CARDS_SPACE;
		final double a = aSqrtTwo / Math.sqrt(2);
		final int x = (int) (Const.CARD_WIDTH + Const.CARDS_SPACE - a);
		final int y = (int) a;

		CARD_X_AT_SHOWDOWN.append(1, new Pair<Integer, Integer>(getCardsX(1), getCardsX(1) + aSqrtTwo));
		CARD_Y_AT_SHOWDOWN.append(1, new Pair<Integer, Integer>(getCardsY(1), getCardsY(1)));

		CARD_X_AT_SHOWDOWN.append(2, new Pair<Integer, Integer>(getCardsX(2) + x, getCardsX(2) + aSqrtTwo));
		CARD_Y_AT_SHOWDOWN.append(2, new Pair<Integer, Integer>(getCardsY(2) - y, getCardsY(2)));

		CARD_X_AT_SHOWDOWN.append(3, new Pair<Integer, Integer>(getCardsX(3) + x + y, getCardsX(3) + y + aSqrtTwo));
		CARD_Y_AT_SHOWDOWN.append(3, new Pair<Integer, Integer>(getCardsY(3) + 2 * y, getCardsY(3) + y));

		CARD_X_AT_SHOWDOWN.append(4, new Pair<Integer, Integer>(getCardsX(4) + Const.CARD_WIDTH, getCardsX(4)
				+ Const.CARD_WIDTH + aSqrtTwo));
		CARD_Y_AT_SHOWDOWN.append(4, new Pair<Integer, Integer>(getCardsY(4) + Const.CARD_HEIGHT, getCardsY(4)
				+ Const.CARD_HEIGHT));

		CARD_X_AT_SHOWDOWN.append(5, new Pair<Integer, Integer>(getCardsX(5) + Const.CARD_WIDTH, getCardsX(5)
				+ Const.CARD_WIDTH + aSqrtTwo));
		CARD_Y_AT_SHOWDOWN.append(5, new Pair<Integer, Integer>(getCardsY(5) + Const.CARD_HEIGHT, getCardsY(5)
				+ Const.CARD_HEIGHT));

		CARD_X_AT_SHOWDOWN.append(6, new Pair<Integer, Integer>(getCardsX(6) + x + y, getCardsX(6) + y + aSqrtTwo));
		CARD_Y_AT_SHOWDOWN.append(6, new Pair<Integer, Integer>(getCardsY(6) + 2 * y, getCardsY(6) + 3 * y));

		CARD_X_AT_SHOWDOWN.append(7, new Pair<Integer, Integer>(getCardsX(7) + x, getCardsX(7) + aSqrtTwo));
		CARD_Y_AT_SHOWDOWN.append(7, new Pair<Integer, Integer>(getCardsY(7) + y, getCardsY(7)));

		CARD_X_AT_SHOWDOWN.append(8, new Pair<Integer, Integer>(getCardsX(8), getCardsX(8) + aSqrtTwo));
		CARD_Y_AT_SHOWDOWN.append(8, new Pair<Integer, Integer>(getCardsY(8), getCardsY(8)));
	}

	public static int getCardXAtShowdown(int playerPosition, int cardPosition) {
		return cardPosition == 0 ? CARD_X_AT_SHOWDOWN.get(playerPosition).first : CARD_X_AT_SHOWDOWN
				.get(playerPosition).second;
	}

	public static int getCardYAtShowdown(int playerPosition, int cardPosition) {
		return cardPosition == 0 ? CARD_Y_AT_SHOWDOWN.get(playerPosition).first : CARD_Y_AT_SHOWDOWN
				.get(playerPosition).second;
	}

}
