package chord;

import com.samsung.chord.IChordChannelListener;

/**
 * Helper class that adds logging capabilities to some of the IChordChannelListener methods. Can be used for debugging
 * purposes.
 */
public class LoggedChordChannelListener implements IChordChannelListener {

	private final IChordChannelListener mChordChannelListener;

	/**
	 * Creates LoggedChordChannelListener that can be used for debugging purposes.
	 * 
	 * @param listener
	 *            IChordChannelListener wrapped in LoggedChordChannelListener
	 */
	public LoggedChordChannelListener(IChordChannelListener listener) {
		mChordChannelListener = listener;
	}

	@Override
	public void onDataReceived(String fromNode, String fromChannel, String payloadType, byte[][] payload) {
//		Log.d(GameActivity.TAG, "onDataReceived[CHANNEL:" + fromChannel + "][" + fromNode + "] "
//				+ getMessageDescription(payload));
		mChordChannelListener.onDataReceived(fromNode, fromChannel, payloadType, payload);
	}

	@Override
	public void onFileChunkReceived(String fromNode, String fromChannel, String fileName, String hash, String fileType,
			String exchangeId, long fileSize, long offset) {
		mChordChannelListener.onFileChunkReceived(fromNode, fromChannel, fileName, hash, fileType, exchangeId,
				fileSize, offset);
	}

	@Override
	public void onFileChunkSent(String toNode, String toChannel, String fileName, String hash, String fileType,
			String exchangeId, long fileSize, long offset, long chunkSize) {
		mChordChannelListener.onFileChunkSent(toNode, toChannel, fileName, hash, fileType, exchangeId, fileSize,
				offset, chunkSize);
	}

	@Override
	public void onFileFailed(String node, String channel, String fileName, String hash, String exchangeId, int reason) {
		mChordChannelListener.onFileFailed(node, channel, fileName, hash, exchangeId, reason);
	}

	@Override
	public void onFileReceived(String fromNode, String fromChannel, String fileName, String hash, String fileType,
			String exchangeId, long fileSize, String tmpFilePath) {
		mChordChannelListener.onFileReceived(fromNode, fromChannel, fileName, hash, fileType, exchangeId, fileSize,
				tmpFilePath);
	}

	@Override
	public void onFileSent(String toNode, String toChannel, String fileName, String hash, String fileType,
			String exchangeId) {
		mChordChannelListener.onFileSent(toNode, toChannel, fileName, hash, fileType, exchangeId);
	}

	@Override
	public void onFileWillReceive(String fromNode, String fromChannel, String fileName, String hash, String fileType,
			String exchangeId, long fileSize) {
		mChordChannelListener.onFileWillReceive(fromNode, fromChannel, fileName, hash, fileType, exchangeId, fileSize);
	}

	@Override
	public void onNodeJoined(String fromNode, String fromChannel) {
		mChordChannelListener.onNodeJoined(fromNode, fromChannel);
	}

	@Override
	public void onNodeLeft(String fromNode, String fromChannel) {
		mChordChannelListener.onNodeLeft(fromNode, fromChannel);
	}

	private String getMessageDescription(byte[][] payload) {
		return ChordMessage.obtainChordMessage(payload[0], null).toString();
	}

}
