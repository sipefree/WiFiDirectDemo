package logic;

import com.squareup.otto.Bus;

/**
 * Singleton class that provides {@link Bus} instance.
 */
public final class CommunicationBus {

	private static final Bus BUS = new Bus();

	/**
	 * Returns the singleton {@link Bus} instance.
	 * 
	 * @return singleton {@link Bus} instance
	 */
	public static Bus getInstance() {
		return BUS;
	}

	private CommunicationBus() {
		// No op.
	}

	/**
	 * Interface used for managing {@link Bus} state.
	 */
	public interface BusManager {
		/**
		 * Registers in the {@link Bus} by calling its register method.
		 */
		void startBus();

		/**
		 * Unregisters from the {@link Bus} by calling its unregister method.
		 */
		void stopBus();
	}
}
