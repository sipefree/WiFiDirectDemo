/*
 ********************************************************************************
 * Copyright (c) 2013 Samsung Electronics, Inc.
 * All rights reserved.
 *
 * This software is a confidential and proprietary information of Samsung
 * Electronics, Inc. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with Samsung Electronics.
 ********************************************************************************
 */
package com.srpol.poker.logic;

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
