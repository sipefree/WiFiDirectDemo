/*
 ********************************************************************************
 * Copyright (c) 2013 Samsung Electronics, Inc.
 * All rights reserved.
 *
 * This software is a confidential and proprietary information of Samsung
 * Electronics, Inc. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with Samsung Electronics.
 ********************************************************************************
 */
package com.srpol.poker.logic;

import android.util.Pair;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.srpol.poker.chord.ChordMessage;
import com.srpol.poker.chord.ChordMessage.MessageType;
import com.srpol.poker.events.BusEvent;
import com.srpol.poker.logic.ClientModel.ClientModelEvent.AllInEvent;
import com.srpol.poker.logic.ClientModel.ClientModelEvent.BidEvent;
import com.srpol.poker.logic.ClientModel.ClientModelEvent.BlindEvent;
import com.srpol.poker.logic.ClientModel.ClientModelEvent.BlindEvent.BlindType;
import com.srpol.poker.logic.ClientModel.ClientModelEvent.CallEvent;
import com.srpol.poker.logic.ClientModel.ClientModelEvent.CardsEvent;
import com.srpol.poker.logic.ClientModel.ClientModelEvent.CheckEvent;
import com.srpol.poker.logic.ClientModel.ClientModelEvent.ClearCardsEvent;
import com.srpol.poker.logic.ClientModel.ClientModelEvent.DealerEvent;
import com.srpol.poker.logic.ClientModel.ClientModelEvent.FoldEvent;
import com.srpol.poker.logic.ClientModel.ClientModelEvent.GameEnd;
import com.srpol.poker.logic.ClientModel.ClientModelEvent.PlayerStateEvent;
import com.srpol.poker.logic.ClientModel.ClientModelEvent.RaiseEvent;
import com.srpol.poker.logic.ClientModel.ClientModelEvent.SitEvent;
import com.srpol.poker.logic.ClientModel.ClientModelEvent.StandEvent;
import com.srpol.poker.logic.ClientModel.ClientModelEvent.YourTurnEvent;
import com.srpol.poker.logic.CommunicationBus.BusManager;
import com.srpol.poker.logic.PokerLogicController.PokerLogicEvent.BiddingEvent.BiddingType;
import com.srpol.poker.ui.GameActivity.GameActivityEvent;
import com.srpol.poker.ui.GameActivity.GameActivityEvent.TokenEvent.TokenType;
import com.srpol.poker.ui.GameActivity.GameActivityEvent.YourTurnEvent.YourTurnType;
import com.srpol.poker.utils.Preconditions;

/**
 * Contains the game model used on the client side.
 */
public class ClientModel implements BusManager {

	private final Bus mBus;

	private int mAmount;
	private int mPreviousBidAmount;
	private int mBidAmount;
	private int mMinimumBidAmount;
	private Pair<Card, Card> mCards;
	private boolean mIsDealer;

	ClientModel() {
		mBus = CommunicationBus.getInstance();
	}

	/**
	 * Updates model and view after player goes all-in.
	 * 
	 * @param event
	 *            containing information about all-in action
	 */
	@Subscribe
	public void allIn(AllInEvent event) {
		mPreviousBidAmount = mBidAmount += mAmount;
		mAmount = 0;

		final ChordMessage message = ChordMessage.obtainMessage(MessageType.ALL_IN);
		message.putInt(ChordMessage.AMOUNT, mBidAmount);
		mBus.post(message);

		postToGameActivity(new GameActivityEvent.AmountEvent(mAmount, mBidAmount, mMinimumBidAmount));
		postToGameActivity(new GameActivityEvent.TurnEndEvent());
	}

	/**
	 * Updates model and view after player receives blind.
	 * 
	 * @param event
	 *            containing information about blind received by the player
	 */
	@Subscribe
	public void blind(BlindEvent event) {
		final int blindAmount = event.getBlindAmount();
		mAmount -= blindAmount;
		mPreviousBidAmount = mBidAmount += blindAmount;
		Preconditions.checkState(mAmount >= 0);

		TokenType tokenType;
		if (event.getBlindType() == BlindType.SMALL_BLIND) {
			if (mIsDealer) {
				tokenType = TokenType.DEALER_WITH_SMALL_BLIND;
			} else {
				tokenType = TokenType.SMALL_BLIND;
			}
		} else {
			tokenType = TokenType.BIG_BLIND;
		}
		mMinimumBidAmount = blindAmount;

		postToGameActivity(new GameActivityEvent.TokenEvent(tokenType));
		postToGameActivity(new GameActivityEvent.AmountEvent(mAmount, mBidAmount, mMinimumBidAmount));
	}

	/**
	 * Updates model and view after player's bidding.
	 * 
	 * @param event
	 *            containing information about bidding
	 */
	@Subscribe
	public void bid(BidEvent event) {
		mAmount -= event.getAmount();
		mBidAmount += event.getAmount();
		Preconditions.checkState(mAmount >= 0);

		postToGameActivity(new GameActivityEvent.AmountEvent(mAmount, mBidAmount, mMinimumBidAmount));
	}

	/**
	 * Updates model and view after player folded.
	 * 
	 * @param event
	 *            containing information about fold
	 */
	@Subscribe
	public void fold(FoldEvent event) {
		mAmount += mBidAmount - mPreviousBidAmount;
		mPreviousBidAmount = mBidAmount = mMinimumBidAmount = 0;

		final ChordMessage message = ChordMessage.obtainMessage(MessageType.FOLD);
		mBus.post(message);

		postToGameActivity(new GameActivityEvent.TurnEndEvent());
		postToGameActivity(new GameActivityEvent.AmountEvent(mAmount, mBidAmount, mMinimumBidAmount));
	}

	/**
	 * Updates model and view after player called.
	 * 
	 * @param event
	 *            containing information about call
	 */
	@Subscribe
	public void call(CallEvent event) {
		mAmount -= mMinimumBidAmount - mBidAmount;
		mPreviousBidAmount = mBidAmount = mMinimumBidAmount;

		final ChordMessage message = ChordMessage.obtainMessage(MessageType.BIDDING);
		message.putInt(ChordMessage.AMOUNT, mBidAmount);
		message.putObject(ChordMessage.BIDDING_TYPE, BiddingType.CALL);
		mBus.post(message);

		postToGameActivity(new GameActivityEvent.TurnEndEvent());
		postToGameActivity(new GameActivityEvent.AmountEvent(mAmount, mBidAmount, mMinimumBidAmount));
	}

	/**
	 * Updates model and view after player raised.
	 * 
	 * @param event
	 *            containing information about raise
	 */
	@Subscribe
	public void raise(RaiseEvent event) {
		mPreviousBidAmount = mMinimumBidAmount = mBidAmount;

		final ChordMessage message = ChordMessage.obtainMessage(MessageType.BIDDING);
		message.putInt(ChordMessage.AMOUNT, mBidAmount);
		message.putObject(ChordMessage.BIDDING_TYPE, BiddingType.RAISE);
		mBus.post(message);

		postToGameActivity(new GameActivityEvent.AmountEvent(mAmount, mBidAmount, mMinimumBidAmount));
		postToGameActivity(new GameActivityEvent.TurnEndEvent());
	}

	/**
	 * Updates model and view after player checked.
	 * 
	 * @param event
	 *            containing information about check
	 */
	@Subscribe
	public void check(CheckEvent event) {
		mPreviousBidAmount = mBidAmount;

		final ChordMessage message = ChordMessage.obtainMessage(MessageType.BIDDING);
		message.putInt(ChordMessage.AMOUNT, mBidAmount);
		message.putObject(ChordMessage.BIDDING_TYPE, BiddingType.CHECK);
		mBus.post(message);

		postToGameActivity(new GameActivityEvent.TurnEndEvent());
	}

	/**
	 * Updates model and view after player received card pair.
	 * 
	 * @param event
	 *            containing information about cards
	 */
	@Subscribe
	public void cards(CardsEvent event) {
		mCards = event.getCards();

		postToGameActivity(new GameActivityEvent.CardsEvent(mCards));
	}

	/**
	 * Updates model and view when game finished.
	 * 
	 * @param event
	 *            containing information about game finish
	 */
	@Subscribe
	public void gameEnd(GameEnd event) {
		mAmount += event.getAmount();
		mBidAmount = 0;
		mMinimumBidAmount = 0;
		mPreviousBidAmount = 0;
		mIsDealer = false;

		postToGameActivity(new GameActivityEvent.GameEndEvent());
		postToGameActivity(new GameActivityEvent.AmountEvent(mAmount, mBidAmount, mMinimumBidAmount));
	}

	/**
	 * Sends SitEvent to the server.
	 * 
	 * @param event
	 *            containing information about sit
	 */
	@Subscribe
	public void sit(SitEvent event) {
		final ChordMessage message = ChordMessage.obtainMessage(MessageType.SIT);
		mBus.post(message);
	}

	/**
	 * Updates model and view after player stood up.
	 * 
	 * @param event
	 *            containing information about stand
	 */
	@Subscribe
	public void stand(StandEvent event) {
		if (hasCards()) {
			final ChordMessage message = ChordMessage.obtainMessage(MessageType.FOLD);
			mBus.post(message);
		}

		final ChordMessage message = ChordMessage.obtainMessage(MessageType.STAND);
		mBus.post(message);

		postToGameActivity(new GameActivityEvent.StandEvent());
		clearModel();
	}

	/**
	 * Updates model and view after player was choosen as a dealer.
	 * 
	 * @param event
	 *            containing information about dealer
	 */
	@Subscribe
	public void dealer(DealerEvent event) {
		mIsDealer = true;
		postToGameActivity(new GameActivityEvent.TokenEvent(TokenType.DEALER));
	}

	/**
	 * Updates model and view after player received move.
	 * 
	 * @param event
	 *            containing information about player's move
	 */
	@Subscribe
	public void yourTurn(YourTurnEvent event) {
		mPreviousBidAmount = mBidAmount;
		mMinimumBidAmount = event.getMinBidAmount();

		final YourTurnType turnType;
		if (mBidAmount + mAmount >= mMinimumBidAmount) {
			turnType = mMinimumBidAmount == mBidAmount ? YourTurnType.CHECK : YourTurnType.CALL;
		} else {
			turnType = YourTurnType.NONE;
		}

		postToGameActivity(new GameActivityEvent.YourTurnEvent(turnType, mAmount, mMinimumBidAmount));
	}

	/**
	 * Updates model and view after player state is received.
	 * 
	 * @param event
	 *            containing information about player's state
	 */
	@Subscribe
	public void playerState(PlayerStateEvent event) {
		mAmount = event.getAmount();

		postToGameActivity(new GameActivityEvent.AmountEvent(mAmount, mBidAmount, mMinimumBidAmount));
	}

	/**
	 * Updates model and view when player should clear his cards.
	 */
	@Subscribe
	public void clearCards(ClearCardsEvent event) {
		mCards = null;

		postToGameActivity(new GameActivityEvent.ClearCardsEvent());
	}

	@Override
	public void startBus() {
		mBus.register(this);
	}

	@Override
	public void stopBus() {
		mBus.unregister(this);
	}

	private boolean hasCards() {
		return mCards != null && mCards.first != null && mCards.second != null;
	}

	private void clearModel() {
		mBidAmount = 0;
		mMinimumBidAmount = 0;
		mPreviousBidAmount = 0;
		mIsDealer = false;

		postToGameActivity(new GameActivityEvent.GameEndEvent());
		postToGameActivity(new GameActivityEvent.ClearCardsEvent());
		postToGameActivity(new GameActivityEvent.TokenEvent(TokenType.NONE));
	}

	private <T extends GameActivityEvent> void postToGameActivity(T event) {
		mBus.post(event);
	}

	/**
	 * Represents event posted through the {@link Bus} from the GameChord to update {@ClientModel}.
	 */
	public static class ClientModelEvent extends BusEvent {

		private static final long serialVersionUID = 20130321L;

		private final ClientModelEventType mType;

		private ClientModelEvent(ClientModelEventType type) {
			super();
			mType = type;
		}

		public ClientModelEventType getType() {
			return mType;
		}

		public static class CardsEvent extends ClientModelEvent {

			private static final long serialVersionUID = 20130321L;

			public static final String CARDS = "CARDS";

			public CardsEvent(Pair<Card, Card> cards) {
				super(ClientModelEventType.CARD_PAIR);
				putObject(CARDS, cards);
			}

			@SuppressWarnings("unchecked")
			public Pair<Card, Card> getCards() {
				return (Pair<Card, Card>) getObject(CARDS);
			}

		}

		public static class BidEvent extends ClientModelEvent {

			private static final long serialVersionUID = 20130321L;

			public static final String AMOUNT = "AMOUNT";

			public BidEvent(int amount) {
				super(ClientModelEventType.BID);
				putInt(AMOUNT, amount);
			}

			public int getAmount() {
				return getInt(AMOUNT);
			}

		}

		public static class AllInEvent extends ClientModelEvent {

			private static final long serialVersionUID = 20130321L;

			public AllInEvent() {
				super(ClientModelEventType.ALL_IN);
			}

		}

		public static class RaiseEvent extends ClientModelEvent {

			private static final long serialVersionUID = 20130321L;

			public RaiseEvent() {
				super(ClientModelEventType.RISE);
			}

		}

		public static class CheckEvent extends ClientModelEvent {

			private static final long serialVersionUID = 20130321L;

			public CheckEvent() {
				super(ClientModelEventType.CHECK);
			}

		}

		public static class CallEvent extends ClientModelEvent {

			private static final long serialVersionUID = 20130321L;

			public CallEvent() {
				super(ClientModelEventType.CALL);
			}

		}

		public static class FoldEvent extends ClientModelEvent {

			private static final long serialVersionUID = 20130321L;

			public FoldEvent() {
				super(ClientModelEventType.FOLD);
			}

		}

		public static class SitEvent extends ClientModelEvent {

			private static final long serialVersionUID = 20130321L;

			public SitEvent() {
				super(ClientModelEventType.SIT);
			}

		}

		public static class StandEvent extends ClientModelEvent {

			private static final long serialVersionUID = 20130321L;

			public StandEvent() {
				super(ClientModelEventType.STAND);
			}

		}

		public static class YourTurnEvent extends ClientModelEvent {

			private static final long serialVersionUID = 20130321L;

			private static final String MIN_BID_AMOUNT = "MIN_BID_AMOUNT";

			public YourTurnEvent(int minBidAmount) {
				super(ClientModelEventType.YOUR_TURN);
				putInt(MIN_BID_AMOUNT, minBidAmount);
			}

			public int getMinBidAmount() {
				return getInt(MIN_BID_AMOUNT);
			}
		}

		public static class DealerEvent extends ClientModelEvent {

			private static final long serialVersionUID = 20130321L;

			public DealerEvent() {
				super(ClientModelEventType.DEALER);
			}

		}

		public static class BlindEvent extends ClientModelEvent {

			private static final long serialVersionUID = 20130321L;

			public static final String BLIND_TYPE = "BLIND_TYPE";
			public static final String BLIND_AMOUNT = "BLIND_AMOUNT";

			public BlindEvent(int amount, BlindType type) {
				super(ClientModelEventType.BLIND);
				putInt(BLIND_AMOUNT, amount);
				putObject(BLIND_TYPE, type);
			}

			public int getBlindAmount() {
				return getInt(BLIND_AMOUNT);
			}

			public BlindType getBlindType() {
				return (BlindType) getObject(BLIND_TYPE);
			}

			public enum BlindType {
				SMALL_BLIND, BIG_BLIND;

				@Override
				public String toString() {
					return name();
				}

			}

		}

		public static class PlayerStateEvent extends ClientModelEvent {

			private static final long serialVersionUID = 20130321L;

			public static final String AMOUNT = "AMOUNT";

			public PlayerStateEvent(int amount) {
				super(ClientModelEventType.PLAYER_STATE);
				putInt(AMOUNT, amount);
			}

			public int getAmount() {
				return getInt(AMOUNT);
			}

		}

		public static class GameEnd extends ClientModelEvent {

			private static final long serialVersionUID = 20130403L;

			public static final String AMOUNT = "AMOUNT";

			public GameEnd(int amount) {
				super(ClientModelEventType.WON);
				putInt(AMOUNT, amount);
			}

			public int getAmount() {
				return getInt(AMOUNT);
			}
		}

		public static class ClearCardsEvent extends ClientModelEvent {

			private static final long serialVersionUID = 20130417L;

			public ClearCardsEvent() {
				super(ClientModelEventType.CLEAR_CARDS);
			}
		}

	}

	/**
	 * Represents type of the {@link ClientModelEvent}.
	 */
	public static enum ClientModelEventType {
		//@formatter:off
		BLIND,
		BID,
		ALL_IN,
		RISE,
		CALL,
		CHECK,
		FOLD,
		CARD_PAIR,
		DEALER,
		PLAYER_STATE,
		YOUR_TURN,
		WON,
		LOSE,
		SIT,
		ALLOW_SIT,
		STAND,
		CLEAR_CARDS;
		//@formatter:off
		
		@Override
		public String toString() {
			return name();
		}
		
	}
}
