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

import java.io.Serializable;

/**
 * Represents individual card used in the poker game.
 */
public class Card implements Serializable {

	private static final long serialVersionUID = 20130327L;

	private final CardColor mColor;
	private final CardRank mRank;

	public Card(CardColor color, CardRank rank) {
		mColor = color;
		mRank = rank;
	}

	public Card(Card card) {
		mColor = card.mColor;
		mRank = card.mRank;
	}

	public CardColor getColor() {
		return mColor;
	}

	public CardRank getRank() {
		return mRank;
	}

	/**
	 * Represents color of the {@link Card}.
	 */
	public enum CardColor {

		CLUBS, SPADES, HEARTS, DIAMONDS;

		@Override
		public String toString() {
			return name();
		}

	}

	/**
	 * Represents rank of the {@link Card}.
	 */
	public enum CardRank {

		TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE;

		@Override
		public String toString() {
			return name();
		}
	}

	@Override
	public String toString() {
		return mRank.name() + " of " + mColor.name();
	}

}
