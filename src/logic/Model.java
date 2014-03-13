package logic;
import utils.Preconditions;
import logic.CommunicationBus.BusManager;

/**
 * Container for the {@link ServerModel} and {@link ClientModel}.
 */
public class Model implements BusManager {

	private ServerModel mServerModel;
	private final ClientModel mClientModel;
	private final boolean mIsServer;

	public Model(boolean isServer) {
		mIsServer = isServer;

		if (mIsServer) {
			mServerModel = new ServerModel();
		}
		mClientModel = new ClientModel();
	}

	@Override
	public void startBus() {
		if (mIsServer) {
			mServerModel.startBus();
		}
		mClientModel.startBus();
	}

	@Override
	public void stopBus() {
		if (mIsServer) {
			mServerModel.stopBus();
		}
		mClientModel.stopBus();
	}

	public ServerModel getServerModel() {
		return Preconditions.checkNotNull(mServerModel);
	}

}
