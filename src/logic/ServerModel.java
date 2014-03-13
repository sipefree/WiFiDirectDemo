package logic;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ui.PlayerInfo;
import utils.Preconditions;

import logic.CommunicationBus.BusManager;

import com.squareup.otto.Bus;


/**
 * Contains the game model used on the server side.
 */
public class ServerModel implements BusManager {

	/**
	 * Specifies initial amount of tokens each player has at the beginning of the game.
	 */
	public static final int INITIAL_AMOUNT = 3000;
	public static final int BIG_BLIND = 50;
	public static final int SMALL_BLIND = BIG_BLIND / 2;
	public static final int MAX_PLAYER_NUMBER = 8;
	public static final int MIN_PLAYER_NUMBER = 2;
	private static final int COMMON_CARDS_COUNT = 5;

	private final List<Player> mPlayers;
	private final Bus mBus;
	private CardDeck mDeck;
	private int mTablePool;
	private GameState mGameState;
	private final List<Card> mCommonCards = new ArrayList<Card>();
	private Player mDealer;
	private Player mPlayerWithSmallBlind;
	private Player mPlayerWithBigBlind;
	private Player mCurrentPlayer;
	private Player mLastPlayer;
	private int mCurrentBid;
	private String mTableMessage;
	private PokerTableEvent mSavedTableState;

	public ServerModel() {
		mPlayers = new ArrayList<Player>();
		mBus = CommunicationBus.getInstance();
		mGameState = GameState.NOT_STARTED;
	}

	void addPlayer(Player player) {
		mPlayers.add(player);
	}

	void removePlayer(Player player) {
		Player toRemove = null;

		for (Player p : mPlayers) {
			if (p.getNodeName().equalsIgnoreCase(player.getNodeName())) {
				toRemove = p;
				break;
			}
		}

		mPlayers.remove(toRemove);
	}

	Card[] getCommonCards() {
		return mCommonCards.toArray(new Card[mCommonCards.size()]);
	}

	/**
	 * Return player with the specified node name
	 * 
	 * @param nodeName
	 *            name of the player's node to be returned
	 * @return player with the specified name or null if such player does not exist.
	 */
	Player getPlayer(String nodeName) {
		for (Player player : mPlayers) {
			if (player.getNodeName().equalsIgnoreCase(nodeName)) {
				return player;
			}
		}
		return null;
	}

	List<Player> getPlayers() {
		return new ArrayList<Player>(mPlayers);
	}

	/**
	 * Returns list of playing players.
	 * 
	 * @return list of playing players
	 */
	List<Player> getPlayingPlayers() {
		final List<Player> playingPlayers = new LinkedList<Player>();
		for (Player player : mPlayers) {
			if (player.isPlaying()) {
				playingPlayers.add(player);
			}
		}
		return playingPlayers;
	}

	List<Player> getSittingPlayers() {
		final List<Player> sittingPlayers = new LinkedList<Player>();
		for (Player player : mPlayers) {
			if (player.isSitting()) {
				sittingPlayers.add(player);
			}
		}
		return sittingPlayers;
	}

	int getPlayingPlayersCount() {
		return getPlayingPlayers().size();
	}

	int getSittingPlayersCount() {
		int i = 0;
		for (Player player : mPlayers) {
			if (player.isSitting()) {
				++i;
			}
		}
		return i;
	}

	int getSittingAndEligibleToPlayPlayersCount() {
		int i = 0;
		for (Player player : mPlayers) {
			if (player.isSitting() && player.getAmount() > 0) {
				++i;
			}
		}
		return i;
	}

	/**
	 * Returns next card from the deck.
	 * 
	 * @return next card from the deck
	 */
	Card getNextCard() {
		return mDeck.popCard();
	}

	CardDeck getDeck() {
		return mDeck;
	}

	GameState getGameState() {
		return mGameState;
	}

	void setGameState(GameState gameState) {
		mGameState = gameState;
	}

	/**
	 * Returns next player that should make the move.
	 * 
	 * @param player
	 *            current {@link Player}
	 * @return next {@link Player}
	 */
	Player getNextPlayer(Player player) {
		final int currentPlayerIndex = mPlayers.indexOf(player);
		Preconditions.checkState(currentPlayerIndex != -1);

		if (currentPlayerIndex + 1 < mPlayers.size()) {
			return mPlayers.get(currentPlayerIndex + 1);
		} else {
			return mPlayers.get(0);
		}
	}

	void addCommonCard(Card card) {
		Preconditions.checkState(mCommonCards.size() < COMMON_CARDS_COUNT);
		mCommonCards.add(card);
	}

	/**
	 * Triggers refresh of poker table.
	 */
	void triggerPokerTableEvent() {
		mBus.post(buildPokerTableEvent());
	}

	/**
	 * Build {@link PokerTableEvent} from the information contained in the model.
	 * 
	 * @return event used for refreshing poker table
	 */
	private PokerTableEvent buildPokerTableEvent() {
		final List<PlayerInfo> playersInfo = new ArrayList<PlayerInfo>();
		Player p;
		for (int i = 0; i < getSittingPlayersCount(); i++) {
			p = getSittingPlayers().get(i);
			playersInfo.add(fromPlayer(p, i + 1));
		}

		final List<Card> commonCards = new ArrayList<Card>();
		for (Card card : mCommonCards) {
			commonCards.add(new Card(card));
		}
		return new PokerTableEvent(playersInfo, commonCards, mTablePool, mTableMessage);
	}

	/**
	 * Saves current table state as the {@link PokerTabelEvent} which can be used to refresh table view.
	 */
	void saveCurrentTableState() {
		mSavedTableState = buildPokerTableEvent();
	}

	/**
	 * If there is any saved table, redraws table view using this state.
	 */
	void redrawSavedTableState() {
		if (mSavedTableState != null) {
			mBus.post(mSavedTableState);
		}
	}

	void updateAndRedrawSavedTableState(String playerName, boolean isSitting) {
		mSavedTableState.changePlayerState(playerName, isSitting);
		redrawSavedTableState();
	}

	void takeAmount(String nodeName, int amount) {
		getPlayer(nodeName).takeAmount(amount);
		triggerPokerTableEvent();
	}

	void giveAmount(String nodeName, int amount) {
		getPlayer(nodeName).addAmount(amount);
		triggerPokerTableEvent();
	}

	int getTablePool() {
		return mTablePool;
	}

	void increaseTablePool(int increaseBy) {
		mTablePool += increaseBy;
		triggerPokerTableEvent();
	}

	void setDealer(Player player) {
		mDealer = player;
	}

	Player getDealer() {
		return mDealer;
	}

	void setPlayerWithSmallBlind(Player player) {
		mPlayerWithSmallBlind = player;
	}

	Player getPlayerWithSmallBlind() {
		return mPlayerWithSmallBlind;
	}

	Player getCurrentPlayer() {
		return mCurrentPlayer;
	}

	void setCurrentPlayer(Player currentPlayer) {
		mCurrentPlayer = currentPlayer;
	}

	void setPlayerWithBigBlind(Player player) {
		mPlayerWithBigBlind = player;
	}

	Player getPlayerWithBigBlind() {
		return mPlayerWithBigBlind;
	}

	void setCurrentBid(int currentBid) {
		Preconditions.checkArgument(currentBid >= mCurrentBid);
		mCurrentBid = currentBid;
	}

	boolean isDealer(Player p) {
		return mDealer == null ? false : mDealer.equals(p);
	}

	boolean hasSmallBlind(Player p) {
		return mPlayerWithSmallBlind == null ? false : mPlayerWithSmallBlind.equals(p);
	}

	boolean hasBigBlind(Player p) {
		return mPlayerWithBigBlind == null ? false : mPlayerWithBigBlind.equals(p);
	}

	boolean hasTurn(Player p) {
		return mCurrentPlayer == null ? false : mCurrentPlayer.equals(p);
	}

	/**
	 * Creates {@link PlayerInfo} that is used to present {@link Player}s state on the table.
	 * 
	 * @param p
	 *            {@link Player} whose state should be presented
	 * @param position
	 *            {@link Player}s position on the table
	 * @return information used to draw player's state on the table
	 */
	private PlayerInfo fromPlayer(Player p, int position) {
		final List<Card> cards = new ArrayList<Card>();
		cards.add(p.getCard(0) == null ? null : new Card(p.getCard(0)));
		cards.add(p.getCard(1) == null ? null : new Card(p.getCard(1)));

		return new PlayerInfo(position + 1, p.getName(), p.getAmount(), cards, p.isSitting(), isDealer(p),
				hasBigBlind(p), hasSmallBlind(p), hasTurn(p), p.isPlaying(), p.getLastPlayersAction(),
				p.getShouldShowCards(), p.getCurrentBid());
	}

	int getCurrentBid() {
		return mCurrentBid;
	}

	/**
	 * Performs table initialization.
	 */
	void initTable() {
		mDeck = new CardDeck();
		triggerPokerTableEvent();
	}

	void setTableMessage(String tableMessage) {
		mTableMessage = tableMessage;
	}

	/**
	 * Clear state of the current game.
	 */
	void clearGame() {
		for (Player player : mPlayers) {
			if (player.hasCards()) {
				for (Card c : player.getCards()) {
					mDeck.pushCard(c);
				}
			}
			player.clearGame();
		}

		for (Card card : mCommonCards) {
			mDeck.pushCard(card);
		}
		mCommonCards.clear();
		mDeck.shuffleCards();
		mTablePool = 0;
		mCurrentBid = 0;
	}

	@Override
	public void startBus() {
		mBus.register(this);
	}

	@Override
	public void stopBus() {
		mBus.unregister(this);
	}

	/**
	 * Indicates what state the game currently is at.
	 */
	public enum GameState {
		//@formatter:off
		NOT_STARTED,
		PRE_FLOP,
		FLOP,
		TURN,
		RIVER,
		GAME_FINISHED;
		//@formatter:on

		/**
		 * Returns {@link GameState} that follows current state.
		 * 
		 * @param currentState
		 *            of the game
		 * @return next {@link GameState}
		 */
		public static GameState getNextState(GameState currentState) {
			switch (currentState) {
			case NOT_STARTED:
				return PRE_FLOP;
			case PRE_FLOP:
				return FLOP;
			case FLOP:
				return TURN;
			case TURN:
				return RIVER;
			case RIVER:
				return GAME_FINISHED;
			case GAME_FINISHED:
				return PRE_FLOP;
			default:
				throw new IllegalArgumentException(currentState.name());
			}
		}

	}

	public Player getLastPlayer() {
		return mLastPlayer;
	}

	public void setLastPlayer(Player lastPlayer) {
		mLastPlayer = lastPlayer;
	}

}
