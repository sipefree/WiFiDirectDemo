package extra;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import extra.Card.CardColor;
import extra.Card.CardRank;

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
