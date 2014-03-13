package logic;
import java.util.Arrays;

/**
 * Class containing an array of integers describing the poker hand.
 * 
 * The first element of the hand is the type - it can range from HIGH_CARD to STRAIGHT_FLUSH. The next elements are the
 * values of the type.
 */
public class PokerHand implements Comparable<PokerHand> {

	private final int[] mPokerHand;

	/**
	 * Constructor method.
	 * 
	 * @param hand
	 *            Array containing hand card values.
	 */
	public PokerHand(int[] hand) {
		mPokerHand = new int[hand.length];
		System.arraycopy(hand, 0, mPokerHand, 0, hand.length);
	}

	/**
	 * Method comparing one hand to another.
	 * 
	 * @param secondHand
	 *            The second hand to be compared to.
	 * @return -1 if the current hand is smaller than secondHand, 0 if equal, 1 if greater.
	 */
	@Override
	public int compareTo(PokerHand secondHand) {

		if (mPokerHand == null) {
			return -1;
		} else if (mPokerHand.length == 0 && secondHand.getHand().length == 0) {
			return 0;
		} else {
			if (mPokerHand.length == secondHand.getHand().length) {
				for (int i = 0; i < mPokerHand.length; i++) {

					if (mPokerHand[i] > secondHand.getHand()[i]) {

						return 1;
					} else if (mPokerHand[i] < secondHand.getHand()[i]) {

						return -1;
					}
				}
			} else {
				if (mPokerHand[0] > secondHand.getHand()[0]) {
					return 1;
				} else if (mPokerHand[0] < secondHand.getHand()[0]) {
					return -1;
				}

			}
		}

		return 0;
	}

	/**
	 * Getter method for the hand array.
	 * 
	 * @return poker hand;
	 */
	public int[] getHand() {
		return Arrays.copyOf(mPokerHand, mPokerHand.length);
	}

	@Override
	public String toString() {
		final StringBuilder toReturn = new StringBuilder();

		for (int element : mPokerHand) {
			toReturn.append(element).append(' ');
		}
		return toReturn.toString();
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
		final PokerHand other = (PokerHand) obj;
		if (!Arrays.equals(mPokerHand, other.mPokerHand)) {
			return false;
		}
		return true;
	}

}
