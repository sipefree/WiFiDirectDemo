package com.example.android.wifidirect;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
public class MessageAsyncTask extends AsyncTask<Void, Void, Void>
{

	private Context context;
	private TextView statusText;
	private WiFiDirectActivity activity;
	private String msg;
	private InetAddress host;

	/**
	 * @param context
	 * @param statusText
	 */
	public MessageAsyncTask(Context context, View statusText, InetAddress host,
			String msg)
	{
		this.context = context;
		this.statusText = (TextView) statusText;
		this.msg = msg;
		this.host = host;
	}

	@Override
	protected Void doInBackground(Void... params)
	{
		Socket socket = new Socket();
		try
		{
			// Send a message to all other players

			socket.bind(null);
			socket.connect((new InetSocketAddress(host, 7023)), 5000);
			OutputStream stream = socket.getOutputStream();
			byte[] buffer = serialize(msg);
			stream.write(buffer);

		} catch (IOException e)
		{
			Log.e(WiFiDirectActivity.TAG, e.getMessage());

		} finally
		{
			if (socket != null && socket.isConnected())
			{
				try
				{
					socket.close();
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(Void result)
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
		statusText.setText("Opening a socket");

	}

	private byte[] serialize(Object obj) throws IOException
	{
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		ObjectOutputStream o = new ObjectOutputStream(b);
		o.writeObject(obj);
		return b.toByteArray();
	}

}