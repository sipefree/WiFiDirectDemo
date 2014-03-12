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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.srpol.poker.logic.Card.CardColor;
import com.srpol.poker.logic.Card.CardRank;
import com.srpol.poker.utils.Preconditions;

/**
 * Represents deck of 52 {@link Card}s.
 */
public class CardDeck {

	private final List<Card> mCards;
	private static final int SHUFFLE_COUNT = 4;

	public CardDeck() {
		mCards = new ArrayList<Card>();

		initializeDeck();
	}

	/**
	 * Shuffles all cards in the deck.
	 */
	public final void shuffleCards() {
		for (int i = 0; i < SHUFFLE_COUNT; i++) {
			Collections.shuffle(mCards);
		}
	}

	/**
	 * Returns first {@link Card} from the {@CardDeck}.
	 * 
	 * @return first card from the deck
	 */
	public Card popCard() {
		return mCards.remove(0);
	}

	/**
	 * Pushes {@link Card} back to the {@link CardDeck}.
	 * 
	 * @param card
	 *            pushed back to the {@link CardDeck}
	 */
	public void pushCard(Card card) {
		mCards.add(Preconditions.checkNotNull(card));
	}

	private void initializeDeck() {
		for (CardColor color : CardColor.values()) {
			for (CardRank rank : CardRank.values()) {
				mCards.add(new Card(color, rank));
			}
		}
		shuffleCards();
	}

	/**
	 * Returns current size of the deck.
	 * 
	 * @return size of the deck
	 */
	public int getSize() {
		return mCards.size();
	}

	@Override
	public String toString() {
		return mCards.toString();
	}

}
