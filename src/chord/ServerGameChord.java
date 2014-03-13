package chord;

import logic.PokerLogicController.PokerLogicEvent;
import logic.PokerLogicController.PokerLogicEvent.UsernameEvent;
import logic.PokerLogicController.PokerLogicEvent.BiddingEvent.BiddingType;
import android.content.Context;
import chord.ChordMessage.MessageType;

import com.samsung.chord.IChordChannel;
import com.squareup.otto.Subscribe;


/**
 * Class responsible for handling {@link ChordMessage}s related to the server side of the game.
 */
public class ServerGameChord extends GameChord {

	public ServerGameChord(Context context, String roomName, String gameName, String userName) {
		super(context, roomName, gameName, userName);
	}

	@Override
	void handlePrivateMessage(ChordMessage message) {
		switch (message.getType()) {
		case SIT:
		case STAND:
		case FOLD:
		case ALL_IN:
		case BIDDING:
		case USERNAME:
			handleMessageFromClient(message);
			break;
		default:
			super.handlePrivateMessage(message);
		}
	}

	@Subscribe
	public void handleMessageFromLocalClient(ChordMessage message) {
		if (!message.isFromLogic()) {
			message.setSenderNodeName(getNodeName());
			handleMessageFromClient(message);
		}
	}

	@Subscribe
	public void handleMessageFromLogic(ChordMessage message) {
		if (message.isFromLogic()) {
			if (message.getReceiverNodeName().equalsIgnoreCase(getNodeName())) {
				handlePrivateMessage(message);
			} else {
				sendPrivateMessage(message, message.getReceiverNodeName());
			}
		}
	}

	private void handleMessageFromClient(ChordMessage message) {
		final String senderNodeName = message.getSenderNodeName();

		switch (message.getType()) {
		case SIT:
			postPokerLogicEvent(new PokerLogicEvent.SitEvent(senderNodeName));
			break;
		case STAND:
			postPokerLogicEvent(new PokerLogicEvent.StandEvent(senderNodeName));
			break;
		case BIDDING:
			postPokerLogicEvent(new PokerLogicEvent.BiddingEvent(senderNodeName, message.getInt(ChordMessage.AMOUNT),
					(BiddingType) message.getObject(ChordMessage.BIDDING_TYPE)));
			break;
		case FOLD:
			postPokerLogicEvent(new PokerLogicEvent.FoldEvent(senderNodeName));
			break;
		case ALL_IN:
			postPokerLogicEvent(new PokerLogicEvent.AllInEvent(senderNodeName, message.getInt(ChordMessage.AMOUNT)));
			break;
		case USERNAME:
			postPokerLogicEvent(new PokerLogicEvent.UsernameEvent(message.getString(UsernameEvent.USERNAME),
					senderNodeName));
			break;
		default:
			throw new IllegalArgumentException(message.getType().name());
		}
	}

	private <T extends PokerLogicEvent> void postPokerLogicEvent(T event) {
		mBus.post(event);
	}

	@Override
	void handlePublicMessage(ChordMessage message) {
		switch (message.getType()) {
		case GET_SERVERS_LIST:
			final ChordMessage chordMessage = ChordMessage.obtainMessage(MessageType.SERVER_NAME_BROADCAST);
			chordMessage.putString(ChordMessage.PRIVATE_CHANNEL_NAME, mRoomName);
			sendPublicMessage(chordMessage);
			break;
		case SERVER_NAME_BROADCAST:
			// Does nothing intentionally.
			break;
		default:
			super.handlePublicMessage(message);
		}
	}

	@Override
	void onChordStarted(String myNodeName, int reason) {
		super.onChordStarted(myNodeName, reason);
		joinPublicChannel();
		joinPrivateChannel(mRoomName);
	}

	@Override
	void onJoinedToPrivateChannel(IChordChannel privateChannel) {
		final ChordMessage usernameMessage = ChordMessage.obtainMessage(MessageType.USERNAME);
		usernameMessage.putString(ChordMessage.USERNAME, mUserName);
		handleMessageFromLocalClient(usernameMessage);
		mBus.post(JoinedToServerEvent.INSTANCE);
		super.onJoinedToPrivateChannel(privateChannel);
	}

	@Override
	void onNodeJoinedOnPrivateChannel(String nodeName) {
		super.onNodeJoinedOnPrivateChannel(nodeName);
		final ChordMessage chordMessage = ChordMessage.obtainMessage(MessageType.SERVER_NODE_NAME);
		chordMessage.putString(ChordMessage.SERVER_NODE_NAME, getNodeName());
		sendPrivateMessage(chordMessage, nodeName);
	}

	@Override
	void onNodeLeftOnPrivateChannel(String nodeName) {
		super.onNodeLeftOnPrivateChannel(nodeName);
		mBus.post(new ClientDisconnectedEvent(nodeName));
	}

}
