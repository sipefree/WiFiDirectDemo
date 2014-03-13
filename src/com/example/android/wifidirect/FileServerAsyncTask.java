package com.example.android.wifidirect;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import ui.WiFiDirectActivity;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * A simple server socket that accepts connection and writes some data on the
 * stream.
 */
public class FileServerAsyncTask extends AsyncTask<Void, Void, PlayerInfo>
{

	private Context context;
	private TextView statusText;
	private WiFiDirectActivity activity;

	/**
	 * @param context
	 * @param statusText
	 */
	public FileServerAsyncTask(Context context, View statusText,
			WiFiDirectActivity activity)
	{
		this.context = context;
		this.statusText = (TextView) statusText;
		this.activity = activity;
	}

	@Override
	protected PlayerInfo doInBackground(Void... params)
	{
		try
		{
			ServerSocket serverSocket = new ServerSocket(8988);
			Log.v("Msg", "Server: Socket opened");
			Socket client = serverSocket.accept();
			Log.v("Msg", "Server: connection done");

			Log.v("Msg", "server: received message ");
			InputStream inputstream = client.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			MessageServer.copyFile(inputstream, baos);
			PlayerInfo info = (PlayerInfo) MessageServer.deserialize(baos
					.toByteArray());
			activity.addPlayer(info);
			serverSocket.close();
			return info;
		} catch (IOException e)
		{
			Log.e(WiFiDirectActivity.TAG, e.getMessage());
			return null;
		} catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(PlayerInfo result)
	{
		if (result != null)
		{
			statusText.setText("Connected - " + result);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute()
	{
		statusText.setText("Opening a server socket");

	}

}