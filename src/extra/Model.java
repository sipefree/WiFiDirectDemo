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

import com.srpol.poker.logic.CommunicationBus.BusManager;
import com.srpol.poker.utils.Preconditions;

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
