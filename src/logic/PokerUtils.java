package logic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PokerUtils {

	private static final int MAX_CARDS_NUMBER = 7;

	private PokerUtils() {
		// No. op.
	}

	/**
	 * Returns game result. Contains the players and their hands.
	 * 
	 * @param tableCards
	 *            An array of 5 cards on the table.
	 * @param players
	 *            A list of all players.
	 * @return A GameResult object.
	 */
	public static GameResult getGameResult(Card[] tableCards, List<Player> players) {
		final int cardSet = 7;
		final GameResult gameResult = new GameResult();

		/**
		 * Create a set of 7 cards for each player (5 from the table 2 from the player), then choose the highest ranking
		 * 5 cards out of each 7.
		 */
		for (Player player : players) {
			final Card[] cards = new Card[cardSet];
			int numberOfCards = 0;
			int handCards = 0;

			while (numberOfCards < tableCards.length) {
				cards[numberOfCards] = tableCards[numberOfCards++];
			}
			while (numberOfCards < cards.length && handCards < player.getCards().length) {
				cards[numberOfCards++] = player.getCards()[handCards++];
			}

			PokerHand currHand;
			PokerHand maxHand = null;
			/**
			 * Finding the highest ranking 5 card hand from a set of 7 cards
			 */
			for (int i = 0; i < MAX_CARDS_NUMBER - 4; i++) {
				for (int j = i + 1; j < MAX_CARDS_NUMBER - 3; j++) {
					for (int k = j + 1; k < MAX_CARDS_NUMBER - 2; k++) {
						for (int l = k + 1; l < MAX_CARDS_NUMBER - 1; l++) {
							for (int m = l + 1; m < MAX_CARDS_NUMBER; m++) {
								final Card[] card = { cards[i], cards[j], cards[k], cards[l], cards[m] };
								currHand = getHandValue(card);
								if (maxHand == null || currHand.compareTo(maxHand) == 1) {
									maxHand = currHand;
								}
							}
						}
					}
				}
			}
			/**
			 * Adding the player and his highest hand to the gameResult object.
			 */
			gameResult.addPlayerHand(player, maxHand);

		}

		return gameResult;

	}

	/**
	 * Transfers the winners from the map to a List.
	 * 
	 * @param sortedPlayers
	 *            Map containing the state of the game, all the players and their hands.
	 * @return A list of winners. Usually just one, but can be more if there is a tie.
	 */
	private static List<Player> getWinnersList(TreeMap<Player, PokerHand> sortedPlayers) {
		final List<Player> winnersList = new ArrayList<Player>();
		/**
		 * The first player on the map is always the winner, since the map is sorted. Next players can be added to the
		 * map, if there is a tie.
		 */
		final PokerHand bestHand = sortedPlayers.firstEntry().getValue();

		for (Map.Entry<Player, PokerHand> entry : sortedPlayers.entrySet()) {
			final Player player = entry.getKey();
			final PokerHand hand = entry.getValue();

			if (bestHand.compareTo(hand) == 0) {

				winnersList.add(player);
			}
		}

		return winnersList;
	}

	/**
	 * Evaluates the value of an array of 5 cards.
	 * 
	 * @param hand
	 *            An array of 5 cards.
	 * @return A PokerHand with the name of the hand and its values.
	 */
	public static PokerHand getHandValue(Card[] hand) {
		/**
		 * Array containing how many cards of each rank there are.
		 */
		final int[] ranks = new int[Card.CardRank.values().length];
		/**
		 * Array containing how many cards of each color there are.
		 */
		final int[] colors = new int[Card.CardColor.values().length];

		for (int i : ranks) {
			ranks[i] = 0;
		}

		for (int i : colors) {
			colors[i] = 0;
		}
		/**
		 * Populating the arrays.
		 */
		for (int i = 0; i < hand.length; i++) {
			ranks[hand[i].getRank().ordinal()]++;
			colors[hand[i].getColor().ordinal()]++;
		}

		/**
		 * Checking what poker hand rank is supplied.
		 */

		final boolean flush = isFlush(colors);
		final boolean straight = isStraight(ranks);

		if (flush && straight) {
			// straight flush
			return straightFlush(ranks);
		} else {

			int pairs = 0;
			int trios = 0;
			int quatro = 0;
			// count all the pairs, threes and fours
			for (int rank : ranks) {
				if (rank > 0) {
					if (rank == 2) {
						pairs++;
					} else if (rank == 3) {
						trios++;
					} else if (rank == 4) {
						quatro++;
					}
				}
			}

			if (quatro > 0) {
				// four of a kind
				return fourOfAKind(ranks);
			} else if (trios > 0 && pairs > 0 || trios == 2) {
				// full house
				return fullHouse(ranks);
			} else if (flush) {
				// flush
				return flush(ranks);
			} else if (straight) {
				// straight
				return straight(ranks);
			} else if (trios > 0) {
				// three of a kind
				return threeOfAKind(ranks);
			} else if (pairs >= 2) {
				// two pairs
				return twoPairs(ranks);
			} else if (pairs == 1) {
				// pair
				return pair(ranks);
			} else {
				// high card
				return highCard(ranks);
			}
		}
	}

	private static PokerHand straightFlush(int[] table) {
		final int[] straightFlush = new int[2];

		straightFlush[0] = HandName.STRAIGHT_FLUSH.ordinal();

		for (int i = table.length - 1; i > 0; i--) {
			if (table[i] > 0) {
				straightFlush[1] = i;
				break;
			}
		}
		return new PokerHand(straightFlush);
	}

	private static PokerHand fourOfAKind(int[] table) {
		final int[] fourOfAKind = new int[3];

		fourOfAKind[0] = HandName.FOUR_OF_A_KIND.ordinal();
		for (int i = 0; i < table.length; i++) {
			if (table[i] == 4) {
				fourOfAKind[1] = i;
			} else if (table[i] == 1) {
				fourOfAKind[2] = i;
			}
		}
		return new PokerHand(fourOfAKind);
	}

	private static PokerHand flush(int[] table) {
		final int[] flush = new int[6];
		flush[0] = HandName.FLUSH.ordinal();
		int j = 1;
		for (int i = table.length - 1; i >= 0; i--) {
			if (table[i] > 0) {
				flush[j++] = i;
			}
		}
		return new PokerHand(flush);
	}

	private static PokerHand fullHouse(int[] table) {
		final int[] fullHouse = new int[3];
		fullHouse[0] = HandName.FULL_HOUSE.ordinal();

		for (int i = 0; i < table.length; i++) {

			if (table[i] == 3) {
				fullHouse[1] = i;
			} else if (table[i] == 2) {
				fullHouse[2] = i;
			}

		}
		return new PokerHand(fullHouse);
	}

	private static PokerHand straight(int[] table) {
		final int[] straight = new int[2];
		int count = 0; // checking for FIVE high straight
		straight[0] = HandName.STRAIGHT.ordinal();

		for (int i = 0; i < table.length; i++) {
			if (table[i] > 0) {
				count++;
				straight[1] = i;
			} else {
				count = 0;
			}
			if (i == 3 && count == 4 && table[table.length - 1] > 0) { // if TWO, THREE, FOUR and FIVE => check if ACE
				straight[1] = 3;
				break;
			}
		}

		return new PokerHand(straight);
	}

	private static PokerHand threeOfAKind(int[] table) {
		final int[] threeOfAKind = new int[4];
		threeOfAKind[0] = HandName.THREE_OF_A_KIND.ordinal();

		int j = 2;
		for (int i = table.length - 1; i >= 0; i--) {
			if (table[i] > 0) {
				if (table[i] == 3) {
					threeOfAKind[1] = i;
				} else {
					threeOfAKind[j++] = i;
				}
			}
		}
		return new PokerHand(threeOfAKind);
	}

	private static PokerHand twoPairs(int[] table) {
		final int[] twoPairs = new int[4];
		twoPairs[0] = HandName.TWO_PAIRS.ordinal();

		int j = 1;
		for (int i = table.length - 1; i >= 0; i--) {
			if (table[i] > 0) {
				if (table[i] == 2) {
					twoPairs[j++] = i;
				} else {
					twoPairs[3] = i;
				}
			}
		}
		return new PokerHand(twoPairs);
	}

	private static PokerHand pair(int[] table) {
		final int[] pair = new int[5];
		pair[0] = HandName.PAIR.ordinal();

		int j = 2;
		for (int i = table.length - 1; i >= 0; i--) {
			if (table[i] > 0) {
				if (table[i] == 2) {
					pair[1] = i;
				} else {
					pair[j++] = i;
				}
			}
		}

		return new PokerHand(pair);
	}

	private static PokerHand highCard(int[] table) {
		final int[] highCard = new int[6];
		highCard[0] = HandName.HIGH_CARD.ordinal();

		int j = 1;
		for (int i = table.length - 1; i >= 0; i--) {
			if (table[i] > 0) {
				highCard[j++] = i;
			}
		}
		return new PokerHand(highCard);
	}

	private static boolean isFlush(int[] table) {
		return countMaxTableValue(table) == 5;
	}

	private static boolean isStraight(int[] pTable) {
		return longestStreak(pTable) >= 5;
	}

	/**
	 * Finding the longest streak in an array.
	 * 
	 * @param table
	 *            The array to search for longest streaks.
	 * @return The max value of numbers next to each other in the array.
	 */
	private static int longestStreak(int[] table) {
		int maxValue = 0;
		int tempValue = 0;

		for (int i = 0; i < table.length; i++) {
			if (i == 4 && maxValue == 4 && table[table.length - 1] > 0) { // if TWO, FREE, FOUR, FIVE and ACE STRAIGHT
				maxValue++;
			}
			if (table[i] > 0) {
				tempValue++;

				if (tempValue > maxValue) {
					maxValue = tempValue;
				}
			} else {
				tempValue = 0;
			}
		}

		return maxValue;
	}

	private static int countMaxTableValue(int[] table) {
		int maxValue = 0;
		for (int element : table) {

			if (element > maxValue) {
				maxValue = element;
			}
		}
		return maxValue;
	}

	public enum HandName {
		// @formatter:off
		HIGH_CARD("High card"), 
		PAIR("Pair"), 
		TWO_PAIRS("Two pairs"), 
		THREE_OF_A_KIND("Three of a kind"), 
		STRAIGHT("Straight"), 
		FLUSH("Flush"), 
		FULL_HOUSE("Full house"), 
		FOUR_OF_A_KIND("Four of a kind"), 
		STRAIGHT_FLUSH("Straight flush");
		// @formatter:on

		private final String mName;

		private HandName(String name) {
			mName = name;
		}

		public String getCombinationName() {
			return mName;
		}

	}

	static class GameResult {
		/**
		 * Unsorted map on which the sorted tree map will be created.
		 */
		private final Map<Player, PokerHand> mPlayersHands;
		/**
		 * Sorted map based on the playersHands map.
		 */
		private final TreeMap<Player, PokerHand> mSortedHands;
		/**
		 * List of winners.
		 */
		private List<Player> mWinners;

		/**
		 * Constructor method. GameResult holds all game information - which player has what cards.
		 */
		public GameResult() {
			mPlayersHands = new HashMap<Player, PokerHand>();
			final HandComparator handCompare = new HandComparator(mPlayersHands);
			mSortedHands = new TreeMap<Player, PokerHand>(handCompare);
		}

		/**
		 * Adds a player and his highest cards to the non-sorted map.
		 * 
		 * @param player
		 *            The Player to be added to the map.
		 * @param hand
		 *            The highest set of 5 cards from the 7 on the table and in hand.
		 */
		public void addPlayerHand(Player player, PokerHand hand) {
			mPlayersHands.put(player, hand);
		}

		/**
		 * Returns the name of the players highest hand.
		 * 
		 * @param player
		 *            The player whose hand is required.
		 * @return The name of the hand.
		 */
		public String getPlayerResult(Player player) {
			return HandName.values()[mPlayersHands.get(player).getHand()[0]].mName;
		}

		/**
		 * Returns a sorted map of all the players (sorted from winner to lowest ranked player) and their cards.
		 * 
		 * @return The map of players.
		 */
		public Map<Player, PokerHand> getSortedMap() {
			sort();
			return mSortedHands;
		}

		/**
		 * Returns the list of winners. Usually only one, except when there is a tie.
		 * 
		 * @return
		 */
		public List<Player> getWinners() {
			sort();
			return mWinners;
		}

		/**
		 * Sorts the unsorted Map into TreeMap
		 */
		private void sort() {
			mSortedHands.putAll(mPlayersHands);
			mWinners = getWinnersList(mSortedHands);
		}
	}

	/**
	 * A simple comparator class. Compares the values of the hand of each player.
	 * 
	 * 
	 */
	private static class HandComparator implements Comparator<Player> {

		private final Map<Player, PokerHand> mBase;

		public HandComparator(Map<Player, PokerHand> base) {
			mBase = base;
		}

		@Override
		public int compare(Player lhs, Player rhs) {
			if (mBase.get(rhs).compareTo(mBase.get(lhs)) == 0) {
				return rhs.getNodeName().compareTo(lhs.getNodeName());
			} else {
				return mBase.get(rhs).compareTo(mBase.get(lhs));
			}
		}
	}

}
