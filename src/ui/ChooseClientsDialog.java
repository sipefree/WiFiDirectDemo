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
package ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import wifidirect.ConnectionTest;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.wifidirect.R;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import logic.CommunicationBus;
import logic.CommunicationBus.BusManager;

/**
 * {@link DialogFragment} for choosing server from the list of discovered servers.
 */
public class ChooseClientsDialog extends DialogFragment implements PeerListListener, ConnectionInfoListener, OnItemClickListener,
		logic.CommunicationBus.BusManager {

	private ClientAdapter mClientAdapter;
	private OnClientAddedListener mOnClientAddedListener;
	private Bus mBus;
	private MenuActivity mParentActivity;

	@Override
	public void onAttach(Activity parent) {
		mParentActivity = (MenuActivity) parent;
		super.onAttach(parent);
	}

	@Override
	public void onDetach() {
		mParentActivity.enableClientButton();
		super.onDetach();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBus = logic.CommunicationBus.getInstance();
		mOnClientAddedListener = (OnClientAddedListener) getActivity();
		startBus();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setTitle(R.string.poker_rooms);
		dialog.setCanceledOnTouchOutside(false);
		return dialog;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.choose_server_dialog_fragment, container);

		final ListView serversListView = (ListView) view.findViewById(R.id.servers_list_view);
		serversListView.setOnItemClickListener(this);
		mClientAdapter = new ClientAdapter();
		serversListView.setAdapter(mClientAdapter);
		return view;
	}

	@Override
	public void onDestroy() {
		stopBus();
		super.onDestroy();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		mOnClientAddedListener.onClientAdded(((ClientAdapter) parent.getAdapter()).getItem(position));
		dismiss();
	}

	@Override
	public void startBus() {
		mBus.register(this);
	}

	@Override
	public void stopBus() {
		mBus.unregister(this);
	}
	
	public void onChanged(List<String> availableServers) {
		mClientAdapter.setClientsList(availableServers);
	}

	/**
	 * Interface definition for a callback to be invoked when server is chosen.
	 */
	public interface OnClientAddedListener {

		/**
		 * Called when a view with server's name has been clicked.
		 * 
		 * @param clientName
		 *            name of the clicked server
		 */
		void onClientAdded(String clientName);

	}

	/**
	 * Interface definition for a callback to be invoked when a servers list has changed.
	 */
	public interface OnServerListChangedListener {

		void onChanged(List<String> availableServers);

	}

	private class ClientAdapter extends BaseAdapter {

		private final List<String> mClients;

		public ClientAdapter() {
			super();
			mClients = new ArrayList<String>();
		}

		public void setClientsList(List<String> servers) {
			if (!(mClients.size() == servers.size() && mClients.containsAll(servers))) {
				mClients.clear();
				mClients.addAll(servers);
				Collections.sort(mClients);
				notifyDataSetChanged();
			}
		}

		@Override
		public int getCount() {
			return mClients.size();
		}

		@Override
		public String getItem(int position) {
			return mClients.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final TextView textView = (TextView) View.inflate(getActivity(), R.layout.server_name_text_view, null);
			textView.setText(mClients.get(position));
			return textView;
		}
	}

	@Override
	public void onPeersAvailable(WifiP2pDeviceList arg0) {
		// TODO Auto-generated method stub
		
	}

}
