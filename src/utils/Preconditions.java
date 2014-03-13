package utils;
public class Preconditions {

	private Preconditions() {
		// No. op.
	}

	public static <T> T checkNotNull(T reference) {
		if (reference == null) {
			throw new NullPointerException();
		}
		return reference;
	}

	public static void checkState(boolean state) {
		if (!state) {
			throw new IllegalStateException();
		}
	}

	public static void checkState(boolean state, String message) {
		if (!state) {
			throw new IllegalStateException(message);
		}
	}

	public static void checkArgument(boolean argument) {
		if (!argument) {
			throw new IllegalArgumentException();
		}
	}

	public static void checkArgument(boolean argument, String message) {
		if (!argument) {
			throw new IllegalArgumentException(message);
		}
	}

	public static <T> T firstNonNull(T first, T second) {
		return first == null ? second : first;
	}

}
