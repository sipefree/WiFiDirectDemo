package extra;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.res.Resources;
import android.util.Pair;
import chord.ChordMessage;
import chord.ChordMessage.MessageType;
import chord.GameChord.ClientDisconnectedEvent;

import com.example.android.wifidirect.R;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import extra.ClientModel.ClientModelEvent.BlindEvent.BlindType;
import extra.CommunicationBus.BusManager;
import extra.GameActivity.GameActivityEvent.SittingPlayersChangedEvent;
import extra.PokerLogicController.PokerLogicEvent.AllInEvent;
import extra.PokerLogicController.PokerLogicEvent.BiddingEvent;
import extra.PokerLogicController.PokerLogicEvent.FoldEvent;
import extra.PokerLogicController.PokerLogicEvent.SitEvent;
import extra.PokerLogicController.PokerLogicEvent.StandEvent;
import extra.PokerLogicController.PokerLogicEvent.UsernameEvent;
import extra.PokerUtils.GameResult;
import extra.ServerModel.GameState;

/**
 * Encapsulates whole logic of the poker game.
 */
public class PokerLogicController implements BusManager {
	private final Bus mBus;
	private final ServerModel mModel;
	private final Resources mResources;

	public PokerLogicController(Model model, Resources resources) {
		mBus = CommunicationBus.getInstance();
		mModel = model.getServerModel();
		mModel.initTable();
		mResources = resources;
	}

	/**
	 * Performs initial actions at the start of the game.
	 * 
	 * @param event
	 *            that triggers game start
	 */
	@Subscribe
	public void startGame(StartGameEvent event) {
		mModel.triggerPokerTableEvent();
		mModel.setGameState(GameState.PRE_FLOP);
		mModel.setTableMessage(mResources.getString(R.string.game_started));

		// Set the sitting players as playing one.
		for (Player player : mModel.getPlayers()) {
			if (player.isSitting()) {
				if (player.getAmount() > 0) {
					player.setPlaying(true);
				} else {
					final ChordMessage message = ChordMessage.obtainMessage(MessageType.CLEAR_CARDS);
					sendToClient(message, player);
				}
			}
		}

		// Choose the dealer.
		Player dealerPlayer = mModel.getNextPlayer(Preconditions.firstNonNull(mModel.getDealer(), mModel.getPlayers()
				.get(0)));

		while (!dealerPlayer.isPlaying() || dealerPlayer.isAllIn()) {
			dealerPlayer = mModel.getNextPlayer(dealerPlayer);
		}

		mModel.setDealer(dealerPlayer);
		final ChordMessage message = ChordMessage.obtainMessage(MessageType.DEALER);
		sendToClient(message, dealerPlayer);

		Player smallBlindPlayer;
		// Choose the player with small blind.
		if (mModel.getPlayingPlayersCount() == ServerModel.MIN_PLAYER_NUMBER) {
			smallBlindPlayer = mModel.getDealer();
		} else {
			smallBlindPlayer = mModel.getNextPlayer(mModel.getDealer());

			while (!smallBlindPlayer.isPlaying() || smallBlindPlayer.isAllIn()) {
				smallBlindPlayer = mModel.getNextPlayer(smallBlindPlayer);
			}
		}

		final int smallBlindAmount = Math.min(smallBlindPlayer.getAmount(), ServerModel.SMALL_BLIND);
		smallBlindPlayer.takeAmount(smallBlindAmount);
		smallBlindPlayer.setCurrentBid(smallBlindAmount);
		mModel.setPlayerWithSmallBlind(smallBlindPlayer);

		final ChordMessage smallBlindMessage = ChordMessage.obtainMessage(MessageType.BLIND);
		smallBlindMessage.putObject(ChordMessage.BLIND_TYPE, BlindType.SMALL_BLIND);
		smallBlindMessage.putInt(ChordMessage.AMOUNT, smallBlindAmount);
		sendToClient(smallBlindMessage, smallBlindPlayer);
		mModel.increaseTablePool(smallBlindAmount);

		if (smallBlindAmount < ServerModel.SMALL_BLIND) {
			smallBlindPlayer.setAllIn(true);
		}

		// Choose the player with big blind.
		Player bigBlindPlayer = mModel.getNextPlayer(mModel.getPlayerWithSmallBlind());

		while (!bigBlindPlayer.isPlaying() || bigBlindPlayer.isAllIn()) {
			bigBlindPlayer = mModel.getNextPlayer(bigBlindPlayer);
		}

		final int bigBlindAmount = Math.min(bigBlindPlayer.getAmount(), ServerModel.BIG_BLIND);
		bigBlindPlayer.takeAmount(bigBlindAmount);
		bigBlindPlayer.setCurrentBid(bigBlindAmount);
		mModel.setPlayerWithBigBlind(bigBlindPlayer);

		final ChordMessage bigBlindMessage = ChordMessage.obtainMessage(MessageType.BLIND);
		bigBlindMessage.putObject(ChordMessage.BLIND_TYPE, BlindType.BIG_BLIND);
		bigBlindMessage.putInt(ChordMessage.AMOUNT, bigBlindAmount);
		sendToClient(bigBlindMessage, bigBlindPlayer);
		mModel.increaseTablePool(bigBlindAmount);

		if (bigBlindAmount < ServerModel.BIG_BLIND) {
			bigBlindPlayer.setAllIn(true);
		}

		for (Player pokerPlayer : mModel.getPlayers()) {
			if (pokerPlayer.isPlaying()) {
				final ChordMessage cardMessage = ChordMessage.obtainMessage(MessageType.CARD_PAIR);
				final Card firstCard = mModel.getNextCard();
				final Card secondCard = mModel.getNextCard();
				pokerPlayer.setCards(new Pair<Card, Card>(firstCard, secondCard));
				cardMessage.putObject(ChordMessage.FIRST_CARD, firstCard);
				cardMessage.putObject(ChordMessage.SECOND_CARD, secondCard);
				sendToClient(cardMessage, pokerPlayer);
			}
		}

		/**
		 * The maxCheck variable prevents to loop infinitely when searching for the turn player. This situation may
		 * happen in head to head game when both players have the amount less than the small blind value. In this
		 * situation both players are playing with all in set to true, so the loop will never end.
		 */
		int maxCheckCount = mModel.getPlayers().size();

		// Choose the player with turn.
		Player turnPlayer = mModel.getNextPlayer(mModel.getPlayerWithBigBlind());

		while (!turnPlayer.isPlaying() || turnPlayer.isAllIn()) {
			turnPlayer = mModel.getNextPlayer(turnPlayer);
			if (maxCheckCount-- == 0) {
				break;
			}
		}

		// Set players and bid.
		mModel.setLastPlayer(turnPlayer);
		mModel.setCurrentPlayer(turnPlayer);
		mModel.setCurrentBid(Math.max(smallBlindAmount, bigBlindAmount));
		mModel.triggerPokerTableEvent();

		// End the game if no one have a turn.
		if (maxCheckCount == 0) {
			showMissingCards();
			endOfGame(false);
			return;
		}

		// Send your turn message.
		final ChordMessage turnMessage = ChordMessage.obtainMessage(MessageType.YOUR_TURN);
		turnMessage.putObject(ChordMessage.AMOUNT, mModel.getCurrentBid());
		sendToClient(turnMessage, turnPlayer);
	}

	/**
	 * Handles player's sitting at the table by updating model and refreshing poker table view.
	 * 
	 * @param sitEvent
	 *            which indicates player that sat at the table
	 */
	@Subscribe
	public void handleSit(SitEvent sitEvent) {
		if (mModel.getSittingPlayersCount() < ServerModel.MAX_PLAYER_NUMBER) {
			sendToClient(ChordMessage.obtainMessage(MessageType.ALLOW_SIT), sitEvent.getNodeName());
			mModel.getPlayer(sitEvent.getNodeName()).setSitting(true);
			mBus.post(new SittingPlayersChangedEvent(mModel.getGameState(), mModel
					.getSittingAndEligibleToPlayPlayersCount()));
			if (mModel.getGameState() == GameState.GAME_FINISHED) {
				mModel.updateAndRedrawSavedTableState(mModel.getPlayer(sitEvent.getNodeName()).getName(), true);
			} else {
				mModel.triggerPokerTableEvent();
			}
		} else {
			sendToClient(ChordMessage.obtainMessage(MessageType.TABLE_FULL), sitEvent.getNodeName());
		}
	}

	/**
	 * Handles player's standing from the table by updating model and refreshing poker table view.
	 * 
	 * @param standEvent
	 *            which indicates player that stood from the table
	 */
	@Subscribe
	public void handleStand(StandEvent standEvent) {
		handleStand(mModel.getPlayer(standEvent.getNodeName()));
	}

	private void handleStand(Player player) {
		player.setPlaying(false);
		player.setSitting(false);
		mBus.post(new SittingPlayersChangedEvent(mModel.getGameState(), mModel
				.getSittingAndEligibleToPlayPlayersCount()));
		if (mModel.getGameState() == GameState.GAME_FINISHED) {
			mModel.updateAndRedrawSavedTableState(player.getName(), false);
		} else {
			mModel.triggerPokerTableEvent();
		}
	}

	/**
	 * Handles player's fold updating model and refreshing poker table view.
	 * 
	 * @param foldEvent
	 *            which indicates player that folded
	 */
	@Subscribe
	public void handleFold(FoldEvent foldEvent) {
		handleFold(mModel.getPlayer(foldEvent.getNodeName()));
	}

	private void handleFold(Player player) {
		player.setPlaying(false);
		player.setLastPlayersAction(Player.FOLD_ACTION);
		mModel.setTableMessage(player.getName() + " " + mResources.getString(R.string.folded));

		// Players without turn can send fold while standing.
		if (player.equals(mModel.getCurrentPlayer())) {
			determineNextPlayer();
		} else if (mModel.getPlayingPlayersCount() == 1) {
			endOfGame(true);
		}
	}

	/**
	 * Handles bidding event.
	 * <p>
	 * Bidding events are RISE, CHECK, CALL.
	 * 
	 * @param biddingEvent
	 *            the bidding event
	 */
	@Subscribe
	public void handleBidding(BiddingEvent biddingEvent) {
		final int bidAmount = biddingEvent.getAmount();
		final Player player = mModel.getPlayer(biddingEvent.getNodeName());
		final int currentBid = player.getCurrentBid();
		mModel.increaseTablePool(bidAmount - player.getCurrentBid());
		player.takeAmount(bidAmount - currentBid);
		player.setCurrentBid(bidAmount);

		if (player.getAmount() == 0) {
			player.setAllIn(true);
		}

		String lastAction = player.getLastPlayersAction();
		switch (biddingEvent.getBiddingType()) {
		case CALL:
			mModel.setTableMessage(player.getName() + " " + mResources.getString(R.string.called));
			lastAction = Player.CALL_ACTION;
			break;
		case CHECK:
			mModel.setTableMessage(player.getName() + " " + mResources.getString(R.string.checked));
			lastAction = Player.CHECK_ACTION;
			break;
		case RAISE:
			mModel.setTableMessage(player.getName() + " " + mResources.getString(R.string.raised) + " " + bidAmount);
			lastAction = Player.RAISE_ACTION;
			break;
		default:
			throw new IllegalArgumentException(biddingEvent.getBiddingType().name());
		}
		player.setLastPlayersAction(lastAction);
		determineNextPlayer();
	}

	/**
	 * Handles all-in event.
	 * 
	 * @param allInEvent
	 *            containing information about the player that went all-in
	 */
	@Subscribe
	public void handleAllIn(AllInEvent allInEvent) {
		final int amount = allInEvent.getAllInAmount();
		final Player player = mModel.getPlayer(allInEvent.getNodeName());
		mModel.increaseTablePool(amount - player.getCurrentBid());
		player.setCurrentBid(amount);
		player.takeAmount(player.getAmount());
		player.setAllIn(true);
		player.setLastPlayersAction(Player.ALL_IN_ACTION);
		mModel.setTableMessage(player.getName() + " " + mResources.getString(R.string.all_in));
		determineNextPlayer();
	}

	private boolean mEndGameInNewRound;

	private void determineNextPlayer() {
		if (mModel.getPlayingPlayersCount() == 1) {
			endOfGame(true);
			return;
		}

		int withoutAllIn = 0;
		for (Player player : mModel.getPlayingPlayers()) {
			if (!player.isAllIn()) {
				++withoutAllIn;
			}
		}

		if (withoutAllIn == 0 || mEndGameInNewRound) {
			showMissingCards();
			endOfGame(false);
			mEndGameInNewRound = false;
			return;
		} else if (withoutAllIn == 1) {
			mEndGameInNewRound = true;
		}

		// If the bid is bigger than the previous one, set the player as last.
		final int bid = mModel.getCurrentPlayer().getCurrentBid();
		if (bid > mModel.getCurrentBid()) {
			mModel.setLastPlayer(mModel.getCurrentPlayer());
			mModel.setCurrentBid(bid);
		}

		// Find next playing player.
		Player nextPlayer = mModel.getNextPlayer(mModel.getCurrentPlayer());

		while (!nextPlayer.isPlaying() || nextPlayer.isAllIn()) {
			nextPlayer = mModel.getNextPlayer(nextPlayer);
		}

		if (withoutAllIn == 1 && nextPlayer.getCurrentBid() == mModel.getCurrentBid()) {
			showMissingCards();
			endOfGame(false);
			mEndGameInNewRound = false;
			return;
		}

		if (nextPlayer.equals(mModel.getLastPlayer())) {
			// End of the round.
			mModel.setGameState(GameState.getNextState(mModel.getGameState()));

			if (mModel.getGameState() == GameState.GAME_FINISHED) {
				endOfGame(false);
				return;
			} else {
				switch (mModel.getGameState()) {
				case FLOP:
					mModel.addCommonCard(mModel.getNextCard());
					mModel.addCommonCard(mModel.getNextCard());
					mModel.addCommonCard(mModel.getNextCard());
					mModel.triggerPokerTableEvent();
					break;
				case RIVER:
				case TURN:
					mModel.addCommonCard(mModel.getNextCard());
					mModel.triggerPokerTableEvent();
					break;
				default:
					throw new IllegalArgumentException(mModel.getGameState().name());
				}

				// Find the first player in a new round.
				nextPlayer = mModel.getNextPlayer(mModel.getDealer());

				while (!nextPlayer.isPlaying() || nextPlayer.isAllIn()) {
					nextPlayer = mModel.getNextPlayer(nextPlayer);
				}

				mModel.setLastPlayer(nextPlayer);
				sendYourTurnToNextPlayingPlayer(nextPlayer);
			}
		} else {
			// Not the end of the round.
			sendYourTurnToNextPlayingPlayer(nextPlayer);
		}

		// Check if the current player has folded.
		if (bid <= mModel.getCurrentBid()) {
			final Player lastPlayer = mModel.getLastPlayer();
			if (mModel.getCurrentPlayer().equals(lastPlayer) && !lastPlayer.isPlaying()) {
				mModel.setLastPlayer(nextPlayer);
			}
		}

		if (mModel.getCurrentPlayer().isAllIn()) {
			mModel.setLastPlayer(nextPlayer);
		}
		// Set a new current player.
		mModel.setCurrentPlayer(nextPlayer);
		mModel.triggerPokerTableEvent();
	}

	@SuppressWarnings("fallthrough")
	private void showMissingCards() {
		switch (mModel.getGameState()) {
		case PRE_FLOP:
			mModel.addCommonCard(mModel.getNextCard());
			mModel.addCommonCard(mModel.getNextCard());
			mModel.addCommonCard(mModel.getNextCard());
		case FLOP:
			mModel.addCommonCard(mModel.getNextCard());
		case TURN:
			mModel.addCommonCard(mModel.getNextCard());
			break;
		default:
			// Does nothing intentionally.
		}
		mModel.triggerPokerTableEvent();
	}

	private void endOfGame(boolean allFolded) {
		mModel.setCurrentPlayer(null);
		mModel.setPlayerWithBigBlind(null);
		mModel.setPlayerWithSmallBlind(null);
		mEndGameInNewRound = false;

		final List<Player> winners;
		Map<Player, PokerHand> losers = null;
		if (allFolded) {
			winners = mModel.getPlayingPlayers();
		} else {
			final GameResult gameResult = PokerUtils.getGameResult(mModel.getCommonCards(), mModel.getPlayingPlayers());
			winners = gameResult.getWinners();
			losers = gameResult.getSortedMap();
		}

		// List of the pots
		final List<Pair<Integer, List<Player>>> pots = new ArrayList<Pair<Integer, List<Player>>>();
		int pot = mModel.getTablePool();

		// Splits the pot into side pots if necessary
		if (mModel.getPlayingPlayersCount() * mModel.getCurrentBid() != pot && !allFolded) {
			// At least one player with all-in different than the current bid, so create the side pots
			final List<Player> allPlayers = mModel.getPlayers();
			Collections.sort(allPlayers);

			for (Player player : allPlayers) {
				int playerBid = player.getCurrentBid();

				if (playerBid > 0) {
					for (Pair<Integer, List<Player>> sidePot : pots) {
						sidePot.second.add(player);
						playerBid -= sidePot.first;
					}

					if (playerBid > 0) {
						// Not the entire amount is splitted so create a new side pot
						final List<Player> potPlayers = new ArrayList<Player>();
						potPlayers.add(player);
						pots.add(new Pair<Integer, List<Player>>(playerBid, potPlayers));
					}
				}
			}
		} else {
			pots.add(new Pair<Integer, List<Player>>(pot / mModel.getPlayingPlayersCount(), mModel.getPlayingPlayers()));
		}

		pot = mModel.getTablePool();
		// Notify the winners
		for (Player winner : winners) {
			if (losers != null) {
				losers.remove(winner);
			}

			int wonAmount = 0;
			for (Pair<Integer, List<Player>> sidePot : pots) {
				if (sidePot.second.contains(winner)) {
					final List<Player> split = new ArrayList<Player>(winners);
					split.retainAll(sidePot.second);
					wonAmount += sidePot.first * sidePot.second.size() / split.size();
				}
			}

			final ChordMessage gameEndMessage = ChordMessage.obtainMessage(MessageType.GAME_END);
			gameEndMessage.putInt(ChordMessage.AMOUNT, wonAmount);
			sendToClient(gameEndMessage, winner.getNodeName());
			winner.addAmount(wonAmount);
			pot -= wonAmount;
		}

		// Remove granted side pots
		final Iterator<Pair<Integer, List<Player>>> it = pots.iterator();
		while (it.hasNext()) {
			final Pair<Integer, List<Player>> sidePot = it.next();
			for (Player player : sidePot.second) {
				if (winners.contains(player)) {
					it.remove();
					break;
				}
			}
		}

		// Notify the losers
		if (losers != null) {
			for (Player loser : losers.keySet()) {
				int wonAmount = 0;

				// There is still amount in the pot, split it between losers
				if (pot > 0) {
					final Iterator<Pair<Integer, List<Player>>> iterator = pots.iterator();
					while (iterator.hasNext()) {
						final Pair<Integer, List<Player>> sidePot = iterator.next();
						if (sidePot.second.contains(loser)) {
							wonAmount += sidePot.first * sidePot.second.size();
							iterator.remove();
						}
					}
				}

				final ChordMessage gameEndMessage = ChordMessage.obtainMessage(MessageType.GAME_END);
				gameEndMessage.putInt(ChordMessage.AMOUNT, wonAmount);
				sendToClient(gameEndMessage, loser.getNodeName());
				loser.addAmount(wonAmount);
				pot -= wonAmount;
			}
		}

		if (!allFolded) {
			for (Player player : mModel.getPlayingPlayers()) {
				player.setShouldShowCards(true);
			}
		}

		mModel.setTableMessage(winners.toString() + " " + mResources.getString(R.string.won) + ". "
				+ mResources.getString(R.string.press_start));

		mModel.saveCurrentTableState();
		mModel.clearGame();
		mModel.increaseTablePool(pot);
		mModel.redrawSavedTableState();
		mModel.setGameState(GameState.GAME_FINISHED);
		mBus.post(new SittingPlayersChangedEvent(mModel.getGameState(), mModel
				.getSittingAndEligibleToPlayPlayersCount()));
	}

	private void sendYourTurnToNextPlayingPlayer(Player nextPlayer) {
		final ChordMessage turnMessage = ChordMessage.obtainMessage(MessageType.YOUR_TURN);
		turnMessage.putObject(ChordMessage.AMOUNT, mModel.getCurrentBid());
		sendToClient(turnMessage, nextPlayer.getNodeName());
		mModel.triggerPokerTableEvent();
	}

	/**
	 * Handles the user name event.
	 * 
	 * @param usernameEvent
	 *            containing information about player that joined the game
	 */
	@Subscribe
	public void handleUsername(UsernameEvent usernameEvent) {
		final String nodeName = usernameEvent.getNodeName();
		final Player player = mModel.getPlayer(nodeName);
		final ChordMessage stateMessage = ChordMessage.obtainMessage(MessageType.PLAYER_STATE);
		final int amount;

		if (player == null) {
			mModel.addPlayer(Player.createPlayer(usernameEvent.getUsername(), nodeName));
			amount = ServerModel.INITIAL_AMOUNT;
		} else {
			amount = player.getAmount();
		}

		stateMessage.putInt(ChordMessage.AMOUNT, amount);
		sendToClient(stateMessage, usernameEvent);
		mModel.triggerPokerTableEvent();
	}

	private <T extends PokerLogicEvent> void sendToClient(ChordMessage message, T event) {
		sendToClient(message, event.getNodeName());
	}

	private void sendToClient(ChordMessage message, String nodeName) {
		message.setReceiverNodeName(nodeName);
		message.setFromLogic(true);
		mBus.post(message);
	}

	private void sendToClient(ChordMessage message, Player player) {
		sendToClient(message, player.getNodeName());
	}

	@Subscribe
	public void onPlayerDisconnected(ClientDisconnectedEvent event) {
		final Player player = mModel.getPlayer(event.getNodeName());

		if (player != null) {
			if (player.isPlaying()) {
				handleFold(player);
			}

			if (player.isSitting()) {
				handleStand(player);
			}
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

	/**
	 * Event posted through the {@link Bus} that contains information about changes in the game logic.
	 */
	public static class PokerLogicEvent extends BusEvent {

		private static final long serialVersionUID = 20130312L;

		private final PokerLogicEventType mType;
		private static final String NODE_NAME = "NODE_NAME";
		private static final String AMOUNT = "AMOUNT";

		private PokerLogicEvent(PokerLogicEventType type, String nodeName) {
			super();
			putObject(NODE_NAME, nodeName);
			mType = type;
		}

		public PokerLogicEventType getType() {
			return mType;
		}

		public String getNodeName() {
			return (String) getObject(NODE_NAME);
		}

		public static class SitEvent extends PokerLogicEvent {

			private static final long serialVersionUID = 20130325L;

			public SitEvent(String nodeName) {
				super(PokerLogicEventType.SIT, nodeName);
			}

		}

		public static class StandEvent extends PokerLogicEvent {

			private static final long serialVersionUID = 20130325L;

			public StandEvent(String nodeName) {
				super(PokerLogicEventType.STAND, nodeName);
			}

		}

		public static class UsernameEvent extends PokerLogicEvent {

			private static final long serialVersionUID = 20130325L;

			public static final String USERNAME = "USERNAME";

			public UsernameEvent(String username, String nodeName) {
				super(PokerLogicEventType.USERNAME, nodeName);
				putObject(USERNAME, username);
			}

			public String getUsername() {
				return (String) getObject(USERNAME);
			}

		}

		public static class BiddingEvent extends PokerLogicEvent {

			private static final long serialVersionUID = 20130325L;
			private final BiddingType mBiddingType;

			public BiddingEvent(String nodeName, int riseAmount, BiddingType biddingType) {
				super(PokerLogicEventType.BIDDING, nodeName);
				putInt(AMOUNT, riseAmount);
				mBiddingType = biddingType;
			}

			public int getAmount() {
				return getInt(AMOUNT);
			}

			public BiddingType getBiddingType() {
				return mBiddingType;
			}

			public enum BiddingType {
				CALL, CHECK, RAISE;
			}

		}

		public static class AllInEvent extends PokerLogicEvent {

			private static final long serialVersionUID = 20130325L;

			public AllInEvent(String nodeName, int allInAmount) {
				super(PokerLogicEventType.ALL_IN, nodeName);
				putInt(AMOUNT, allInAmount);
			}

			public int getAllInAmount() {
				return getInt(AMOUNT);
			}

		}

		public static class FoldEvent extends PokerLogicEvent {

			private static final long serialVersionUID = 20130325L;

			public FoldEvent(String nodeName) {
				super(PokerLogicEventType.FOLD, nodeName);
			}

		}

		/**
		 * Represents type of the {@link PokerLogicEvent}.
		 */
		public enum PokerLogicEventType {
			//@formatter:off
			SIT,
		    STAND,
		    USERNAME,
		    BIDDING,
		    ALL_IN,
		    FOLD;
			//@formatter:on

			@Override
			public String toString() {
				return name();
			}

		}
	}

	public enum StartGameEvent {
		INSTANCE;
	}

}
