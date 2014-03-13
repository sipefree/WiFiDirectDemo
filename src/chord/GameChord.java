package chord;

import android.content.Context;
import android.util.Pair;
import extra.Card;
import extra.ClientModel.ClientModelEvent;
import extra.ClientModel.ClientModelEvent.BlindEvent.BlindType;
import extra.GameActivity.GameActivityEvent;

/**
 * Class responsible for handling {@link ChordMessage}s related to the game.
 */
public abstract class GameChord extends AbstractChord {

	final String mUserName;
	final String mRoomName;

	GameChord(Context context, String roomName, String gameName, String userName) {
		super(context, gameName);
		mUserName = userName;
		mRoomName = roomName;
	}

	@Override
	void handlePrivateMessage(ChordMessage message) {
		switch (message.getType()) {
		case DEALER:
			mBus.post(new ClientModelEvent.DealerEvent());
			break;
		case BLIND:
			mBus.post(new ClientModelEvent.BlindEvent(message.getInt(ChordMessage.AMOUNT), (BlindType) message
					.getObject(ChordMessage.BLIND_TYPE)));
			break;
		case CARD_PAIR:
			final Pair<Card, Card> cardPair = new Pair<Card, Card>((Card) message.getObject(ChordMessage.FIRST_CARD),
					(Card) message.getObject(ChordMessage.SECOND_CARD));
			mBus.post(new ClientModelEvent.CardsEvent(cardPair));
			break;
		case PLAYER_STATE:
			mBus.post(new ClientModelEvent.PlayerStateEvent(message.getInt(ChordMessage.AMOUNT)));
			break;
		case YOUR_TURN:
			mBus.post(new ClientModelEvent.YourTurnEvent(message.getInt(ChordMessage.AMOUNT)));
			break;
		case GAME_END:
			mBus.post(new ClientModelEvent.GameEnd(message.getInt(ChordMessage.AMOUNT)));
			break;
		case TABLE_FULL:
			mBus.post(new GameActivityEvent.TableFullEvent());
			break;
		case CLEAR_CARDS:
			mBus.post(new ClientModelEvent.ClearCardsEvent());
			break;
		case ALLOW_SIT:
			mBus.post(new GameActivityEvent.SitEvent());
			break;
		case GET_SERVERS_LIST:
		case SIT:
		case STAND:
		case SERVER_NAME_BROADCAST:
		case USERNAME:
		case FOLD:
		case ALL_IN:
		case BIDDING:
		case SERVER_NODE_NAME:
			throw new IllegalArgumentException(message.getType().name());
		default:
			super.handlePrivateMessage(message);
		}
	}

	public enum ServerDisconnectedEvent {
		INSTANCE;
	}

	public static class ClientDisconnectedEvent {

		private final String mNodeName;

		public ClientDisconnectedEvent(String nodeName) {
			mNodeName = nodeName;
		}

		public String getNodeName() {
			return mNodeName;
		}
	}

	public enum JoinedToServerEvent {
		INSTANCE;
	}

}
