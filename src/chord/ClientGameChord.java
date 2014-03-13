package chord;

import android.content.Context;
import chord.ChordMessage.MessageType;

import com.squareup.otto.Subscribe;

/**
 * Class responsible for handling {@link ChordMessage}s related to the client side of the game.
 */
public class ClientGameChord extends GameChord {

	private String mServerNodeName;

	public ClientGameChord(Context context, String roomName, String gameName, String userName) {
		super(context, roomName, gameName, userName);
	}

	@Override
	void handlePrivateMessage(ChordMessage message) {
		switch (message.getType()) {
		case SERVER_NODE_NAME:
			mServerNodeName = message.getString(ChordMessage.SERVER_NODE_NAME);
			final ChordMessage usernameMessage = ChordMessage.obtainMessage(MessageType.USERNAME);
			usernameMessage.putString(ChordMessage.USERNAME, mUserName);
			sendPrivateMessage(usernameMessage);
			mBus.post(JoinedToServerEvent.INSTANCE);
			break;
		default:
			super.handlePrivateMessage(message);
		}
	}

	@Override
	void onNodeLeftOnPrivateChannel(String nodeName) {
		super.onNodeLeftOnPrivateChannel(nodeName);

		if (nodeName.equalsIgnoreCase(mServerNodeName)) {
			mBus.post(ServerDisconnectedEvent.INSTANCE);
		}
	}

	@Override
	void onChordStarted(String userNodeName, int reason) {
		super.onChordStarted(userNodeName, reason);
		joinPrivateChannel(mRoomName);
	}

	@Subscribe
	public void sendPrivateMessage(ChordMessage message) {
		super.sendPrivateMessage(message, mServerNodeName);
	}

}
