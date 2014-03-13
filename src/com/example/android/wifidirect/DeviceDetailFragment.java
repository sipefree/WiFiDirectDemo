/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.wifidirect;

import ui.WiFiDirectActivity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.wifidirect.DeviceListFragment.DeviceActionListener;

/**
 * A fragment that manages a particular peer and allows interaction with device
 * i.e. setting up network connection and transferring data.
 */
public class DeviceDetailFragment extends Fragment implements
		ConnectionInfoListener
{

	protected static final int CHOOSE_FILE_RESULT_CODE = 20;
	private View mContentView = null;
	private WifiP2pDevice device;
	ProgressDialog progressDialog = null;
	private PlayerInfo playerinfo = null;
	private WiFiDirectActivity activity;

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{

		mContentView = inflater.inflate(R.layout.device_detail, null);
		mContentView.findViewById(R.id.btn_connect).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v)
					{
						WifiP2pConfig config = new WifiP2pConfig();
						config.deviceAddress = device.deviceAddress;
						config.wps.setup = WpsInfo.PBC;
						if (progressDialog != null
								&& progressDialog.isShowing())
						{
							progressDialog.dismiss();
						}
						progressDialog = ProgressDialog.show(getActivity(),
								"Press back to cancel", "Connecting to :"
										+ device.deviceAddress, true, true);
						((DeviceActionListener) getActivity()).connect(config);
					}
				});

		mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v)
					{
						((DeviceActionListener) getActivity()).disconnect();
					}
				});

		activity = (WiFiDirectActivity) getActivity();

		return mContentView;
	}

	@Override
	public void onConnectionInfoAvailable(final WifiP2pInfo info)
	{
		if (progressDialog != null && progressDialog.isShowing())
		{
			progressDialog.dismiss();
		}
		this.getView().setVisibility(View.VISIBLE);

		// The owner IP is now known.
		TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
		view.setText(getResources().getString(R.string.group_owner_text)
				+ ((info.isGroupOwner == true) ? getResources().getString(
						R.string.yes) : getResources().getString(R.string.no)));

		// InetAddress from WifiP2pInfo struct.
		view = (TextView) mContentView.findViewById(R.id.device_info);
		view.setText("Group Owner IP - "
				+ info.groupOwnerAddress.getHostAddress());

		// After the group negotiation, we assign the group owner as the file
		// server. The file server is single threaded, single connection server
		// socket.
		if (info.groupFormed && info.isGroupOwner)
		{
			PlayerInfo ownerPlayer = new PlayerInfo(
					info.groupOwnerAddress.getHostAddress(), activity.getPlayerName(),
					true);
			((NewPlayerListener) getActivity()).addPlayer(ownerPlayer);
			new FileServerAsyncTask(getActivity(),
					mContentView.findViewById(R.id.status_text), activity)
					.execute();

		} else if (info.groupFormed)
		{
			// The other device acts as the client. In this case, we send our IP
			// address to group owner
			// and notify user that connection was successful by changing icon
			Intent serviceIntent = new Intent(getActivity(),
					FileTransferService.class);
			serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);

			// TODO: populate player info
			String ip = Utils.getLocalIP();

			serviceIntent.putExtra("PlayerInfo", new PlayerInfo(ip,
					activity.getPlayerName(), false));
			// serviceIntent.putExtra(FileTransferService.EXTRAS_MY_INFO,
			// uri.toString());
			serviceIntent.putExtra(
					FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
					info.groupOwnerAddress.getHostAddress());
			serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT,
					8988);
			getActivity().startService(serviceIntent);

			((TextView) mContentView.findViewById(R.id.status_text))
					.setText(getResources().getString(R.string.client_text));
		}

		// hide the connect button
//		mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
	}

	/**
	 * Updates the UI with device data
	 * 
	 * @param device
	 *            the device to be displayed
	 */
	public void showDetails(WifiP2pDevice device)
	{
		this.device = device;
		this.getView().setVisibility(View.VISIBLE);
		TextView view = (TextView) mContentView
				.findViewById(R.id.device_address);
		view.setText(device.deviceAddress);
		view = (TextView) mContentView.findViewById(R.id.device_info);
		view.setText(device.toString());
		 if(device.status == WifiP2pDevice.CONNECTED)
		 {
		 ImageView image = (ImageView) mContentView.findViewById(R.id.icon);
		 image.setImageResource(R.drawable.connected);
		 }

	}

	/**
	 * Clears the UI fields after a disconnect or direct mode disable operation.
	 */
	public void resetViews()
	{
		mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
		TextView view = (TextView) mContentView
				.findViewById(R.id.device_address);
		view.setText(R.string.empty);
		view = (TextView) mContentView.findViewById(R.id.device_info);
		view.setText(R.string.empty);
		view = (TextView) mContentView.findViewById(R.id.group_owner);
		view.setText(R.string.empty);
		view = (TextView) mContentView.findViewById(R.id.status_text);
		view.setText(R.string.empty);
		mContentView.findViewById(R.id.btn_start_client).setVisibility(
				View.GONE);
		this.getView().setVisibility(View.GONE);
	}

	public interface NewPlayerListener
	{

		void addPlayer(PlayerInfo player);
	}

}
