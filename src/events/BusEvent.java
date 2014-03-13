package events;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import utils.Preconditions;


/**
 * Represents game events passed through the Bus.
 */
public class BusEvent implements Serializable {

	private static final long serialVersionUID = 20130321L;

	private Map<String, Object> mPayload;

	protected final String getString(String key) {
		return (String) Preconditions.checkNotNull(mPayload.get(key));
	}

	protected final int getInt(String key) {
		return (Integer) mPayload.get(key);
	}

	protected final Object getObject(String key) {
		return Preconditions.checkNotNull(mPayload.get(key));
	}

	protected final void putString(String key, String value) {
		putPayload(key, value);
	}

	protected final void putInt(String key, int value) {
		putPayload(key, value);
	}

	protected final void putObject(String key, Object value) {
		putPayload(key, value);
	}

	private void putPayload(String key, Object value) {
		if (mPayload == null) {
			mPayload = new HashMap<String, Object>();
		}
		mPayload.put(key, Preconditions.checkNotNull(value));
	}

	@Override
	public String toString() {
		return "BusEvent [mPayload=" + mPayload + "]";
	}

}
