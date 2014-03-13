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
import chord.AbstractChord.NodeJoinedOnPublicChannelEvent;
import chord.AbstractChord.NodeLeftOnPublicChannelEvent;
import chord.ConnectionChord;
import chord.ConnectionChord.OnServerListChangedListener;
import logic.CommunicationBus;
import logic.CommunicationBus.BusManager;

/**
 * {@link DialogFragment} for choosing server from the list of discovered servers.
 */
public class ChooseServerDialog extends DialogFragment implements chord.ConnectionChord.OnServerListChangedListener, OnItemClickListener,
		logic.CommunicationBus.BusManager {

	private chord.ConnectionChord mConnectionChord;
	private OnServerChosenListener mOnServerChosenListener;
	private ServerAdapter mServerAdapter;
	private Bus mBus;
	private MenuActivity mParentActivity;

	@Subscribe
	public void onNodeLeftOnPublicChannel(chord.AbstractChord.NodeLeftOnPublicChannelEvent event) {
		findServers();
	}

	@Subscribe
	public void onNodeJoinedOnPublicChannel(chord.AbstractChord.NodeJoinedOnPublicChannelEvent event) {
		findServers();
	}

	private void findServers() {
		mConnectionChord.findServers();
	}

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
		mConnectionChord = new ConnectionChord(getActivity(), GameActivity.GAME_NAME, ChooseServerDialog.this);
		mOnServerChosenListener = (OnServerChosenListener) getActivity();
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
		mServerAdapter = new ServerAdapter();
		serversListView.setAdapter(mServerAdapter);
		return view;
	}

	@Override
	public void onDestroy() {
		stopBus();
		mConnectionChord.stopChord();
		super.onDestroy();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		mOnServerChosenListener.onServerChosen(((ServerAdapter) parent.getAdapter()).getItem(position));
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

	@Override
	public void onChanged(List<String> availableServers) {
		mServerAdapter.setServersList(availableServers);
	}

	/**
	 * Interface definition for a callback to be invoked when server is chosen.
	 */
	public interface OnServerChosenListener {

		/**
		 * Called when a view with server's name has been clicked.
		 * 
		 * @param serverName
		 *            name of the clicked server
		 */
		void onServerChosen(String serverName);

	}

	/**
	 * Interface definition for a callback to be invoked when a servers list has changed.
	 */
	public interface OnServerListChangedListener {

		void onChanged(List<String> availableServers);

	}

	private class ServerAdapter extends BaseAdapter {

		private final List<String> mServers;

		public ServerAdapter() {
			super();
			mServers = new ArrayList<String>();
		}

		public void setServersList(List<String> servers) {
			if (!(mServers.size() == servers.size() && mServers.containsAll(servers))) {
				mServers.clear();
				mServers.addAll(servers);
				Collections.sort(mServers);
				notifyDataSetChanged();
			}
		}

		@Override
		public int getCount() {
			return mServers.size();
		}

		@Override
		public String getItem(int position) {
			return mServers.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final TextView textView = (TextView) View.inflate(getActivity(), R.layout.server_name_text_view, null);
			textView.setText(mServers.get(position));
			return textView;
		}
	}

}
