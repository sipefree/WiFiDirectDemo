// Copyright 2011 Google Inc. All Rights Reserved.

package com.example.android.wifidirect;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import ui.WiFiDirectActivity;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and sending IP address to
 * enable bi-directional communication
 */
public class FileTransferService extends IntentService
{

	private static final int SOCKET_TIMEOUT = 5000;
	public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
	public static final String EXTRAS_MY_INFO = "my_info";
	public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
	public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";

	public FileTransferService(String name)
	{
		super(name);
	}

	public FileTransferService()
	{
		super("FileTransferService");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	protected void onHandleIntent(Intent intent)
	{

		Context context = getApplicationContext();
		if (intent.getAction().equals(ACTION_SEND_FILE))
		{

			// Get PlayerInfo from activity via intent extras
			PlayerInfo myInfo = (PlayerInfo) intent
					.getSerializableExtra("PlayerInfo");

			String host = intent.getExtras().getString(
					EXTRAS_GROUP_OWNER_ADDRESS);
			Socket socket = new Socket();
			int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);

			// Open a new socket and connect to the server
			try
			{
				Log.d(WiFiDirectActivity.TAG, "Opening client socket - ");
				socket.bind(null);
				socket.connect((new InetSocketAddress(host, port)),
						SOCKET_TIMEOUT);

				Log.d(WiFiDirectActivity.TAG,
						"Client socket - " + socket.isConnected());

				// Write the PlayerInfo to the server
				OutputStream stream = socket.getOutputStream();
				byte[] buffer = serialize(myInfo);
				stream.write(buffer);

				Log.d(WiFiDirectActivity.TAG, "Client: Data written");
			} catch (IOException e)
			{
				Log.e(WiFiDirectActivity.TAG, e.getMessage());
			} finally
			{
				if (socket != null)
				{
					if (socket.isConnected())
					{
						try
						{
							socket.close();
						} catch (IOException e)
						{
							// Give up
							e.printStackTrace();
						}
					}
				}
			}

		}
	}

	private byte[] serialize(Object obj) throws IOException
	{
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		ObjectOutputStream o = new ObjectOutputStream(b);
		o.writeObject(obj);
		return b.toByteArray();
	}
}
