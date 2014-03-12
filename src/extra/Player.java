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

import java.util.Arrays;

import android.util.Pair;

import com.srpol.poker.utils.Preconditions;

/**
 * Represents a player's state in the poker game.
 */
public final class Player implements Comparable<Player> {

	private final String mName;
	private final String mNodeName;
	private final Card[] mCards;
	private int mAmount;
	private int mCurrentBid;
	private boolean mAllIn;
	private boolean mPlaying;
	private boolean mSitting;
	private String mLastPlayersAction;
	private boolean mShouldShowCards;

	public static final String INITIAL_PLAYERS_ACTION = "-----";
	public static final String FOLD_ACTION = "fold";
	public static final String RAISE_ACTION = "raise";
	public static final String CALL_ACTION = "call";
	public static final String CHECK_ACTION = "check";
	public static final String ALL_IN_ACTION = "all in";

	private Player(String playerName, String nodeName) {
		mName = Preconditions.checkNotNull(playerName);
		mNodeName = Preconditions.checkNotNull(nodeName);
		mCards = new Card[2];
		mAmount = ServerModel.INITIAL_AMOUNT;
		mSitting = false;
		mLastPlayersAction = INITIAL_PLAYERS_ACTION;
	}

	/**
	 * Creates new player instance with the {@value Player#INITIAL_AMOUNT} value as initial amount.
	 * 
	 * @param name
	 *            the player's name
	 * @param nodeName
	 *            the player's Chord node name
	 * @return {@link Player} instance
	 */
	public static Player createPlayer(String name, String nodeName) {
		return new Player(name, nodeName);
	}

	/**
	 * Takes the given amount from the player's pool.
	 * 
	 * @param amount
	 *            an amount to take from the player's pool
	 */
	public void takeAmount(int amount) {
		final int newAmount = mAmount - amount;
		Preconditions.checkState(newAmount >= 0);
		mAmount = newAmount;
	}

	/**
	 * Adds the given amount to the player's pool.
	 * 
	 * @param amount
	 *            amount to add to the player's pool
	 */
	public void addAmount(int amount) {
		mAmount += amount;
	}

	/**
	 * Returns single card.
	 * 
	 * @param index
	 *            index of the card (could be 0 or 1)
	 * @return returns the card at the given index
	 * @throws IllegalArgumentException
	 *             thrown when the given index is different than 0 or 1
	 */
	public Card getCard(int index) {
		if (index < 0 || index > 1) {
			throw new IllegalArgumentException(Integer.toString(index));
		}
		return mCards[index];
	}

	/**
	 * Sets the player's cards.
	 * 
	 * @param cards
	 *            the pair of cards to set
	 */
	public void setCards(Pair<Card, Card> cards) {
		mCards[0] = cards.first;
		mCards[1] = cards.second;
	}

	/**
	 * Clears player's cards.
	 */
	public void clearCards() {
		mCards[0] = mCards[1] = null;
	}

	/**
	 * Checks if player has cards.
	 * 
	 * @return boolean that indicates if the player has cards
	 */
	public boolean hasCards() {
		return mCards[0] != null && mCards[1] != null;
	}

	public Card[] getCards() {
		return Arrays.copyOf(mCards, mCards.length);
	}

	public int getAmount() {
		return mAmount;
	}

	public String getName() {
		return mName;
	}

	public String getNodeName() {
		return mNodeName;
	}

	public void setPlaying(boolean playing) {
		mPlaying = playing;
	}

	public boolean isPlaying() {
		return mPlaying;
	}

	public void setCurrentBid(int currentBid) {
		mCurrentBid = currentBid;
	}

	public int getCurrentBid() {
		return mCurrentBid;
	}

	public boolean isSitting() {
		return mSitting;
	}

	public void setSitting(boolean sitting) {
		mSitting = sitting;
	}

	public boolean isAllIn() {
		return mAllIn;
	}

	public void setAllIn(boolean allIn) {
		mAllIn = allIn;
	}

	/**
	 * Clears player's information.
	 */
	public void clearGame() {
		mAllIn = false;
		mPlaying = false;
		mCurrentBid = 0;
		mCards[0] = null;
		mCards[1] = null;
		mLastPlayersAction = INITIAL_PLAYERS_ACTION;
		mShouldShowCards = false;
	}

	public String getLastPlayersAction() {
		return mLastPlayersAction;
	}

	public void setLastPlayersAction(String lastPlayersAction) {
		mLastPlayersAction = lastPlayersAction;
	}

	public boolean getShouldShowCards() {
		return mShouldShowCards;
	}

	public void setShouldShowCards(boolean shouldShowCards) {
		mShouldShowCards = shouldShowCards;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final Player other = (Player) obj;
		return mNodeName.equals(other.mNodeName);
	}

	@Override
	public int hashCode() {
		final String hash = mNodeName + mName;
		return hash.hashCode();
	}

	@Override
	public String toString() {
		return mName;
	}

	@Override
	public int compareTo(Player another) {
		if (mCurrentBid > another.mCurrentBid) {
			return 1;
		} else if (mCurrentBid < another.mCurrentBid) {
			return -1;
		} else {
			return 0;
		}
	}
}
