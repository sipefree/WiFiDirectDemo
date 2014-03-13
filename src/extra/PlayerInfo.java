package extra;
import java.util.List;

/**
 * Contains information about player's current game state. Used for refresing poker table view.
 */
public class PlayerInfo {

	private final int mPosition;
	private final String mPlayerName;
	private final int mTokens;
	private final List<Card> mCards;
	private final boolean mDealer;
	private final boolean mBigBlind;
	private final boolean mSmallBlind;
	private boolean mSitting;
	private final boolean mPlayersTurn;
	private final boolean mPlaying;
	private final String mLastAction;
	private final boolean mShouldShowCards;
	private final int mCurrentBid;

	public PlayerInfo(int position, String name, int tokens, List<Card> cards, boolean isSitting, boolean isDealer,
			boolean isBigBlind, boolean isSmallBlind, boolean isPlayersTurn, boolean isPlaying, String lastAction,
			boolean shouldShowCards, int currentBid) {
		mPosition = position;
		mPlayerName = name;
		mTokens = tokens;
		mCards = cards;
		mSitting = isSitting;
		mDealer = isDealer;
		mBigBlind = isBigBlind;
		mSmallBlind = isSmallBlind;
		mPlayersTurn = isPlayersTurn;
		mPlaying = isPlaying;
		mLastAction = lastAction;
		mShouldShowCards = shouldShowCards;
		mCurrentBid = currentBid;
	}

	public int getPosition() {
		return mPosition;
	}

	public String getPlayerName() {
		return mPlayerName;
	}

	public int getTokens() {
		return mTokens;
	}

	public List<Card> getCards() {
		return mCards;
	}

	public String getLastAction() {
		return mLastAction;
	}

	public boolean isDealer() {
		return mDealer;
	}

	public boolean isBigBlind() {
		return mBigBlind;
	}

	public boolean isSmallBlind() {
		return mSmallBlind;
	}

	public boolean isSitting() {
		return mSitting;
	}

	public void setSitting(boolean isSitting) {
		mSitting = isSitting;
	}

	public boolean isTurn() {
		return mPlayersTurn;
	}

	public boolean isPlaying() {
		return mPlaying;
	}

	public String getPlayerDescription() {
		return Integer.toString(mTokens) + "  |  " + "[" + mLastAction + "]";
	}

	public boolean getShouldShowCards() {
		return mShouldShowCards;
	}

	public int getCurrentBid() {
		return mCurrentBid;
	}

}
